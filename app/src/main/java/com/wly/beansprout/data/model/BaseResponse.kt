package com.wly.beansprout.data.model

/**
 * 通用接口返回数据
 */
data class BaseResponse<out T>(
    // 200 = 成功，其他 = 失败
    val code: Int,
    val errorinfo: ErrorInfo?,
    // 成功返回用户数据
    val data: T?
)