package com.wly.beansprout.data.model

/**
 * 客户端设备信息（与旧项目 ClientInfo.java 对应）
 */
data class ClientInfo(
    // 手机号
    val cell: String? = null,
    // 设备ID (IMEI)
    val deviceid: String? = null,
    // SIM卡序列号
    val simid: String? = null,
    // 操作系统: android
    val os: String = "android",
    // 系统版本
    val osver: String = android.os.Build.VERSION.SDK_INT.toString(),
    // 客户端版本号
    val vercode: Int = 0,
    // 客户端版本名
    val vername: String = "",
    // 屏幕高度(像素)
    val ppiheight: Int = 0,
    // 屏幕宽度(像素)
    val ppiwidth: Int = 0
)
