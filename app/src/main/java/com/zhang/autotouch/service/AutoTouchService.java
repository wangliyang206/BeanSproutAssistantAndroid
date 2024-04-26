package com.zhang.autotouch.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.zhang.autotouch.R;
import com.zhang.autotouch.TouchEventManager;
import com.zhang.autotouch.bean.TouchEvent;
import com.zhang.autotouch.bean.TouchPoint;
import com.zhang.autotouch.utils.DensityUtil;
import com.zhang.autotouch.utils.WindowUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

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
    private TextView tvTouchPoint;
    //倒计时
    private float countDownTime;
    private final DecimalFormat floatDf = new DecimalFormat("#0.0");
    //修改点击文本的倒计时
    private Runnable touchViewRunnable;

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
                handler.removeCallbacks(touchViewRunnable);
                break;
            case TouchEvent.ACTION_STOP:
                handler.removeCallbacks(autoTouchRunnable);
                handler.removeCallbacks(touchViewRunnable);
                removeTouchView();
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
            showTouchView();
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

            if(autoTouchPoint.isFunction()){
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
                    Log.d("AutoTouchService", "滑动结束" + gestureDescription.getStrokeCount());
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.d("AutoTouchService", "滑动取消");
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

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName() == null ? "" : event.getPackageName().toString();

        Log.d("onAccessibilityEvent", "包名：" + packageName);
        if (!TextUtils.isEmpty(TouchEventManager.getInstance().getAppPackageName())) {
            // 如果活动APP不是目标APP，则不响应
            if (packageName.equalsIgnoreCase(TouchEventManager.getInstance().getAppPackageName())) {
                // 如果是动作暂停状态，则自动开启继续点赞
                if (TouchEventManager.getInstance().isPaused()) {
                    TouchEvent.postContinueAction();
                }
            } else {
                // 如果是动作开启状态，则自动停止点赞动作
                TouchEvent.postPauseAction();
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        removeTouchView();
    }

    /**
     * 显示倒计时
     */
    private void showTouchView() {
        if (autoTouchPoint != null) {
            //创建触摸点View
            if (tvTouchPoint == null) {
                tvTouchPoint = (TextView) LayoutInflater.from(this).inflate(R.layout.window_touch_point, null);
            }
            //显示触摸点View
            if (windowManager != null && !tvTouchPoint.isAttachedToWindow()) {
                int width = DensityUtil.dip2px(this, 40);
                int height = DensityUtil.dip2px(this, 40);
                WindowManager.LayoutParams params = WindowUtils.newWmParams(width, height);
                params.gravity = Gravity.START | Gravity.TOP;
                params.x = autoTouchPoint.getX() - width / 2;
                params.y = autoTouchPoint.getY() - width;
                params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                windowManager.addView(tvTouchPoint, params);
            }
            //开启倒计时
            countDownTime = autoTouchPoint.getDelay();
            if (touchViewRunnable == null) {
                touchViewRunnable = new Runnable() {
                    @Override
                    public void run() {
                        handler.removeCallbacks(touchViewRunnable);
                        Log.d("触摸倒计时", countDownTime + "");
                        if (countDownTime > 0) {
                            float offset = 0.1f;
                            tvTouchPoint.setText(floatDf.format(countDownTime));
                            countDownTime -= offset;
                            handler.postDelayed(touchViewRunnable, (long) (1000L * offset));
                        } else {
                            removeTouchView();
                        }
                    }
                };
            }
            handler.post(touchViewRunnable);
        }
    }

    private void removeTouchView() {
        if (windowManager != null && tvTouchPoint.isAttachedToWindow()) {
            windowManager.removeView(tvTouchPoint);
        }
    }
}
