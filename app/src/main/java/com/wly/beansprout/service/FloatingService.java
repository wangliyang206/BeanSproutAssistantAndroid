package com.wly.beansprout.service;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.wly.beansprout.R;
import com.wly.beansprout.TouchEventManager;
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
    private ImageView mFloatingView;
    private MenuDialog menuDialog;
    private WindowManager.LayoutParams floatLayoutParams;

    // 是否勾选的点赞功能
    private boolean isFunction;
    // 当前窗口的X、Y坐标
    private int x;
    private int y;
    // 目标窗口的X、Y坐标
    private int targetX;
    private int targetY;
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
        mFloatingView.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:                                                       // 按下动作
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    isMoving = false;
                    return true;
                case MotionEvent.ACTION_MOVE:                                                       // 移动动作
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
                case MotionEvent.ACTION_UP:                                                         // 抬起动作
                    if (isMoving) {
                        // 移动后抬起的动作
                        // 如果正在触控中，则开启动画
                        if (TouchEventManager.getInstance().isTouching()) {
                            onStartAnimation(targetX, targetY);
                        }
                    } else {
                        // 按下后抬起的动作
                        onShowSelectDialog();
                        return true;
                    }
                    break;
            }
            return false;
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

                    targetX = x;
                    targetY = y;
                    onStartAnimation(x, y);
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
     * 动画入口
     */
    private void onStartAnimation(int x, int y) {
        // 特殊处理，由于鸡的目标位置容易挡住点击位置，这里需要将鸡的位置向下移10像素
        y = y + 10;

        // 开启溜达鸡动画
//        onStartStrollingChickenAnimation(x, y);
        // 开启闪现鸡动画
        onStartFlashChickenAnimation(x, y);
    }

    /**
     * 开启溜达鸡动画
     */
    private void onStartStrollingChickenAnimation(int targetX, int targetY) {
        Log.d("onFloatWindowAttachChange", "targetX=" + targetX + ";targetY=" + targetY);

        if (mWindowManager != null) {
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

    /**
     * 开启闪现鸡动画
     */
    private void onStartFlashChickenAnimation(int targetX, int targetY) {
        Log.d("onFloatWindowAttachChange", "targetX=" + targetX + ";targetY=" + targetY);

        if (mWindowManager != null) {
            // 收缩动画
            AnimationDrawable shrinkAnim = (AnimationDrawable) ContextCompat.getDrawable(getApplicationContext(), R.drawable.hide_frame_animation);
//            shrinkAnim.selectDrawable(0);
            mFloatingView.setImageDrawable(shrinkAnim);
            shrinkAnim.start();

            // 使用Handler来在动画结束后执行操作
            new Handler().postDelayed(() -> {
                // 这里是动画播放完成后的操作
                // 例如，你可以重置动画或执行其他任务
                shrinkAnim.stop(); // 如果需要的话，可以停止动画

//                mFloatingView.setImageResource(R.mipmap.icon_wandering_chicken);

                // 闪现
                floatLayoutParams.x = targetX;
                floatLayoutParams.y = targetY;
                mWindowManager.updateViewLayout(mFloatingView, floatLayoutParams);

                // 冒出
                AnimationDrawable extendAnim = (AnimationDrawable) ContextCompat.getDrawable(getApplicationContext(), R.drawable.show_frame_animation);
//                extendAnim.selectDrawable(0);
                mFloatingView.setImageDrawable(extendAnim);
                extendAnim.start();

            }, 375);// 动画的总时长（毫秒）

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
