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
 * 抢福袋功能使用坐标循环 + 无障碍辅助检测：
 * - 用户录制一组有序坐标（图标→动作按钮→关闭按钮）
 * - 服务按序循环点击，适配不同设备和网络速度
 * - 无障碍服务仅在每轮后检测结果弹窗（未中奖/中奖）
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

    // ======================== 抢福袋坐标循环 ========================

    /**
     * 福袋录制的触点坐标列表
     *
     * 索引约定：
     * - [0]: 福袋浮标图标位置（打开面板）
     * - [1]: 动作按钮位置（"一键发表评论"等）
     * - [2]: 关闭/确认按钮位置（"我知道了"等）
     */
    private var luckyBagRecordedPoints: List<TouchPoint> = emptyList()
    private var luckyBagCycleActive = false  // 坐标循环是否正在运行
    private var luckyBagCycleComplete = false  // 当前轮次所有坐标是否已点完（仅此时才检测结果）
    private var luckyBagCurrentIndex = 0  // 当前执行到第几个坐标
    private var lastResultCheckTime = 0L  // 结果检测节流时间戳

    // 坐标循环时间参数
    private val LUCKY_BAG_INTERVAL_MS = 5000L    // 每次坐标点击之间的固定间隔（5秒，适配大多数设备）
    private val CYCLE_START_DELAY_MS = 10_000L   // 首次启动延迟（用户切回抖音的时间）
    private val CYCLE_RESET_DELAY_MS = 10_000L   // 每轮结束后等待（给开奖倒计时留时间）
    private val RESULT_CHECK_THROTTLE_MS = 1000L  // 结果弹窗检测节流（避免频繁扫描）

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
                        // 福袋模式：启动坐标循环自动抢夺
                        startLuckyBagAutoGrab()
                    }
                }
                TouchAction.CONTINUE -> {
                    if (autoTouchPoint != null) scheduleAutoTouch()
                    if (luckyBagCycleActive) {
                        Log.d(TAG, "###福袋坐标循环恢复: index=$luckyBagCurrentIndex")
                    }
                }
                TouchAction.PAUSE -> {
                    handler.removeCallbacks(autoTouchRunnable)
                }
                TouchAction.STOP -> {
                    handler.removeCallbacks(autoTouchRunnable)
                    autoTouchPoint = null
                    stopLuckyBagAutoGrab()
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

                // 福袋坐标循环：在内容变化时辅助检测结果弹窗
                if (luckyBagCycleActive
                    && packageName.contains(TouchEventManager.getTargetPackage())
                ) {
                    handleLuckyBagResultCheck()
                }
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // 窗口状态变化（弹窗出现/消失）也辅助检测结果弹窗
                if (luckyBagCycleActive
                    && packageName.contains(TouchEventManager.getTargetPackage())
                ) {
                    handleLuckyBagResultCheck()
                }
            }
        }
    }

    /**
     * 窗口内容变化回调 —— 常规模式自动暂停/恢复
     */
    private fun onWindowContentChanged(packageName: String, functionType: Int) {
        val targetPkg = TouchEventManager.getTargetPackage()

        // 自动暂停/恢复（排除自动回复和福袋模式）
        if (targetPkg.isNotEmpty()
            && functionType != TouchPoint.TYPE_AUTO_REPLY
            && !luckyBagCycleActive
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

    // ======================== 抢福袋：坐标循环自动抢夺 ========================

    /**
     * 启动福袋坐标循环自动抢夺
     *
     * 加载用户录制的触控点坐标，按顺序循环点击。
     * 每次点击之间等待固定间隔（[LUCKY_BAG_INTERVAL_MS]），
     * 以适配不同设备和网络条件下的 UI 响应时间。
     *
     * 无障碍服务作为辅助：仅在每轮坐标全部点完后检测结果弹窗，
     * 发现"未中奖"则自动关闭，发现"中奖"则暂停等待用户手动领奖。
     */
    private fun startLuckyBagAutoGrab() {
        luckyBagRecordedPoints = touchPointRepo?.getTouchPointsByType(TouchPoint.TYPE_LUCKY_BAG)
            ?: emptyList()

        if (luckyBagRecordedPoints.isEmpty()) {
            Log.w(TAG, "###福袋坐标循环启动失败：无录制坐标")
            return
        }

        luckyBagCycleActive = true
        luckyBagCycleComplete = false
        luckyBagCurrentIndex = 0
        lastResultCheckTime = 0L

        Log.d(TAG, "###福袋坐标循环启动：坐标=${luckyBagRecordedPoints.size}个, " +
                "间隔=${LUCKY_BAG_INTERVAL_MS}ms, " +
                "启动延迟=${CYCLE_START_DELAY_MS}ms")

        // 启动延迟：给用户时间切回抖音、等待 UI 就绪
        handler.postDelayed({
            if (luckyBagCycleActive) {
                scheduleNextLuckyBagPoint()
            }
        }, CYCLE_START_DELAY_MS)
    }

    /**
     * 停止福袋坐标循环
     */
    private fun stopLuckyBagAutoGrab() {
        luckyBagCycleActive = false
        luckyBagCycleComplete = false
        luckyBagCurrentIndex = 0
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "###福袋坐标循环已停止")
    }

    /**
     * 调度下一个坐标点击
     *
     * 循环流程：
     * 1. 点击当前索引的坐标
     * 2. 等待固定间隔 [LUCKY_BAG_INTERVAL_MS]（给 UI 响应时间，适配不同设备/网速）
     * 3. 前进到下一个坐标
     * 4. 所有坐标点完后，标记 cycleComplete=true，等待 [CYCLE_RESET_DELAY_MS]
     *    （此期间无障碍事件可触发结果检测），然后重置并进入下一轮
     *
     * 重要：结果检测只在 cycleComplete=true 时生效，
     * 避免点击过程中的误检（如弹幕中的"恭喜"被误判为中奖）。
     */
    private fun scheduleNextLuckyBagPoint() {
        if (!luckyBagCycleActive) return
        if (luckyBagRecordedPoints.isEmpty()) return

        val point = luckyBagRecordedPoints[luckyBagCurrentIndex]
        Log.d(TAG, "###坐标循环: 点击[${luckyBagCurrentIndex}] ${point.name} (${point.x},${point.y})")
        dispatchQuickClick(point.x, point.y)

        // 前进到下一个坐标
        luckyBagCurrentIndex++

        if (luckyBagCurrentIndex >= luckyBagRecordedPoints.size) {
            // 所有坐标点已执行完毕，标记可以检测结果弹窗
            luckyBagCycleComplete = true
            Log.d(TAG, "###一轮坐标循环完成(${luckyBagRecordedPoints.size}个点)，" +
                    "已开启结果检测，等待${CYCLE_RESET_DELAY_MS}ms后进入下一轮")

            // 立即检测一次（结果弹窗可能已经出现）
            checkAndHandleResult()

            // 重置索引，等待后开始下一轮
            luckyBagCurrentIndex = 0
            handler.postDelayed({
                if (luckyBagCycleActive) {
                    luckyBagCycleComplete = false  // 新一轮开始，关闭结果检测
                    scheduleNextLuckyBagPoint()
                }
            }, CYCLE_RESET_DELAY_MS)
        } else {
            // 还有更多坐标点，等待固定间隔后点击下一个
            handler.postDelayed({
                if (luckyBagCycleActive) {
                    scheduleNextLuckyBagPoint()
                }
            }, LUCKY_BAG_INTERVAL_MS)
        }
    }

    /**
     * 无障碍辅助：在窗口事件触发时检测结果弹窗
     *
     * 带节流机制（[RESULT_CHECK_THROTTLE_MS]），避免频繁扫描无障碍树。
     *
     * 重要：只在 [luckyBagCycleComplete]=true 时执行检测。
     * 这避免了点击过程中直播间弹幕、礼物动画等包含"恭喜"文字的元素
     * 被误判为中奖，导致循环意外停止。
     */
    private fun handleLuckyBagResultCheck() {
        if (!luckyBagCycleComplete) return  // 坐标还没点完，不检测结果
        val now = System.currentTimeMillis()
        if (now - lastResultCheckTime < RESULT_CHECK_THROTTLE_MS) return
        lastResultCheckTime = now
        checkAndHandleResult()
    }

    /**
     * 检测结果弹窗并处理
     *
     * - 未中奖 → 自动关闭弹窗（"我知道了" / 坐标兜底 / 返回键）
     * - 中奖 → 暂停循环，等用户手动领奖
     */
    private fun checkAndHandleResult() {
        if (!luckyBagCycleActive) return

        val result = detectResultType()
        when (result) {
            ResultType.NOT_WON -> {
                Log.d(TAG, "###检测到未中奖，自动关闭弹窗")
                closeNotWonDialog()
            }
            ResultType.WON -> {
                Log.d(TAG, "###检测到中奖！暂停循环，请手动填写地址领奖")
                luckyBagCycleActive = false
            }
            ResultType.NONE -> { /* 无结果弹窗，继续循环 */ }
        }
    }

    // ======================== 福袋辅助方法 ========================

    /** 结果类型 */
    private enum class ResultType { NONE, NOT_WON, WON }

    /**
     * 检测结果弹窗类型
     *
     * 使用精确关键词避免误检：
     * - 中奖检测用 "抽中"（仅福袋中奖界面出现，弹幕/礼物不会有此词）
     * - 不用 "恭喜"（太宽泛，直播间弹幕和礼物动画常见，会导致误判）
     */
    private fun detectResultType(): ResultType {
        val root = rootInActiveWindow ?: return ResultType.NONE

        // 通过 contentDescription 检测未中奖
        val notWon = findNodeByContentDesc(root, "未中奖")
        if (notWon != null) {
            notWon.recycle()
            root.recycle()
            return ResultType.NOT_WON
        }
        // 通过 contentDescription 检测中奖（用 "抽中" 而非 "恭喜"，避免误检）
        val won = findNodeByContentDesc(root, "抽中")
        if (won != null) {
            won.recycle()
            root.recycle()
            return ResultType.WON
        }

        // 通过文本检测
        val notWonText = findNodeByText("未中奖")
        if (notWonText != null) {
            notWonText.recycle()
            root.recycle()
            return ResultType.NOT_WON
        }
        val wonText = findNodeByText("恭喜抽中福袋")
            ?: findNodeByText("抽中")
        if (wonText != null) {
            wonText.recycle()
            root.recycle()
            return ResultType.WON
        }

        root.recycle()
        return ResultType.NONE
    }

    /**
     * 关闭"未中奖"结果弹窗
     *
     * 策略1：点击 "我知道了" 按钮（文本匹配）
     * 策略2：查找关闭按钮（content-desc = "关闭"）
     * 策略3：使用录制坐标[2]（关闭/确认按钮位置）
     * 策略4：模拟返回键
     */
    private fun closeNotWonDialog() {
        val root = rootInActiveWindow

        // 策略1：点击 "我知道了"
        val confirmNode = findNodeByText("我知道了")
        if (confirmNode != null) {
            val rect = Rect()
            confirmNode.getBoundsInScreen(rect)
            Log.d(TAG, "###点击 '我知道了': (${rect.centerX()},${rect.centerY()})")
            dispatchQuickClick(rect.centerX(), rect.centerY())
            confirmNode.recycle()
            root?.recycle()
            return
        }

        // 策略2：查找关闭按钮（中奖弹窗有 content-desc="关闭" 的节点）
        val closeNode = root?.let { findNodeByContentDesc(it, "关闭") }
        if (closeNode != null) {
            val rect = Rect()
            closeNode.getBoundsInScreen(rect)
            Log.d(TAG, "###点击 '关闭': (${rect.centerX()},${rect.centerY()})")
            dispatchQuickClick(rect.centerX(), rect.centerY())
            closeNode.recycle()
            root?.recycle()
            return
        }

        // 策略3：使用录制坐标[2]（关闭/确认按钮位置）
        if (luckyBagRecordedPoints.size > 2) {
            Log.d(TAG, "###使用录制坐标[2]关闭弹窗")
            clickRecordedPointSafely(2)
            root?.recycle()
            return
        }

        // 策略4：模拟返回键
        Log.d(TAG, "###模拟返回键关闭弹窗")
        performGlobalAction(GLOBAL_ACTION_BACK)
        root?.recycle()
    }

    /**
     * BFS 遍历查找 contentDescription 包含指定关键词的节点
     *
     * 重要：此方法 **不会** 回收 rootNode，调用者需自行回收。
     * 方法内部会回收遍历过程中创建的子节点。
     * 返回的匹配节点由调用者负责回收。
     */
    private fun findNodeByContentDesc(
        rootNode: AccessibilityNodeInfo,
        keyword: String
    ): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(rootNode)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()

            val desc = node.contentDescription?.toString()
            if (desc != null && desc.contains(keyword)) {
                // 找到匹配：回收队列中剩余的子节点（不包括 rootNode）
                queue.forEach { if (it !== rootNode) it.recycle() }
                return node
            }

            for (i in 0 until node.childCount) {
                val child = try { node.getChild(i) } catch (_: Exception) { null }
                if (child != null) queue.add(child)
            }

            // 回收遍历过的非根子节点
            if (node !== rootNode) {
                node.recycle()
            }
        }
        return null
    }

    /**
     * 安全地点击录制的坐标点
     * @param index 在 luckyBagRecordedPoints 中的索引
     */
    private fun clickRecordedPointSafely(index: Int) {
        if (index < luckyBagRecordedPoints.size) {
            val p = luckyBagRecordedPoints[index]
            Log.d(TAG, "###录制坐标点击 [${index}]: ${p.name} (${p.x},${p.y})")
            dispatchQuickClick(p.x, p.y)
        }
    }

    /**
     * 快速手势点击（50ms，用于福袋抢夺场景）
     */
    private fun dispatchQuickClick(x: Int, y: Int) {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 50L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    // ======================== 工具方法 ========================

    /** 精确手势点击（500ms长按，用于常规坐标点击） */
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
