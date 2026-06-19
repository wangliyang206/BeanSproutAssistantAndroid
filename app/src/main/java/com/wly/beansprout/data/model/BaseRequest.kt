package com.wly.beansprout.data.model

/**
 * 通用接口请求数据（与服务端 GsonRequest 对应）
 */
data class BaseRequest<T>(
    // 用户ID
    val userId: String? = null,
    // 接口版本号（服务端约定为 1）
    val version: Int = 1,
    // 客户端设备信息
    val client: ClientInfo? = null,
    // 用户令牌
    val token: String? = null,
    // 语言（ZH/EN/AR）
    val language: String? = null,
    // 请求数据
    val data: T
)