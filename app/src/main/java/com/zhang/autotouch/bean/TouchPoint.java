package com.zhang.autotouch.bean;

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

    public TouchPoint(String name, int x, int y, int delay) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.delay = delay;
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
