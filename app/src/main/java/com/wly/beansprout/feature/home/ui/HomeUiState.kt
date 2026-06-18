package com.wly.beansprout.feature.home.ui

import androidx.compose.runtime.Stable

/**
 * 开始按钮状态
 * 对应旧项目 MainActivity 中的多态按钮：无障碍服务 → 悬浮窗权限 → 开始 → 已开启
 */
enum class StartButtonState {
    NEED_ACCESSIBILITY,   // 需要开启无障碍服务
    NEED_OVERLAY,         // 需要开启悬浮窗权限
    READY,                // 就绪，可以开始
    RUNNING               // 服务已运行
}

/**
 * 首页 UI 状态
 */
@Stable
data class HomeUiState(
    // 用户信息
    val phoneNumber: String = "15032134297",
    val userType: String = "体验用户",

    // 设置选项
    val selectedExclusive: Int = 0,  // 0:抖音, 1:快手, 2:其它
    val selectedFunction: Int = 1,  // 默认选中"直播点赞"（与旧项目一致）
    val selectedModel: Int = 1,  // 默认选中"跳绳小鸡"（与旧项目一致）
    val selectedLuckyBagTime: Int = 999,  // 默认"不设置"（与旧项目 tag=999 一致）

    // 版本信息
    val versionName: String = "",

    // 开始按钮状态
    val startButtonState: StartButtonState = StartButtonState.NEED_ACCESSIBILITY,

    // UI 状态
    val showLogoutDialog: Boolean = false,
    val showStartDialog: Boolean = false,
    val showAccessibilityDialog: Boolean = false,
    val showOverlayDialog: Boolean = false,
    val showLuckyBagTipDialog: Boolean = false  // 福袋每日提示弹窗
)

/**
 * 首页事件
 */
sealed class HomeEvent {
    object NavigateToLogin : HomeEvent()
    object NavigateToServiceAgreement : HomeEvent()
    object NavigateToPrivacyPolicy : HomeEvent()
    object NavigateToTutorial : HomeEvent()
    object NavigateToAccessibilitySettings : HomeEvent()
    object NavigateToOverlaySettings : HomeEvent()
    object ShowExitAppDialog : HomeEvent()
    object ShowStartDialog : HomeEvent()
    object NavigateToAddTouchPoint : HomeEvent()
    data class ShowToast(val message: String) : HomeEvent()
}

/**
 * 功能选项数据
 */
object HomeFunctionOptions {
    // 基础功能选项
    val baseFunctions = listOf(
        "轻点触发",
        "直播点赞",
        "向下滑动",
        "向上滑动",
        "向左滑动",
        "向右滑动",
        "自动回复"
    )

    // 抖音专属功能
    val douyinExclusiveFunctions = listOf("抢福袋")

    // 专属平台选项
    val exclusiveOptions = listOf("抖音", "快手", "其它")

    // 模型选项
    val modelOptions = listOf("功德小鸡", "跳绳小鸡")

    // 福袋时间选项（索引对应 tag 值：0→999不设置, 1→998随机5~10, 2→997随机0~5, 3→5分钟, 4→1分钟, 5→小于1分）
    val luckyBagTimeOptions = listOf("不设置", "5~10随机", "0~5随机", "5分钟", "1分钟", "小于1分")

    /**
     * 根据选择的专属平台获取功能选项
     */
    fun getFunctionsForExclusive(exclusive: Int): List<String> {
        return if (exclusive == 0) { // 抖音
            baseFunctions + douyinExclusiveFunctions
        } else {
            baseFunctions
        }
    }
}