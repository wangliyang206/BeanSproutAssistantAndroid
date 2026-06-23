package com.wly.beansprout.feature.tutorial.ui

/**
 * 教程项数据类
 *
 * @param title 教程标题
 * @param description 教程简介
 * @param videoUrl 教学视频的网络 URL
 */
data class TutorialItem(
    val title: String,
    val description: String,
    val videoUrl: String
)

/**
 * 预置的教程列表
 *
 * 视频 URL 为占位符，后续替换为真实地址。
 */
val defaultTutorials = listOf(
    TutorialItem(
        title = "开启无障碍服务",
        description = "学习如何开启无障碍服务以启用自动操作功能",
        videoUrl = "https://your-server.com/tutorials/accessibility.mp4"
    ),
    TutorialItem(
        title = "直播点赞",
        description = "学习如何设置和使用直播自动点赞功能",
        videoUrl = "https://your-server.com/tutorials/live_like.mp4"
    ),
    TutorialItem(
        title = "自动回复",
        description = "学习如何配置自动回复话术并在直播间自动发送",
        videoUrl = "https://your-server.com/tutorials/auto_reply.mp4"
    ),
    TutorialItem(
        title = "抢福袋",
        description = "学习如何录制坐标、设置方案并自动抢夺直播间福袋",
        videoUrl = "https://your-server.com/tutorials/lucky_bag.mp4"
    )
)
