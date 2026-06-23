package com.wly.beansprout.feature.floating

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.Service
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.wly.beansprout.R
import com.wly.beansprout.core.TouchEventManager
import com.wly.beansprout.core.utils.DensityUtil
import com.wly.beansprout.core.utils.PathFinder
import com.wly.beansprout.core.utils.WindowUtils
import com.wly.beansprout.data.model.Point

/**
 * 悬浮窗服务
 *
 * 管理悬浮窗动画小鸡形象，包括：
 * - 拖动移动
 * - 基础动画（眨眼/挥手）
 * - 随机动画（扭动/呀呦/变身/扭头）
 * - 溜达鸡动画（移动到目标点）
 * - 闪现鸡动画（收缩→闪现→冒出→工作）
 * - 跳绳动画（执行触控动作时）
 * - 菜单弹窗（添加/停止/话术/退出）
 * - 福袋卡点时间显示
 */
class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var layoutFloatingView: LinearLayout
    private lateinit var viewChickenImage: ImageView
    private lateinit var txviTime: TextView
    private lateinit var floatLayoutParams: WindowManager.LayoutParams

    private var menuDialog: FloatingMenuDialog? = null
    private val handler = Handler(Looper.getMainLooper())

    /**
     * 功能类型：
     * 0-其它 1-单击 2-点赞 3-向下滑动 4-向上滑动 5-向左滑动 6-向右滑动 7-自动回复 8-抢福袋
     */
    private var functionType: Int = 0

    /**
     * 小鸡模型：1-功德小鸡（闪现） 2-其它小鸡（溜达）
     */
    private var chickModel: Int = 1

    /**
     * 卡福袋时间：0-不限制 8-8分钟以下 6-6分钟以下 4-4分钟以下 3-3分钟以下 2-2分钟以下
     * 999-立即参与 998-5~10随机 997-0~5随机
     */
    private var luckybagTime: Int = 0

    // 当前拖拽位置（绝对屏幕坐标）
    private var currentX: Int = 0
    private var currentY: Int = 0
    // 目标触点坐标
    private var targetX: Int = 0
    private var targetY: Int = 0
    // 是否正在拖动
    private var isMoving: Boolean = false
    // 最后一次按下时间（防双击）
    private var lastClickTime: Long = 0L

    // 基础动画（眨眼+挥手）
    private var basicAnim: AnimationDrawable? = null
    // 随机动画
    private var randomAnim: AnimationDrawable? = null
    // 跳绳动画
    private var skippingRopeAnim: AnimationDrawable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // 初始化悬浮窗布局
        @Suppress("UNCHECKED_CAST")
        layoutFloatingView = LayoutInflater.from(this)
            .inflate(R.layout.layout_window, null) as LinearLayout
        viewChickenImage = layoutFloatingView.findViewById(R.id.imvi_layoutwindow_chicken)
        txviTime = layoutFloatingView.findViewById(R.id.txt_layoutwindow_time)

        // 设置 WindowManager 布局参数
        val width = DensityUtil.dip2px(this, 100f)
        val height = DensityUtil.dip2px(this, 130f)
        floatLayoutParams = WindowUtils.newWmParams(width, height)
        floatLayoutParams.gravity = Gravity.TOP or Gravity.START
        floatLayoutParams.x = WindowUtils.getScreenWidth(this) - DensityUtil.dip2px(this, 100f)
        floatLayoutParams.y = WindowUtils.getScreenHeight(this) - DensityUtil.dip2px(this, 200f)

        // 添加到窗口
        windowManager = WindowUtils.getWindowManager(this)
        windowManager.addView(layoutFloatingView, floatLayoutParams)

        // 设置拖动和点击监听
        setupTouchListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            functionType = it.getIntExtra(EXTRA_FUNCTION_TYPE, 0)
            chickModel = it.getIntExtra(EXTRA_CHICK_MODEL, 1)
            luckybagTime = it.getIntExtra(EXTRA_LUCKYBAG_TIME, 0)
        }

        // 启动基础动画（眨眼+挥手）
        val basicAnimRes = if (chickModel == 1) {
            R.drawable.golden_basic_animation
        } else {
            R.drawable.cute_basic_animation
        }
        basicAnim = ContextCompat.getDrawable(this, basicAnimRes) as? AnimationDrawable
        viewChickenImage.setImageDrawable(basicAnim)
        viewChickenImage.post { basicAnim?.start() }

        return START_NOT_STICKY
    }

    /**
     * 设置悬浮窗触摸监听（拖动 + 点击）
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        layoutFloatingView.isClickable = true
        layoutFloatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentX = event.rawX.toInt()
                    currentY = event.rawY.toInt()
                    isMoving = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val nowX = event.rawX.toInt()
                    val nowY = event.rawY.toInt()
                    val moveX = nowX - currentX
                    val moveY = nowY - currentY
                    if (moveX != 0 || moveY != 0) {
                        isMoving = true
                        floatLayoutParams.x += moveX
                        floatLayoutParams.y += moveY
                        windowManager.updateViewLayout(layoutFloatingView, floatLayoutParams)
                        currentX = nowX
                        currentY = nowY
                        true
                    } else {
                        false
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (isMoving) {
                        // 拖动后抬起
                        handleMoveEnd()
                        true
                    } else {
                        // 点击（按下后直接抬起）
                        onShowSelectDialog()
                        true
                    }
                }

                else -> false
            }
        }
    }

    /**
     * 拖动结束后：如果正在触控则开启动画，否则执行随机动画
     */
    private fun handleMoveEnd() {
        if (TouchEventManager.isTouching()) {
            if (functionType != TYPE_LUCKY_BAG) {
                onStartAnimation(targetX, targetY)
            }
        } else {
            // 仅在模型2（溜达鸡）且未跳绳时，执行随机动画
            if (chickModel == 2 && !TouchEventManager.isOpenSkippingRope.value) {
                stopAnimation(basicAnim)
                stopAnimation(randomAnim)

                randomAnim = ContextCompat.getDrawable(this, getRandomAnimRes())
                        as? AnimationDrawable
                viewChickenImage.setImageDrawable(randomAnim)
                viewChickenImage.post { randomAnim?.start() }
            }
        }
    }

    /**
     * 随机动画资源（仅模型2使用）
     */
    private fun getRandomAnimRes(): Int {
        return when ((Math.random() * 4).toInt()) {
            1 -> R.drawable.cute_twisting_animation     // 扭动
            2 -> R.drawable.cute_yayo_animation          // 呀呦
            3 -> R.drawable.cute_transformation_animation // 变身
            else -> R.drawable.cute_twisthead_animation   // 扭头
        }
    }

    // ─────────────────────────────────────────────────
    //  菜单弹窗
    // ─────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private fun onShowSelectDialog() {
        dismissDialog(menuDialog)
        if (menuDialog == null) {
            menuDialog = FloatingMenuDialog(this).apply {
                setListener(object : FloatingMenuDialog.Listener {
                    override fun onStartTouch(x: Int, y: Int) {
                        // 停止基础动画
                        stopAnimation(basicAnim)

                        targetX = x
                        targetY = y
                        if (functionType != TYPE_LUCKY_BAG) {
                            // 跑到目标点，然后执行动画
                            onStartAnimation(x, y)
                        } else {
                            // 福袋模式：显示卡点时间
                            updateLuckyBagTimeText()
                            // 原地跳绳（不跑到目标点）
                            if (skippingRopeAnim?.isRunning != true) {
                                startSkippingRopeAnimation()
                            }
                        }
                    }

                    override fun onStopTouch() {
                        Log.d(TAG, "onStopTouch, basicRunning=${basicAnim?.isRunning}")
                        if (basicAnim != null && basicAnim?.isRunning != true) {
                            TouchEventManager.setOpenSkippingRope(false)
                            viewChickenImage.setImageDrawable(basicAnim)
                            basicAnim?.start()
                        }
                    }

                    override fun onExitService() {
                        stopSelf()
                    }

                    override fun onAddTouchPoint() {
                        // 启动主 Activity 并导航到添加触点页面
                        val intent = Intent(this@FloatingService, com.wly.beansprout.MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra(com.wly.beansprout.MainActivity.EXTRA_NAVIGATE_TO,
                                com.wly.beansprout.MainActivity.NAV_ADD_TOUCH_POINT)
                        }
                        startActivity(intent)
                    }

                    override fun onEditReplyScript() {
                        // 话术编辑弹窗已在 FloatingMenuDialog 内部直接展示，此处无需额外操作
                    }
                })
            }
        }
        menuDialog?.setFunctionType(functionType, luckybagTime)
        menuDialog?.show()
    }

    /**
     * 更新福袋循环间隔显示
     */
    private fun updateLuckyBagTimeText() {
        txviTime.text = if (luckybagTime > 0) "间隔${luckybagTime}秒" else ""
    }

    // ─────────────────────────────────────────────────
    //  动画系统
    // ─────────────────────────────────────────────────

    /**
     * 动画入口：根据小鸡模型选择动画类型
     */
    private fun onStartAnimation(x: Int, y: Int) {
        // 目标位置下移 30px，避免挡住点击位置
        val adjustedY = y + 30
        if (chickModel == 1) {
            onStartFlashChickenAnimation(x, adjustedY)
        } else {
            onStartStrollingChickenAnimation(x, adjustedY)
        }
    }

    /**
     * 溜达鸡动画：一步步走到目标点，然后跳绳
     */
    private fun onStartStrollingChickenAnimation(targetX: Int, targetY: Int) {
        Log.d(TAG, "strolling target=($targetX, $targetY)")

        // 起始坐标
        val startX = if (currentX == 0 && currentY == 0) floatLayoutParams.x else currentX
        val startY = if (currentX == 0 && currentY == 0) floatLayoutParams.y else currentY

        // 转圈动画（根据方向选择左/右）
        val circleRes = if (startX > targetX) {
            R.drawable.cute_circle_left_animation
        } else {
            R.drawable.cute_circle_right_animation
        }
        val circleAnim = ContextCompat.getDrawable(this, circleRes) as? AnimationDrawable
        viewChickenImage.setImageDrawable(circleAnim)
        circleAnim?.start()

        Log.d(TAG, "strolling start=($startX, $startY)")

        // 路径插值
        val pathFinder = PathFinder(startX, startY, targetX, targetY)
        val pathList = pathFinder.getPath()
        Log.d(TAG, "strolling pathSize=${pathList.size}")

        if (pathList.isNotEmpty()) {
            val moveTask = object : Runnable {
                private var index = 0
                override fun run() {
                    if (index < pathList.size) {
                        val point = pathList[index]
                        floatLayoutParams.x = point.x
                        floatLayoutParams.y = point.y
                        windowManager.updateViewLayout(layoutFloatingView, floatLayoutParams)
                        Log.d(TAG, "moving step=$index (${point.x}, ${point.y})")
                        index++
                        handler.postDelayed(this, 500L)
                    } else {
                        // 到达目标点，开始跳绳
                        startSkippingRopeAnimation()
                    }
                }
            }
            handler.postDelayed(moveTask, 500L)
        }
    }

    /**
     * 闪现鸡动画：收缩 → 闪现到目标 → 冒出 → 工作动画
     */
    private fun onStartFlashChickenAnimation(targetX: Int, targetY: Int) {
        Log.d(TAG, "flash target=($targetX, $targetY)")

        // 收缩动画 (oneshot=true)
        val shrinkAnim = ContextCompat.getDrawable(this, R.drawable.hide_frame_animation)
                as? AnimationDrawable
        viewChickenImage.setImageDrawable(shrinkAnim)
        shrinkAnim?.start()

        // 收缩结束后（~375ms = 15帧 * 25ms）闪现到目标点
        handler.postDelayed({
            shrinkAnim?.stop()

            // 闪现
            floatLayoutParams.x = targetX
            floatLayoutParams.y = targetY
            windowManager.updateViewLayout(layoutFloatingView, floatLayoutParams)

            // 冒出动画 (oneshot=true)
            val extendAnim = ContextCompat.getDrawable(this, R.drawable.show_frame_animation)
                    as? AnimationDrawable
            viewChickenImage.setImageDrawable(extendAnim)
            extendAnim?.start()

            // 冒出结束后（~450ms = 18帧 * 25ms）开始工作动画
            handler.postDelayed({
                TouchEventManager.setOpenSkippingRope(true)
                val workAnim = ContextCompat.getDrawable(this, R.drawable.work_animation)
                        as? AnimationDrawable
                viewChickenImage.setImageDrawable(workAnim)
                viewChickenImage.post { workAnim?.start() }
            }, 450L)

        }, 375L)
    }

    /**
     * 开始跳绳动画（执行触控动作时的持续动画）
     */
    private fun startSkippingRopeAnimation() {
        TouchEventManager.setOpenSkippingRope(true)
        skippingRopeAnim = ContextCompat.getDrawable(this, R.drawable.cute_skipping_rope_animation)
                as? AnimationDrawable
        viewChickenImage.setImageDrawable(skippingRopeAnim)
        skippingRopeAnim?.start()
    }

    // ─────────────────────────────────────────────────
    //  生命周期
    // ─────────────────────────────────────────────────

    override fun onDestroy() {
        super.onDestroy()

        // 移除悬浮窗
        if (layoutFloatingView.isAttachedToWindow) {
            windowManager.removeView(layoutFloatingView)
        }

        // 关闭弹窗
        dismissDialog(menuDialog)

        // 停止所有动画
        stopAnimation(basicAnim)
        basicAnim = null
        stopAnimation(randomAnim)
        randomAnim = null
        stopAnimation(skippingRopeAnim)
        skippingRopeAnim = null

        // 移除 Handler 回调
        handler.removeCallbacksAndMessages(null)
    }

    // ─────────────────────────────────────────────────
    //  工具方法
    // ─────────────────────────────────────────────────

    private fun stopAnimation(anim: AnimationDrawable?) {
        if (anim != null && anim.isRunning) {
            anim.stop()
        }
    }

    private fun dismissDialog(dialog: Dialog?) {
        if (dialog != null && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    companion object {
        private const val TAG = "FloatingService"

        const val EXTRA_FUNCTION_TYPE = "functionType"
        const val EXTRA_CHICK_MODEL = "chickModel"
        const val EXTRA_LUCKYBAG_TIME = "luckybagTime"

        /** 功能类型常量 */
        const val TYPE_OTHER = 0
        const val TYPE_SINGLE_CLICK = 1
        const val TYPE_LIKE = 2
        const val TYPE_SLIDE_DOWN = 3
        const val TYPE_SLIDE_UP = 4
        const val TYPE_SLIDE_LEFT = 5
        const val TYPE_SLIDE_RIGHT = 6
        const val TYPE_AUTO_REPLY = 7
        const val TYPE_LUCKY_BAG = 8
    }
}
