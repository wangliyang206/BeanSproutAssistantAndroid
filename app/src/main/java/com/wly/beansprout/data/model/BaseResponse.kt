package com.wly.beansprout.data.model

/**
 * 通用接口返回数据（与服务端 GsonResponse 对应）
 *
 * 服务端返回格式: {"version": "...", "errorinfo": null, "data": {...}}
 * 成功判断: errorinfo == null
 */
data class BaseResponse<out T>(
    // 接口版本
    val version: String? = null,
    // 错误信息（null 表示成功）
    val errorinfo: ErrorInfo?,
    // 成功时返回的业务数据
    val data: T?
)