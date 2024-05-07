package com.wly.beansprout.service;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.wly.beansprout.R;
import com.wly.beansprout.bean.Point;
import com.wly.beansprout.dialog.MenuDialog;
import com.wly.beansprout.utils.DensityUtil;
import com.wly.beansprout.utils.PathFinder;
import com.wly.beansprout.utils.WindowUtils;

import java.util.List;

/**
 * 悬浮窗
 */
public class FloatingService extends Service {
    private WindowManager mWindowManager;
    private View mFloatingView;
    private MenuDialog menuDialog;
    private WindowManager.LayoutParams floatLayoutParams;

    // 是否开启点赞
    private boolean isFunction;
    // 不是窗口的X和Y，是mFloatingView的X和Y
    private int x;
    private int y;
    //是否在移动
    private boolean isMoving;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // 处理传递过来的数据
            isFunction = intent.getBooleanExtra("isFunction", false);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFloatingView = creatView(R.layout.layout_window);
        //设置WindowManger布局参数以及相关属性
        int d = DensityUtil.dip2px(this, 80);
        floatLayoutParams = WindowUtils.newWmParams(d, d);
        //初始化位置
        floatLayoutParams.gravity = Gravity.TOP | Gravity.START;
        floatLayoutParams.x = WindowUtils.getScreenWidth(this) - DensityUtil.dip2px(this, 80);
        floatLayoutParams.y = WindowUtils.getScreenHeight(this) - DensityUtil.dip2px(this, 200);
        //获取WindowManager对象
        mWindowManager = WindowUtils.getWindowManager(this);
        addViewToWindow(mFloatingView, floatLayoutParams);
        //FloatingView的拖动事件
        mFloatingView.setClickable(true);
        mFloatingView.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        isMoving = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();
                        int moveX = nowX - x;
                        int moveY = nowY - y;
                        if (Math.abs(moveX) > 0 || Math.abs(moveY) > 0) {
                            isMoving = true;
                            floatLayoutParams.x += moveX;
                            floatLayoutParams.y += moveY;
                            //更新View的位置
                            mWindowManager.updateViewLayout(mFloatingView, floatLayoutParams);
                            x = nowX;
                            y = nowY;
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            onShowSelectDialog();
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void onShowSelectDialog() {
        //弹出菜单弹窗
        hideDialog(menuDialog);
        if (menuDialog == null) {
            menuDialog = new MenuDialog(this);
            menuDialog.setListener(new MenuDialog.Listener() {
                @Override
                public void onFloatWindowAttachChange(int x, int y) {
//                    if (attach) {
//                        addViewToWindow(mFloatingView, floatLayoutParams);
//                    } else {
//                        removeViewFromWinddow(mFloatingView);
//                    }
                    openWanderingChicken(x, y);
                }

                @Override
                public void onExitService() {
                    stopSelf();
                }
            });
        }
        menuDialog.setFunction(isFunction);
        menuDialog.show();
    }

    /**
     * 开启溜达鸡动画
     */
    private void openWanderingChicken(int targetX, int targetY) {
        Log.d("onFloatWindowAttachChange", "targetX=" + targetX + ";targetY=" + targetY);

        if (mWindowManager != null) {
            // 闪现
//            floatLayoutParams.x = targetX;
//            floatLayoutParams.y = targetY;
//            mWindowManager.updateViewLayout(mFloatingView, floatLayoutParams);

            // 让鸡一步一步走过去，第一步，拿到鸡的位置
            int startX = (x == 0 && y == 0) ? floatLayoutParams.x : x;
            int startY = (x == 0 && y == 0) ? floatLayoutParams.y : y;

            Log.d("openWanderingChicken", "startX=" + startX + ";startY=" + startY);

            PathFinder mPathFinder = new PathFinder(startX, startY, targetX, targetY);
            List<Point> list = mPathFinder.getPath();
            Log.d("openWanderingChicken", "list=" + list.size());

            if (!list.isEmpty()) {
                // Handler用于发送Runnable或Message到主线程的消息队列
                final Handler handler = new Handler();
                // Runnable定义了要执行的任务
                final Runnable moveViewTask = new Runnable() {
                    private int num = 0;

                    @Override
                    public void run() {
                        // 更新View的位置
                        if (num < list.size()) {
                            Point info = list.get(num);
                            floatLayoutParams.x = info.x;
                            floatLayoutParams.y = info.y;
                            mWindowManager.updateViewLayout(mFloatingView, floatLayoutParams);
                            Log.d("过程", "info.x=" + info.x + ";info.y=" + info.y);
                            num++;

                            handler.postDelayed(this, 500); // 0.5秒后再次执行
                        }
                    }
                };

                // 开始移动View
                handler.postDelayed(moveViewTask, 500);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeViewFromWinddow(mFloatingView);
        hideDialog(menuDialog);
    }

    private void hideDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void addViewToWindow(View view, WindowManager.LayoutParams params) {
        if (mWindowManager != null) {
            mWindowManager.addView(view, params);
        }
    }

    private void removeViewFromWinddow(View view) {
        if (mWindowManager != null && view != null && view.isAttachedToWindow()) {
            mWindowManager.removeView(view);
        }
    }

    private <T extends View> T creatView(int layout) {
        return (T) LayoutInflater.from(this).inflate(layout, null);
    }
}
