package com.wly.beansprout.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 触摸事件管理器（单例）
 * 管理当前触摸动作状态、目标 APP 包名、跳绳动画开关。
 * 使用 StateFlow 替代 EventBus，所有组件可通过 collect 观察状态变化。
 */
object TouchEventManager {

    private const val PACKAGE_DOUYIN = "com.ss.android.ugc.aweme"
    private const val PACKAGE_KWAI = "com.smile.gifmaker"

    // ---- 触摸动作 ----
    private val _touchAction = MutableStateFlow(TouchAction.IDLE)
    val touchAction: StateFlow<TouchAction> = _touchAction.asStateFlow()

    // ---- 目标 APP 包名 ----
    private val _appPackageName = MutableStateFlow(PACKAGE_DOUYIN)
    val appPackageName: StateFlow<String> = _appPackageName.asStateFlow()

    // ---- 跳绳动画 ----
    private val _isOpenSkippingRope = MutableStateFlow(false)
    val isOpenSkippingRope: StateFlow<Boolean> = _isOpenSkippingRope.asStateFlow()

    /** 是否允许抢福袋（由 FindTargetNodeUtil 根据时间条件设置） */
    var isLuckyBagAllowed: Boolean = true

    /** 当前选中的福袋方案 ID（由 FloatingMenuDialog 写入，AutoTouchService 读取） */
    var currentLuckyBagSchemeId: Int = 0

    fun setTouchAction(action: TouchAction) {
        _touchAction.value = action
    }

    fun setAppPackageName(type: Int) {
        _appPackageName.value = when (type) {
            1 -> PACKAGE_DOUYIN
            2 -> PACKAGE_KWAI
            else -> ""
        }
    }

    fun setOpenSkippingRope(open: Boolean) {
        _isOpenSkippingRope.value = open
    }

    /** 是否正在触控（START 或 CONTINUE） */
    fun isTouching(): Boolean =
        _touchAction.value == TouchAction.START || _touchAction.value == TouchAction.CONTINUE

    /** 是否处于暂停状态 */
    fun isPaused(): Boolean =
        _touchAction.value == TouchAction.PAUSE

    /** 是否处于启动状态 */
    fun isStarted(): Boolean =
        _touchAction.value == TouchAction.START

    /** 获取当前目标包名字符串 */
    fun getTargetPackage(): String = _appPackageName.value
}
