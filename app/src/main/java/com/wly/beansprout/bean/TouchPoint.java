package com.wly.beansprout.bean;

public class TouchPoint {
    // 标题名称
    private String name;
    // 屏幕点击位置
    private int x;
    private int y;
    // 时间间隔
    private int delay;
    // 是否开启点击
    private boolean isStartClick;
    // 功能：0其它；1单击；2点赞；3向下滑动；4向上滑动；5向左滑动；6向右滑动；7自动回复；8抢福袋；
    private int functionType = 0;
    // 抢福袋时间
    private int luckybagTime;

    // 复制方法
    public TouchPoint copy() {
        return new TouchPoint(name, x, y, delay, isStartClick, functionType, luckybagTime);
    }

    public TouchPoint(String name, int x, int y, int delay) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.delay = delay;
    }

    public TouchPoint(String name, int x, int y, int delay, boolean isStartClick, int functionType, int luckybagTime) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.delay = delay;
        this.isStartClick = isStartClick;
        this.functionType = functionType;
        this.luckybagTime = luckybagTime;
    }

    public int getFunctionType() {
        return functionType;
    }

    public void setFunctionType(int functionType) {
        this.functionType = functionType;
    }

    public int getLuckybagTime() {
        return luckybagTime;
    }

    public void setLuckybagTime(int luckybagTime) {
        this.luckybagTime = luckybagTime;
    }

    public boolean isStartClick() {
        return isStartClick;
    }

    public void setStartClick(boolean startClick) {
        isStartClick = startClick;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDelay() {
        return delay;
    }
}
