package com.wly.beansprout.data.model

/**
 * 通用接口请求数据
 */
data class BaseRequest<T>(
    // 用户ID
    val userId: String? = null,
    // 用户令牌
    val token: String? = null,
    // 请求数据
    val data: T
)