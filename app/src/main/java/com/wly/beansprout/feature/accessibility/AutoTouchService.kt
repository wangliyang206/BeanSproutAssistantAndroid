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

    // ---- 抢福袋状态机 ----
    private var luckyBagStep = 0       // 0=空闲 1=查找控件 2=福袋界面 3=已参与 4=已观看直播
    private var luckyBagX = 0
    private var luckyBagY = 0
    private var luckyBagTime = -1

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
                    autoTouchPoint = touchPoint
                    resolveLuckyBagTime()
                    scheduleAutoTouch()
                }
                TouchAction.CONTINUE -> {
                    if (autoTouchPoint != null) scheduleAutoTouch()
                }
                TouchAction.PAUSE -> {
                    handler.removeCallbacks(autoTouchRunnable)
                }
                TouchAction.STOP -> {
                    luckyBagStep = 0
                    handler.removeCallbacks(autoTouchRunnable)
                    autoTouchPoint = null
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
            TouchPoint.TYPE_LUCKY_BAG -> {
                if (luckyBagStep == 0) {
                    Log.d(TAG, "###执行 抢福袋 功能")
                    grabLuckyBag()
                }
            }
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
     * 窗口内容变化回调 —— 自动暂停/恢复 + 福袋结果监听
     */
    private fun onWindowContentChanged(packageName: String, functionType: Int) {
        val targetPkg = TouchEventManager.getTargetPackage()

        // 自动暂停/恢复（排除自动回复和抢福袋）
        if (targetPkg.isNotEmpty() && functionType != TouchPoint.TYPE_AUTO_REPLY
            && functionType != TouchPoint.TYPE_LUCKY_BAG
        ) {
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

        // 福袋相关：检测到"没有抽中福袋"界面时自动关闭
        if (packageName.contains(targetPkg)
            && functionType == TouchPoint.TYPE_LUCKY_BAG
            && luckyBagStep >= 2
        ) {
            serviceScope.launch(Dispatchers.Default) {
                val foundNode = findNodeUtil?.findTargetNode(
                    rootInActiveWindow, TG_CLASS_PATH, "我知道了", -1
                )
                if (foundNode != null) {
                    Log.d(TAG, "###界面监听到【没有抽中福袋】")
                    foundNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                    val bounds = Rect()
                    foundNode.getBoundsInScreen(bounds)
                    dispatchClickAt(bounds.centerX(), bounds.centerY(), onCompleted = {
                        Log.d(TAG, "###已点击 我知道了")
                        resolveLuckyBagTime()
                        luckyBagStep = 0
                        TouchEventManager.isLuckyBagAllowed = true
                    })
                    foundNode.recycle()
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

    // ======================== 抢福袋状态机 ========================

    private fun grabLuckyBag() {
        luckyBagStep = 1
        findNodeUtil?.findNode(CJ_CLASS_PATH, "超级福袋", luckyBagTime, rootInActiveWindow) { luckyBagNode ->
            if (luckyBagNode != null) {
                Log.d(TAG, "###检测到 超级福袋")
                if (!TouchEventManager.isLuckyBagAllowed) {
                    Log.d(TAG, "###福袋卡点时间未到：${luckyBagTime}分钟")
                    luckyBagStep = 0
                    return@findNode
                }

                // 记录坐标并点击
                val rect = Rect()
                luckyBagNode.getBoundsInScreen(rect)
                luckyBagX = rect.left
                luckyBagY = rect.top
                luckyBagNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                dispatchClickAt(luckyBagX, luckyBagY, onCompleted = {
                    luckyBagStep = 2
                    Log.d(TAG, "###已点击 超级福袋")

                    // 3 秒后查找参与按钮
                    delayHandler.postDelayed({
                        handleSuperLuckyBagParticipation()
                    }, 3000)
                })
            } else {
                // 没找到超级福袋，尝试找团购福袋
                findNodeUtil?.findNode("android.widget.Button", "生活服务-直播-福袋", -1, rootInActiveWindow) { tgNode ->
                    if (tgNode != null) {
                        handleTeamBuyLuckyBag(tgNode)
                    } else {
                        Log.d(TAG, "###没有检测到左上角福袋")
                        luckyBagStep = 0
                    }
                }
            }
        }
    }

    /** 超级福袋 —— 参与流程 */
    private fun handleSuperLuckyBagParticipation() {
        findNodeUtil?.findNode(TG_CLASS_PATH, "一键发表评论", -1, rootInActiveWindow) { commentNode ->
            val point = autoTouchPoint ?: return@findNode
            if (commentNode != null) {
                dispatchClickAt(point.x, point.y, onCompleted = {
                    Log.d(TAG, "###已点击 一键发表评论")
                    luckyBagStep = 3
                    delayHandler.postDelayed({ findAndStartWatchTask() }, 2000)
                })
            } else {
                // 查找 "参与抽奖"
                findNodeUtil?.findNode(TG_CLASS_PATH, "参与抽奖", -1, rootInActiveWindow) { drawNode ->
                    if (drawNode != null) {
                        dispatchClickAt(point.x, point.y, onCompleted = {
                            Log.d(TAG, "###已点击 参与抽奖")
                            luckyBagStep = 3
                            delayHandler.postDelayed({ findAndStartWatchTask() }, 2000)
                        })
                    } else {
                        // 直接查找 "开始观看直播任务"
                        findNodeUtil?.findNode(TG_CLASS_PATH, "开始观看直播任务", -1, rootInActiveWindow) { watchNode ->
                            if (watchNode != null) {
                                startWatchTask()
                            } else {
                                closeLuckyBagDialog()
                            }
                        }
                    }
                }
            }
        }
    }

    /** 团购福袋 —— 参与流程 */
    private fun handleTeamBuyLuckyBag(tgNode: AccessibilityNodeInfo) {
        Log.d(TAG, "###检测到 团购福袋")
        if (!TouchEventManager.isLuckyBagAllowed) {
            Log.d(TAG, "###福袋卡点时间未到：${luckyBagTime}分钟")
            luckyBagStep = 0
            return
        }

        val rect = Rect()
        tgNode.getBoundsInScreen(rect)
        luckyBagX = rect.left
        luckyBagY = rect.top
        tgNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        val point = autoTouchPoint ?: return

        dispatchClickAt(luckyBagX, luckyBagY, onCompleted = {
            luckyBagStep = 2
            Log.d(TAG, "###已点击 团购福袋")

            delayHandler.postDelayed({
                // 查找 "发送评论 参与抽奖"
                findNodeUtil?.findNode(TG_CLASS_PATH, "发送评论 参与抽奖", -1, rootInActiveWindow) { buyInfoNode ->
                    if (buyInfoNode != null) {
                        dispatchClickAt(point.x, point.y, onCompleted = {
                            Log.d(TAG, "###已点击 发送评论 参与抽奖")
                            luckyBagStep = 3
                        })
                    } else {
                        // 查找 "发送评论"
                        findNodeUtil?.findNode(TG_CLASS_PATH, "发送评论", -1, rootInActiveWindow) { commentNode ->
                            if (commentNode != null) {
                                dispatchClickAt(point.x, point.y, onCompleted = {
                                    luckyBagStep = 3
                                    delayHandler.postDelayed({
                                        findNodeUtil?.findNode(TG_CLASS_PATH, "开始观看直播任务 参与抽奖", -1, rootInActiveWindow) { watchNode ->
                                            if (watchNode != null) {
                                                dispatchClickAt(point.x, point.y, onCompleted = {
                                                    luckyBagStep = 4
                                                    closeLuckyBagDialog()
                                                })
                                            } else {
                                                closeLuckyBagDialog()
                                            }
                                        }
                                    }, 2000)
                                })
                            } else {
                                closeLuckyBagDialog()
                            }
                        }
                    }
                }
            }, 3000)
        })
    }

    /** 查找并点击 "开始观看直播任务" */
    private fun findAndStartWatchTask() {
        findNodeUtil?.findNode(TG_CLASS_PATH, "开始观看直播任务", -1, rootInActiveWindow) { node ->
            if (node != null) {
                startWatchTask()
            } else {
                closeLuckyBagDialog()
            }
        }
    }

    /** 点击 "开始观看直播任务" 后关闭弹窗 */
    private fun startWatchTask() {
        val point = autoTouchPoint ?: return
        dispatchClickAt(point.x, point.y, onCompleted = {
            Log.d(TAG, "###已点击 开始观看直播任务")
            luckyBagStep = 4
            closeLuckyBagDialog()
        })
    }

    /** 关闭福袋弹窗（点击空白区域） */
    private fun closeLuckyBagDialog() {
        delayHandler.postDelayed({
            dispatchClickAt(luckyBagX, luckyBagY, onCompleted = {
                Log.d(TAG, "###已点击空白 关闭弹窗")
            })
        }, 2000)
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

    /** 解析抢福袋时间（处理随机值） */
    private fun resolveLuckyBagTime() {
        val point = autoTouchPoint ?: run { luckyBagTime = -1; return }
        luckyBagTime = when (point.luckyBagTime) {
            998 -> (5..10).random()       // 5~10 分钟随机
            997 -> (0..5).random()        // 0~5 分钟随机
            else -> point.luckyBagTime
        }
        if (point.luckyBagTime == 998 || point.luckyBagTime == 997) {
            Log.d(TAG, "###随机生成福袋时间=${luckyBagTime}分钟")
        }
    }

    companion object {
        // 抖音福袋控件路径
        private const val TG_CLASS_PATH = "com.lynx.tasm.behavior.ui.view.UIView"
        private const val CJ_CLASS_PATH = "com.lynx.tasm.behavior.ui.LynxFlattenUI"

        /** 全局实例引用，供外部（如悬浮窗菜单）直接调用 handleTouchAction */
        var instance: AutoTouchService? = null
            private set
    }

    init {
        instance = this
    }
}
