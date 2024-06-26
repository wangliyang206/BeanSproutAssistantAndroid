package com.wly.beansprout.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import com.wly.beansprout.TouchEventManager;
import com.wly.beansprout.bean.TouchEvent;
import com.wly.beansprout.bean.TouchPoint;
import com.wly.beansprout.utils.WindowUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 无障碍服务-自动点赞
 *
 * @date 2024/4/26 16:23
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class AutoTouchService extends AccessibilityService {

    private final String TAG = "AutoTouchService+++";
    //自动点击事件
    private TouchPoint autoTouchPoint;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windowManager;
    //倒计时
    private float countDownTime;
    private final DecimalFormat floatDf = new DecimalFormat("#0.0");

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        handler = new Handler();
        EventBus.getDefault().register(this);
        windowManager = WindowUtils.getWindowManager(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReciverTouchEvent(TouchEvent event) {
        Log.d(TAG, "onReciverTouchEvent: " + event.toString());
        TouchEventManager.getInstance().setTouchAction(event.getAction());
        handler.removeCallbacks(autoTouchRunnable);
        switch (event.getAction()) {
            case TouchEvent.ACTION_START:
                autoTouchPoint = event.getTouchPoint();
                onAutoClick();
                break;
            case TouchEvent.ACTION_CONTINUE:
                if (autoTouchPoint != null) {
                    onAutoClick();
                }
                break;
            case TouchEvent.ACTION_PAUSE:
                handler.removeCallbacks(autoTouchRunnable);
                break;
            case TouchEvent.ACTION_STOP:
                handler.removeCallbacks(autoTouchRunnable);
                autoTouchPoint = null;
                break;
        }
    }

    /**
     * 执行自动点击
     */
    private void onAutoClick() {
        if (autoTouchPoint != null) {
            handler.postDelayed(autoTouchRunnable, getDelayTime());
        }
    }

    private final Runnable autoTouchRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "onAutoClick: " + "x=" + autoTouchPoint.getX() + " y=" + autoTouchPoint.getY());

            // 获取系统的GestureDescription.Builder对象
            GestureDescription.Builder builder = new GestureDescription.Builder();

            // 创建第一次点击的Path
            Path path = new Path();
            path.moveTo(autoTouchPoint.getX(), autoTouchPoint.getY());
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 1)).build();

            if (autoTouchPoint.isFunction()) {
                // 创建第二次点击的Path
                Path clickPath2 = new Path();
                clickPath2.moveTo(autoTouchPoint.getX(), autoTouchPoint.getY()); // 点击位置2，通常这里x2,y2与x1,y1有一定的间隔
                builder.addStroke(new GestureDescription.StrokeDescription(clickPath2, 100, 1)); // 100ms后模拟第二次点击
            }

            // 创建GestureDescription对象
            GestureDescription gestureDescription = builder.build();
            // 执行模拟的手势
            dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                }
            }, null);
            onAutoClick();
        }
    };

    private long getDelayTime() {
        // 单位：秒
//        return autoTouchPoint.getDelay() * 1000L;
        // 单位：毫秒
        return autoTouchPoint.getDelay();
    }

    /**
     * 有关AccessibilityEvent事件的回调函数，系统通过sendAccessibiliyEvent()不断的发送AccessibilityEvent到此处
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 获取当前侦听到的包名
        String packageName = event.getPackageName() == null ? "" : event.getPackageName().toString();
//        Log.d("onAccessibilityEvent", "###包名：" + packageName + "，事件类型=" + event.getEventType());
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                // 类型窗口内容已更改
                onWindowContentChanged(packageName);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                // 类型窗口状态已更改
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                // 类型窗口已更改
                break;
        }
    }

    /**
     * 窗口内容发生变化(判断是不是切出目标窗口，如果切出，则需要停止点赞动作)
     */
    private void onWindowContentChanged(String packageName) {
        // 包名不为空，说明外界已经设置了专属。
        if (!TextUtils.isEmpty(TouchEventManager.getInstance().getAppPackageName())) {
            // 如果活动APP不是目标APP，则不响应
            if (packageName.equalsIgnoreCase(TouchEventManager.getInstance().getAppPackageName())) {
                // 如果是动作暂停状态，则自动开启继续点赞
                if (TouchEventManager.getInstance().isPaused()) {
                    Log.d("onWindowContentChanged", "###执行了 继续动作 事件");
                    TouchEvent.postContinueAction();
                }
            } else {
                // 如果是动作开启状态，则自动停止点赞动作
                if (TouchEventManager.getInstance().isTouching()) {
                    Log.d("onWindowContentChanged", "###执行了 动作暂停 事件");
                    TouchEvent.postPauseAction();
                }
            }
        }
    }

    /**
     * 查找对应文本的View，无论该node能不能点击
     */
    public AccessibilityNodeInfo findViewByText(String text) {
        AccessibilityNodeInfo viewByText = findViewByText(text, true);
        if (viewByText == null) {
            viewByText = findViewByText(text, false);
        }
        return viewByText;
    }

    /**
     * 查找对应文本的view
     *
     * @param text      文本
     * @param clickable 该View是否可以点击
     * @return view
     */
    public AccessibilityNodeInfo findViewByText(String text, boolean clickable) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }

        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (nodeInfo.isClickable() == clickable)) {
                    return nodeInfo;
                }
            }
        }

        return null;
    }

    /**
     * 查找对应id的View
     *
     * @param id id
     * @return view
     */
    public AccessibilityNodeInfo findViewById(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }

        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }

        return null;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
