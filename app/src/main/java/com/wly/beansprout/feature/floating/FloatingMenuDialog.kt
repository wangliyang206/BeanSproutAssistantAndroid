package com.wly.beansprout.feature.floating

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wly.beansprout.R
import com.wly.beansprout.core.TouchAction
import com.wly.beansprout.core.TouchEventManager
import com.wly.beansprout.core.utils.DensityUtil
import com.wly.beansprout.data.model.TouchPoint
import com.wly.beansprout.data.repository.TouchPointRepository
import com.wly.beansprout.feature.accessibility.AutoTouchService

/**
 * 悬浮窗菜单弹窗
 *
 * 从 FloatingService 弹出，提供触控点的管理与操作入口：
 * - 添加触控点
 * - 停止触控
 * - 回复话术（自动回复模式下）
 * - 退出助手
 *
 * 使用传统 Dialog 而非 Compose，因为从 Service 上下文弹出。
 */
class FloatingMenuDialog(context: Context) : Dialog(context) {

    private lateinit var btAdd: Button
    private lateinit var btStop: Button
    private lateinit var btReply: Button
    private lateinit var btExit: Button
    private lateinit var recyclerView: RecyclerView

    private val touchPointAdapter = TouchPointAdapter()
    private val repository = TouchPointRepository(context)

    private var functionType: Int = 0
    private var luckybagTime: Int = 0
    private var listener: Listener? = null

    /** 防止快速重复点击 */
    private var lastClickTime: Long = 0L

    interface Listener {
        /** 开始触控动作 */
        fun onStartTouch(x: Int, y: Int)
        /** 停止触控 */
        fun onStopTouch()
        /** 退出助手 */
        fun onExitService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_menu)

        // 设置弹窗尺寸
        window?.setLayout(DensityUtil.dip2px(context, 350f), WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        setCanceledOnTouchOutside(true)

        // 初始化控件
        btAdd = findViewById(R.id.bt_add)
        btStop = findViewById(R.id.bt_stop)
        btReply = findViewById(R.id.bt_reply)
        btExit = findViewById(R.id.bt_exit)
        recyclerView = findViewById(R.id.rv)

        // 设置 RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = touchPointAdapter

        // 设置 item 点击监听
        touchPointAdapter.setOnItemClickListener { view, position, touchPoint ->
            if (isDoubleClick()) return@setOnItemClickListener

            when (view.id) {
                R.id.item_touch_point -> {
                    // 开始触控动作
                    btReply.visibility = View.GONE
                    btStop.visibility = View.VISIBLE
                    dismiss()

                    // 更新触点数据
                    val points = touchPointAdapter.getTouchPointList()
                    // 重置所有触点的运行状态
                    val updatedPoints = points.mapIndexed { index, point ->
                        point.copy(
                            isStartClick = (index == position),
                            functionType = if (index == position) functionType else point.functionType,
                            luckyBagTime = if (index == position) luckybagTime else point.luckyBagTime
                        )
                    }
                    touchPointAdapter.setTouchPointList(updatedPoints)
                    repository.saveTouchPoints(updatedPoints)

                    // 选中触点
                    val selected = updatedPoints[position]

                    // 通知无障碍服务开启触控
                    AutoTouchService.instance?.handleTouchAction(TouchAction.START, selected)

                    // 通知悬浮窗开始动画
                    listener?.onStartTouch(selected.x, selected.y)
                }

                R.id.bt_delete -> {
                    // 删除触点
                    touchPointAdapter.onRemove(position)
                    repository.saveTouchPoints(touchPointAdapter.getTouchPointList())
                }
            }
        }

        // 按钮点击
        btAdd.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            // 添加触点 → 跳转到添加触点页面（通过悬浮窗回调）
            // 由于 Service 无法直接启动 Activity 导航，这里暂时通过 Toast 提示
            // 实际使用应在主 app 中添加触点
            dismiss()
        }

        btStop.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            // 停止触控
            btReply.visibility = if (functionType == TYPE_AUTO_REPLY) View.VISIBLE else View.GONE
            btStop.visibility = View.GONE

            // 通知无障碍服务停止触控
            AutoTouchService.instance?.handleTouchAction(TouchAction.STOP)

            // 重置所有触点运行状态
            val updatedPoints = touchPointAdapter.getTouchPointList().map { it.copy(isStartClick = false) }
            touchPointAdapter.setTouchPointList(updatedPoints)
            repository.saveTouchPoints(updatedPoints)

            listener?.onStopTouch()
        }

        btReply.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            // 话术功能 — 在主 app 的触点管理页面中处理
            dismiss()
        }

        btExit.setOnClickListener {
            if (isDoubleClick()) return@setOnClickListener
            // 退出助手
            AutoTouchService.instance?.handleTouchAction(TouchAction.STOP)
            listener?.onExitService()
        }

        // 弹窗关闭时：如果没有专属包名且之前暂停，则继续触控
        setOnDismissListener {
            if (TouchEventManager.appPackageName.value.isBlank() && TouchEventManager.isPaused()) {
                AutoTouchService.instance?.handleTouchAction(TouchAction.CONTINUE)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // 打开菜单时暂停触控（如果没有专属包名）
        if (TouchEventManager.appPackageName.value.isBlank()) {
            AutoTouchService.instance?.handleTouchAction(TouchAction.PAUSE)
        }
        // 加载触点列表
        val points = repository.getTouchPoints()
        touchPointAdapter.setTouchPointList(points)
    }

    /**
     * 设置功能类型和福袋时间
     */
    fun setFunctionType(functionType: Int, luckybagTime: Int) {
        this.functionType = functionType
        this.luckybagTime = luckybagTime

        if (::btReply.isInitialized) {
            if (TouchEventManager.isTouching()) {
                btReply.visibility = View.GONE
            } else {
                btReply.visibility = if (functionType == TYPE_AUTO_REPLY) View.VISIBLE else View.GONE
            }
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    private fun isDoubleClick(): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - lastClickTime
        return if (diff in 1..800L) {
            true
        } else {
            lastClickTime = now
            false
        }
    }

    companion object {
        private const val TYPE_AUTO_REPLY = 7
    }
}
