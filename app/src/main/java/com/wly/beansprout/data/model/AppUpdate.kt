package com.wly.beansprout.data.model

/**
 * 应用更新信息（服务端返回）
 */
data class AppUpdate(
    val _id: Int = 0,
    val verCode: Int = 0,
    val verName: String = "",
    val name: String = "",
    val fileName: String = "",
    val filePath: String = "",       // APK 下载 URL
    val appForce: Int = 0,           // 1=强制更新 0=可选更新
    val newAppSize: Float = 0f,      // APK 大小（MB）
    val newAppUpdateDesc: String = "" // 更新说明
)
