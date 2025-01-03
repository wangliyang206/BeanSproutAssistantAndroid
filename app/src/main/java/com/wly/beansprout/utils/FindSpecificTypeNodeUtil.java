package com.wly.beansprout.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.wly.beansprout.service.AutoTouchService;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout.utils
 * @ClassName: FindSpecificTypeNodeUtil
 * @Description: 查找特定类型节点
 * @Author: WLY
 * @CreateDate: 2025/1/2 16:39
 */
public class FindSpecificTypeNodeUtil {
    private Context context;
    // 创建一个Handler，它与当前线程（通常是UI线程）的Looper关联
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper());
    private final String TAG = "FindSpecificTypeNodeUtil+++";
    // 结果节点
    private AccessibilityNodeInfo resultNode;
    private FindNodeCallback findNodeCallback;

    public FindSpecificTypeNodeUtil(Context context) {
        this.context = context;
    }

    /**
     * 查找特定类型节点 入口
     *
     * @param className        控件全称，例如：android.widget.Button或com.lynx.tasm.behavior.ui.view.UIView
     * @param tips             内容描述，控件上面的提示词
     * @param luckyBagTime     福袋卡点时间，单位：分钟；如果不设置才赋值为0；
     * @param rootNode         屏幕获取的节点
     * @param findNodeCallback 回调
     */
    public void findNode(String className, String tips, int luckyBagTime, AccessibilityNodeInfo rootNode, FindNodeCallback findNodeCallback) {
        this.findNodeCallback = findNodeCallback;

        // 在子线程中执行
        handler.post(() -> {
//            Log.d(TAG, "-------------------------------【开始】--------------------------------");
            boolean found = findSpecificTypeNode(className, tips, luckyBagTime, rootNode);
            if (!found) {
                if (findNodeCallback != null) {
                    findNodeCallback.onFindNodeResults(null);
                }
            }
//            Log.d(TAG, "-------------------------------【结束】--------------------------------");
        });
    }


    /**
     * 遍历所有控件
     *
     * @param className    控件全称，例如：android.widget.Button或com.lynx.tasm.behavior.ui.view.UIView
     * @param tips         内容描述，控件上面的提示词
     * @param luckyBagTime 福袋卡点时间，单位：分钟；如果不设置才赋值为0；
     * @param rootNode     屏幕获取的节点
     */
    public boolean findSpecificTypeNode(String className, String tips, int luckyBagTime, AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            resultNode = null;
            return false;
        }

        // Check if the current node matches the desired class name
        if (TextUtils.isEmpty(tips)) {
            // 不需要匹配提示词
            if (rootNode.getClassName().equals(className)) {
                resultNode = rootNode;
//                Log.d(TAG, "ClassName1=" + rootNode.getClassName() + "；text=" + rootNode.getText());
                if (findNodeCallback != null) {
                    findNodeCallback.onFindNodeResults(rootNode);
                }
                return true;
            }
        } else {
            // 需要匹配提示词
            if (rootNode.getClassName().equals(className) && rootNode.getContentDescription().toString().contains(tips)) {
//                Log.d(TAG, "ClassName0=" + rootNode.getClassName() + "；text=" + rootNode.getText());
                // 如果是【超级福袋】的话，匹配一下时间
                if (tips.contains("超级福袋")) {
                    // 获取时间，格式：超级福袋 3分56秒 按钮
                    String time = rootNode.getContentDescription().toString();
                    time = time.substring(time.indexOf("袋") + 2, time.lastIndexOf("分"));

                    Log.d(TAG, "福袋时间=" + time + "分钟");
                    if (luckyBagTime == 999) {
                        // 不设置时间，要求立即参与
                        AutoTouchService.isAllowed = true;
                    } else if (luckyBagTime == 998) {
                        // 5~10分钟随机
                        int num = CommonUtils.getRandomNum(5,10);
                        Log.d(TAG, "随机生成时间=" + num + "分钟");
                        AutoTouchService.isAllowed = num >= Integer.parseInt(time);

                    } else if (luckyBagTime == 997) {
                        // 0~5分钟随机
                        int num = CommonUtils.getRandomNum(0,5);
                        Log.d(TAG, "随机生成时间=" + num + "分钟");
                        AutoTouchService.isAllowed = num >= Integer.parseInt(time);
                    } else {
                        // 配置时间小于超级福袋时间，不允许参与
                        AutoTouchService.isAllowed = luckyBagTime >= Integer.parseInt(time);
                    }
                }
                resultNode = rootNode;
                if (findNodeCallback != null) {
                    findNodeCallback.onFindNodeResults(rootNode);
                }
                return true;
            }
        }

        // Recursively check all child nodes
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo child = rootNode.getChild(i);
            if (child != null) {
                if (findSpecificTypeNode(className, tips, luckyBagTime, child)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 遍历所有控件
     */
    public boolean findNode(String className, String tips, AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return false;
        }

        // Check if the current node matches the desired class name
        if (TextUtils.isEmpty(tips)) {
            if (rootNode.getClassName().equals(className)) {
                return true;
            }
        } else {
            if (rootNode.getClassName().equals(className) && rootNode.getContentDescription().toString().contains(tips)) {
                return true;
            }
        }

        // Recursively check all child nodes
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo child = rootNode.getChild(i);
            if (child != null) {
                if (findNode(className, tips, child)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 对外提供结果
     */
    public AccessibilityNodeInfo getResultNode() {
        return resultNode;
    }

    public void onDestroy() {
        this.handler.removeCallbacksAndMessages(null);
        if (findNodeCallback != null) {
            findNodeCallback = null;
        }
        this.resultNode = null;
    }

    public interface FindNodeCallback {
        void onFindNodeResults(AccessibilityNodeInfo nodeInfo);
    }
}

