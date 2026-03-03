package com.wly.beansprout.feature.home.ui

import androidx.compose.runtime.Stable

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
    val selectedFunction: Int = 0,  // 单选功能
    val selectedModel: Int = 0,  // 0:功德小鸡, 1:跳绳小鸡

    // 版本信息
    val versionName: String = "",

    // UI 状态
    val showLogoutDialog: Boolean = false,
    val showStartDialog: Boolean = false
)

/**
 * 首页事件
 */
sealed class HomeEvent {
    object NavigateToLogin : HomeEvent()
    object ShowExitAppDialog : HomeEvent()
    object ShowStartDialog : HomeEvent()
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