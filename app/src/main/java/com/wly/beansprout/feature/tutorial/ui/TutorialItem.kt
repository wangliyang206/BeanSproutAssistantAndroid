package com.wly.beansprout.feature.tutorial.ui

/**
 * 教程项数据类
 *
 * @param title 教程标题
 * @param description 教程简介
 * @param videoUrl 视频地址：本地视频为 res/raw 资源名（不含前缀），网络视频为 http/https URL
 * @param isLocal true=本地视频（res/raw），false=网络视频
 */
data class TutorialItem(
    val title: String,
    val description: String,
    val videoUrl: String,
    val isLocal: Boolean = true
)

/**
 * 预置的教程列表
 *
 * 本地视频文件放置在 app/src/main/res/raw/ 目录下：
 *   tutorial_accessibility.mp4 — 开启无障碍服务
 *   tutorial_live_like.mp4     — 直播点赞
 *   tutorial_auto_reply.mp4    — 自动回复
 *   tutorial_lucky_bag.mp4     — 抢福袋
 */
val defaultTutorials = listOf(
    TutorialItem(
        title = "开启无障碍服务",
        description = "学习如何开启无障碍服务以启用自动操作功能",
        videoUrl = "tutorial_accessibility"
    ),
    TutorialItem(
        title = "直播点赞",
        description = "学习如何设置和使用直播自动点赞功能",
        videoUrl = "tutorial_live_like"
    ),
    TutorialItem(
        title = "自动回复",
        description = "学习如何配置自动回复话术并在直播间自动发送",
        videoUrl = "tutorial_auto_reply"
    ),
    TutorialItem(
        title = "抢福袋",
        description = "学习如何录制坐标、设置方案并自动抢夺直播间福袋",
        videoUrl = "tutorial_lucky_bag"
    )
)
