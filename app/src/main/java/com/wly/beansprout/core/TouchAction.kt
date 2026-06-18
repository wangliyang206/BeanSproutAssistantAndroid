package com.wly.beansprout.core

/**
 * 触摸动作类型
 */
enum class TouchAction(val value: Int) {
    IDLE(0),
    START(1),
    PAUSE(2),
    CONTINUE(3),
    STOP(4);

    companion object {
        fun fromValue(value: Int): TouchAction =
            entries.firstOrNull { it.value == value } ?: IDLE
    }
}
