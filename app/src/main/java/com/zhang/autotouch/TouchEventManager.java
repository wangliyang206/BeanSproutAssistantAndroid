package com.zhang.autotouch;

import com.zhang.autotouch.bean.TouchEvent;

public class TouchEventManager {

    private static TouchEventManager touchEventManager;
    private int touchAction;

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

    public boolean isPaused() {
        return touchAction == TouchEvent.ACTION_PAUSE;
    }
}
