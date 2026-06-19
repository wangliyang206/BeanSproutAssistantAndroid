package com.wly.beansprout.feature.accessibility

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.wly.beansprout.core.TouchEventManager
import java.util.LinkedList

/**
 * 无障碍节点查找工具
 * 使用 BFS（广度优先搜索）遍历 AccessibilityNodeInfo 树，
 * 按类名和内容描述（contentDescription）匹配目标 UI 控件。
 * 主要用于在抖音直播中查找福袋相关控件。
 */
class FindTargetNodeUtil {

    private val handler = Handler(Looper.getMainLooper())

    private companion object {
        const val TAG = "FindTargetNodeUtil"
    }

    /**
     * 异步查找目标节点
     * @param className 控件类名（如 com.lynx.tasm.behavior.ui.LynxFlattenUI）
     * @param tips 内容描述关键词
     * @param luckyBagTime 福袋卡点时间（分钟），-1/999 表示不限
     * @param rootNode 当前屏幕根节点
     * @param callback 结果回调，参数为找到的节点或 null
     */
    fun findNode(
        className: String,
        tips: String,
        luckyBagTime: Int,
        rootNode: AccessibilityNodeInfo?,
        callback: (AccessibilityNodeInfo?) -> Unit
    ) {
        handler.post {
            val result = findTargetNode(rootNode, className, tips, luckyBagTime)
            callback(result)
        }
    }

    /**
     * 同步 BFS 遍历节点树查找目标控件
     */
    fun findTargetNode(
        rootNode: AccessibilityNodeInfo?,
        className: String,
        tips: String,
        luckyBagTime: Int
    ): AccessibilityNodeInfo? {
        if (rootNode == null) return null

        val queue: LinkedList<AccessibilityNodeInfo> = LinkedList()
        queue.add(rootNode)

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: continue

            if (isTargetNode(current, className, tips, luckyBagTime)) {
                return current
            }

            for (i in 0 until current.childCount) {
                val child = current.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }

            // 回收非根节点，避免内存泄漏
            if (current !== rootNode) {
                current.recycle()
            }
        }
        return null
    }

    /**
     * 匹配条件判断
     */
    private fun isTargetNode(
        node: AccessibilityNodeInfo,
        className: String,
        tips: String,
        luckyBagTime: Int
    ): Boolean {
        if (tips.isEmpty()) {
            return node.className?.toString() == className
        }

        try {
            val nodeClassName = node.className?.toString() ?: return false
            val contentDesc = node.contentDescription?.toString() ?: return false

            if (nodeClassName == className && contentDesc.contains(tips)) {
                // 超级福袋 —— 解析时间判断是否参与
                if (tips.contains("超级福袋")) {
                    val timeStr = contentDesc.substringAfter("袋").trimStart().substringBefore("分")
                    Log.d(TAG, "###福袋时间=${timeStr}分钟")
                    val time = timeStr.toIntOrNull() ?: 0
                    TouchEventManager.isLuckyBagAllowed = when {
                        luckyBagTime == -1 || luckyBagTime == 999 -> true
                        else -> luckyBagTime >= time
                    }
                } else if (tips.contains("生活服务-直播-福袋")) {
                    TouchEventManager.isLuckyBagAllowed = true
                }
                return true
            }
        } catch (_: Exception) {
            // 节点可能已被回收，忽略异常
        }
        return false
    }

    fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
    }
}
