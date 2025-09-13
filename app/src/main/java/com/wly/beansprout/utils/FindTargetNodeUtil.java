package com.wly.beansprout.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.wly.beansprout.service.AutoTouchService;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout.utils
 * @ClassName: FindSpecificTypeNodeUtil
 * @Description: 查找目标节点
 * @Author: WLY
 * @CreateDate: 2025/1/2 16:39
 */
public class FindTargetNodeUtil {
    private final String TAG = "FindSpecificTypeNodeUtil+++";
    // 句柄
    private Context context;
    // 创建一个Handler，它与当前线程（通常是UI线程）的Looper关联
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper());
    // 回调方式
    private FindNodeCallback findNodeCallback;

    public FindTargetNodeUtil(Context context) {
        this.context = context;
    }

    /**
     * 查找目标节点 入口
     *
     * @param className        控件全称，例如：android.widget.Button或com.lynx.tasm.behavior.ui.view.UIView
     * @param tips             内容描述，控件上面的提示词
     * @param luckyBagTime     福袋卡点时间，单位：分钟；如果不设置赋值为-1或999；
     * @param rootNode         屏幕获取的节点
     * @param findNodeCallback 回调
     */
    public void findNode(String className, String tips, int luckyBagTime, AccessibilityNodeInfo rootNode, FindNodeCallback findNodeCallback) {
        this.findNodeCallback = findNodeCallback;

        // 在子线程中执行
        handler.post(() -> {
//            Log.d(TAG, "-------------------------------【开始】--------------------------------");
            AccessibilityNodeInfo info = findTargetNode(rootNode, className, tips, luckyBagTime);

            if (findNodeCallback != null) {
                findNodeCallback.onFindNodeResults(info);
            }
//            Log.d(TAG, "-------------------------------【结束】--------------------------------");
        });
    }

    /**
     * 遍历节点树寻找目标控件
     *
     * @param rootNode     当前窗口的根节点
     * @param className    控件全称，例如：android.widget.Button或com.lynx.tasm.behavior.ui.view.UIView
     * @param tips         内容描述，控件上面的提示词
     * @param luckyBagTime 福袋卡点时间，单位：分钟；如果不设置赋值为-1或999；
     */
    public AccessibilityNodeInfo findTargetNode(AccessibilityNodeInfo rootNode, String className, String tips, int luckyBagTime) {
        if (rootNode == null) return null;

        // 使用队列实现广度优先搜索
        Queue<AccessibilityNodeInfo> queue = new LinkedList<>();
        queue.add(rootNode);

        while (!queue.isEmpty()) {
            AccessibilityNodeInfo currentNode = queue.poll();

            // 此处添加你的匹配条件（示例：通过ID匹配）
            if (isTargetNode(currentNode, className, tips, luckyBagTime)) {
                // 匹配成功
                return currentNode;
            }

            // 递归添加子节点
            for (int i = 0; i < currentNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = currentNode.getChild(i);
                if (child != null) {
                    queue.add(child);
                }
            }

            // 回收非目标节点（重要！避免内存泄漏）
            if (currentNode != rootNode) {
                currentNode.recycle();
            }
        }
        return null;
    }

    /**
     * 匹配条件
     */
    private boolean isTargetNode(AccessibilityNodeInfo rootNode, String className, String tips, int luckyBagTime) {
        if (TextUtils.isEmpty(tips)) {
            // 不需要匹配提示词
            return rootNode.getClassName().equals(className);

        } else {
            // 需要匹配提示词
            // 由于在停止抢福袋时会报异常，这里为了统一处理，直接采用异常捕获方式

            try {
                if (rootNode.getClassName().equals(className) && rootNode.getContentDescription().toString().contains(tips)) {
                    // 如果是【超级福袋】的话，匹配一下时间
                    if (tips.contains("超级福袋")) {
                        // 获取时间，格式：超级福袋 3分56秒 按钮
                        String time = rootNode.getContentDescription().toString();
                        time = time.substring(time.indexOf("袋") + 2, time.lastIndexOf("分"));

                        Log.d(TAG, "###福袋时间=" + time + "分钟");
                        if (luckyBagTime == -1 || luckyBagTime == 999) {
                            // 不设置时间，要求立即参与
                            AutoTouchService.isAllowed = true;
                        } else {
                            // 配置时间小于超级福袋时间，不允许参与
                            AutoTouchService.isAllowed = luckyBagTime >= Integer.parseInt(time);
                        }
                    } else if (tips.contains("生活服务-直播-福袋")) {
                        AutoTouchService.isAllowed = true;
                    }

                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    public void onDestroy() {
        this.handler.removeCallbacksAndMessages(null);
        if (findNodeCallback != null) {
            findNodeCallback = null;
        }
    }

    public interface FindNodeCallback {
        void onFindNodeResults(AccessibilityNodeInfo nodeInfo);
    }
}

