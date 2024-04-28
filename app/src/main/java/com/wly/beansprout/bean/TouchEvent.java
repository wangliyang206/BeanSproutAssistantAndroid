package com.wly.beansprout.bean;

import androidx.annotation.NonNull;

import com.wly.beansprout.utils.GsonUtils;

import org.greenrobot.eventbus.EventBus;

public class TouchEvent {

    // 动作开启
    public static final int ACTION_START = 1;
    // 动作暂停
    public static final int ACTION_PAUSE = 2;
    // 动作继续
    public static final int ACTION_CONTINUE = 3;
    // 动作停止
    public static final int ACTION_STOP = 4;

    private int action;
    private TouchPoint touchPoint;

    private TouchEvent(int action) {
        this(action, null);
    }

    private TouchEvent(int action, TouchPoint touchPoint) {
        this.action = action;
        this.touchPoint = touchPoint;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public TouchPoint getTouchPoint() {
        return touchPoint;
    }

    /**
     * 动作开启
     */
    public static void postStartAction(TouchPoint touchPoint) {
        postAction(new TouchEvent(ACTION_START, touchPoint));
    }

    /**
     * 暂停动作
     */
    public static void postPauseAction() {
        postAction(new TouchEvent(ACTION_PAUSE));
    }

    /**
     * 动作继续
     */
    public static void postContinueAction() {
        postAction(new TouchEvent(ACTION_CONTINUE));
    }

    /**
     * 动作停止
     */
    public static void postStopAction() {
        postAction(new TouchEvent(ACTION_STOP));
    }

    private static void postAction(TouchEvent touchEvent) {
        EventBus.getDefault().post(touchEvent);
    }

    @NonNull
    @Override
    public String toString() {
        return "action=" + action + " touchPoint=" + (touchPoint == null ? "null" : GsonUtils.beanToJson(touchPoint));
    }
}
