package com.zhang.autotouch;

import com.zhang.autotouch.bean.TouchEvent;

public class TouchEventManager {
    // 快手
    private final String kwai = "com.smile.gifmaker";
    // 抖音
    private final String tiktok = "com.ss.android.ugc.aweme";

    private static TouchEventManager touchEventManager;
    // 触摸动作
    private int touchAction;
    // 专属(包名)
    private String appPackageName = tiktok;

    public static TouchEventManager getInstance() {
        if (touchEventManager == null) {
            synchronized (TouchEventManager.class) {
                if (touchEventManager == null) {
                    touchEventManager = new TouchEventManager();
                }
            }
        }
        return touchEventManager;
    }

    private TouchEventManager() {
    }

    public void setTouchAction(int touchAction) {
        this.touchAction = touchAction;
    }

    public int getTouchAction() {
        return touchAction;
    }

    /**
     * @return 正在触控
     */
    public boolean isTouching() {
        return touchAction == TouchEvent.ACTION_START || touchAction == TouchEvent.ACTION_CONTINUE;
    }

    /**
     * 是否暂停动作服务
     */
    public boolean isPaused() {
        return touchAction == TouchEvent.ACTION_PAUSE;
    }

    /**
     * 是否开启动作服务
     */
    public boolean isStart() {
        return touchAction == TouchEvent.ACTION_START;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    /**
     * 设置专属
     */
    public void setAppPackageName(int type) {
        if (type == 1) {
            this.appPackageName = tiktok;
        } else if (type == 2) {
            this.appPackageName = kwai;
        } else {
            this.appPackageName = "";
        }
    }
}
