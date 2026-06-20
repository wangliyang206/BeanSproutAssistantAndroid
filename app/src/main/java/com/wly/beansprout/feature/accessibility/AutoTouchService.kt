package com.wly.beansprout.feature.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.wly.beansprout.core.TouchAction
import com.wly.beansprout.core.TouchEventManager
import com.wly.beansprout.core.utils.StringUtils
import com.wly.beansprout.data.model.TouchPoint
import com.wly.beansprout.data.repository.TouchPointRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 无障碍服务 —— 自主操作引擎
 *
 * 通过 AccessibilityService.dispatchGesture() 模拟手势操作：
 * 单击、双击（点赞）、四向滑动、自动回复、抢福袋。
 *
 * 使用 TouchEventManager（StateFlow）替代旧项目的 EventBus 进行事件通信。
 */
class AutoTouchService : AccessibilityService() {

    private val TAG = "AutoTouchService"

    // 手势调度
    private val handler = Handler(Looper.getMainLooper())
    private val delayHandler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ---- 数据源 ----
    private var touchPointRepo: TouchPointRepository? = null
    private var findNodeUtil: FindTargetNodeUtil? = null

    // ---- 当前触点 ----
    private var autoTouchPoint: TouchPoint? = null

    // ---- 抢福袋顺序执行器 ----
    private var luckyBagPoints: List<TouchPoint> = emptyList()
    private var currentLuckyBagIndex: Int = 0

    // ======================== 生命周期 ========================

    override fun onServiceConnected() {
        super.onServiceConnected()
        touchPointRepo = TouchPointRepository(applicationContext)
        findNodeUtil = FindTargetNodeUtil()
        Log.d(TAG, "###无障碍服务已连接")
    }

    override fun onInterrupt() {
        Log.d(TAG, "###无障碍服务中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        delayHandler.removeCallbacksAndMessages(null)
        findNodeUtil?.onDestroy()
        serviceScope.cancel()
        touchPointRepo = null
        Log.d(TAG, "###无障碍服务已销毁")
    }

    // ======================== 事件分发（替代 EventBus） ========================

    /**
     * 外部调用入口：通过此方法向无障碍服务发送操作指令。
     * 可在任意线程调用，内部会切到主线程处理。
     */
    fun handleTouchAction(action: TouchAction, touchPoint: TouchPoint? = null) {
        handler.post {
            Log.d(TAG, "###handleTouchAction: $action, point=$touchPoint")
            TouchEventManager.setTouchAction(action)
            handler.removeCallbacks(autoTouchRunnable)

            when (action) {
                TouchAction.START -> {
                    if (touchPoint != null) {
                        // 常规模式：使用指定触点
                        autoTouchPoint = touchPoint
                        scheduleAutoTouch()
                    } else {
                        // 福袋模式：从仓库加载所有福袋触点，顺序循环执行
                        luckyBagPoints = touchPointRepo?.getTouchPointsByType(TouchPoint.TYPE_LUCKY_BAG) ?: emptyList()
                        currentLuckyBagIndex = 0
                        if (luckyBagPoints.isNotEmpty()) {
                            Log.d(TAG, "###福袋模式启动：共 ${luckyBagPoints.size} 个坐标")
                            scheduleLuckyBagTick()
                        } else {
                            Log.d(TAG, "###福袋模式：无可用触点")
                        }
                    }
                }
                TouchAction.CONTINUE -> {
                    if (autoTouchPoint != null) scheduleAutoTouch()
                    if (luckyBagPoints.isNotEmpty()) scheduleLuckyBagTick()
                }
                TouchAction.PAUSE -> {
                    handler.removeCallbacks(autoTouchRunnable)
                }
                TouchAction.STOP -> {
                    handler.removeCallbacks(autoTouchRunnable)
                    autoTouchPoint = null
                    luckyBagPoints = emptyList()
                    currentLuckyBagIndex = 0
                }
                else -> Unit
            }
        }
    }

    // ======================== 手势构建 ========================

    private val autoTouchRunnable = Runnable {
        val point = autoTouchPoint ?: return@Runnable
        Log.d(TAG, "###autoTouch: x=${point.x} y=${point.y} type=${point.functionType}")

        when (point.functionType) {
            TouchPoint.TYPE_AUTO_REPLY -> {
                Log.d(TAG, "###执行 自动回复 功能")
                val root = rootInActiveWindow ?: return@Runnable
                val pkg = root.packageName?.toString() ?: ""
                if (pkg == TouchEventManager.getTargetPackage()) {
                    // 抖音主版
                    performAutoReply(
                        "com.ss.android.ugc.aweme:id/gl6",
                        "com.ss.android.ugc.aweme:id/0=v"
                    )
                } else {
                    // 极速版
                    performAutoReply(
                        "com.ss.android.ugc.aweme.lite:id/dv_",
                        "m.l.live.plugin:id/tv_send_portrait"
                    )
                }
            }
            else -> {
                val builder = GestureDescription.Builder()
                when (point.functionType) {
                    TouchPoint.TYPE_SINGLE_CLICK -> builder.addStroke(singleClickStroke(point))
                    TouchPoint.TYPE_LIKE -> {
                        builder.addStroke(singleClickStroke(point))
                        builder.addStroke(doubleClickStroke(point))
                    }
                    TouchPoint.TYPE_SLIDE_DOWN -> builder.addStroke(slideStroke(point, 1))
                    TouchPoint.TYPE_SLIDE_UP -> builder.addStroke(slideStroke(point, 2))
                    TouchPoint.TYPE_SLIDE_LEFT -> builder.addStroke(slideStroke(point, 3))
                    TouchPoint.TYPE_SLIDE_RIGHT -> builder.addStroke(slideStroke(point, 4))
                }
                dispatchGesture(
                    builder.build(),
                    object : GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                        }
                        override fun onCancelled(gestureDescription: GestureDescription?) {
                            super.onCancelled(gestureDescription)
                        }
                    },
                    null
                )
            }
        }

        // 调度下一轮
        scheduleAutoTouch()
    }

    private fun scheduleAutoTouch() {
        val point = autoTouchPoint ?: return
        handler.postDelayed(autoTouchRunnable, point.delay.toLong())
    }

    private fun singleClickStroke(point: TouchPoint): GestureDescription.StrokeDescription {
        val path = Path().apply { moveTo(point.x.toFloat(), point.y.toFloat()) }
        return GestureDescription.StrokeDescription(path, 0, 1)
    }

    private fun doubleClickStroke(point: TouchPoint): GestureDescription.StrokeDescription {
        val path = Path().apply { moveTo(point.x.toFloat(), point.y.toFloat()) }
        return GestureDescription.StrokeDescription(path, 100, 1)
    }

    /**
     * 滑动 Stroke
     * @param direction 1=下 2=上 3=左 4=右
     */
    private fun slideStroke(
        point: TouchPoint,
        direction: Int
    ): GestureDescription.StrokeDescription {
        val path = Path().apply {
            moveTo(point.x.toFloat(), point.y.toFloat())
            val dist = 1000f
            when (direction) {
                1 -> lineTo(point.x.toFloat(), point.y - dist)
                2 -> lineTo(point.x.toFloat(), point.y + dist)
                3 -> lineTo(point.x - dist, point.y.toFloat())
                4 -> lineTo(point.x + dist, point.y.toFloat())
            }
        }
        return GestureDescription.StrokeDescription(path, 0, 100)
    }

    // ======================== 无障碍事件监听 ========================

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: ""
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val type = autoTouchPoint?.functionType ?: 0
                onWindowContentChanged(packageName, type)
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> Unit
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> Unit
        }
    }

    /**
     * 窗口内容变化回调 —— 自动暂停/恢复
     * 注：福袋模式（纯坐标执行）不受自动暂停影响，
     * 因为 autoTouchPoint 为 null 时 functionType = 0，不会触发暂停逻辑。
     */
    private fun onWindowContentChanged(packageName: String, functionType: Int) {
        val targetPkg = TouchEventManager.getTargetPackage()

        // 自动暂停/恢复（排除自动回复）
        if (targetPkg.isNotEmpty() && functionType != TouchPoint.TYPE_AUTO_REPLY) {
            if (packageName.contains(targetPkg)) {
                if (TouchEventManager.isPaused()) {
                    Log.d(TAG, "###自动恢复动作")
                    handleTouchAction(TouchAction.CONTINUE)
                }
            } else {
                if (TouchEventManager.isTouching()) {
                    Log.d(TAG, "###自动暂停动作")
                    handleTouchAction(TouchAction.PAUSE)
                }
            }
        }
    }

    // ======================== 自动回复 ========================

    private fun performAutoReply(commentResId: String, sendResId: String) {
        val commentLayout = findNodeById(commentResId) ?: findNodeByText("说点什么...") ?: return
        commentLayout.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        delayHandler.postDelayed({
            val root = rootInActiveWindow ?: return@postDelayed
            // 查找 EditText（根据弹窗状态取不同 index）
            var input = root.getChild(1)
            if (input?.className?.toString() != "android.widget.EditText") {
                input = root.getChild(2)
            }
            if (input == null) return@postDelayed

            // 从脚本库随机选取一条回复
            val script = touchPointRepo?.getAutoReplyScript() ?: return@postDelayed
            val replyText = StringUtils.randomPick(script)
            if (replyText.isBlank()) return@postDelayed

            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, replyText)
            }
            val success = input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            if (success) {
                val sendBtn = findNodeById(sendResId) ?: findNodeByText("发送")
                sendBtn?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }, 1000)
    }

    // ======================== 抢福袋顺序执行器 ========================

    /**
     * 福袋模式 Runnable：按顺序逐个点击所有福袋坐标，完成后循环。
     * 不再依赖无障碍控件检测，纯坐标点击。
     */
    private val luckyBagRunnable = Runnable {
        if (luckyBagPoints.isEmpty()) return@Runnable

        val point = luckyBagPoints[currentLuckyBagIndex]
        Log.d(TAG, "###福袋点击 [${currentLuckyBagIndex + 1}/${luckyBagPoints.size}]: " +
                "${point.name} (${point.x}, ${point.y})")

        dispatchClickAt(point.x, point.y, onCompleted = {
            // 推进到下一个坐标
            currentLuckyBagIndex = (currentLuckyBagIndex + 1) % luckyBagPoints.size
            if (currentLuckyBagIndex == 0) {
                Log.d(TAG, "###福袋一轮完成，开始下一轮循环")
            }
            // 调度下一轮（使用当前点的 delay）
            scheduleLuckyBagTick()
        })
    }

    /** 按当前触点的 delay 调度下一次福袋点击 */
    private fun scheduleLuckyBagTick() {
        if (luckyBagPoints.isEmpty()) return
        val point = luckyBagPoints[currentLuckyBagIndex]
        handler.postDelayed(luckyBagRunnable, point.delay.toLong())
    }

    // ======================== 工具方法 ========================

    /** 精确手势点击 */
    private fun dispatchClickAt(
        x: Int,
        y: Int,
        onCompleted: () -> Unit = {},
        onCancelled: () -> Unit = {}
    ) {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 500L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(
            gesture,
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    onCompleted()
                }
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    onCancelled()
                }
            },
            null
        )
    }

    /** 按 ID 查找节点 */
    private fun findNodeById(viewId: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        return nodes?.firstOrNull()
    }

    /** 按文本查找节点（先找可点击的，再找不可点击的） */
    private fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByText(text)
        if (nodes.isNullOrEmpty()) return null
        return nodes.firstOrNull { it.isClickable } ?: nodes.firstOrNull()
    }

    companion object {
        /** 全局实例引用，供外部（如悬浮窗菜单）直接调用 handleTouchAction */
        var instance: AutoTouchService? = null
            private set
    }

    init {
        instance = this
    }
}
