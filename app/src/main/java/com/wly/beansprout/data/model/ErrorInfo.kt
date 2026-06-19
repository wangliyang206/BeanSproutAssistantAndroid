package com.wly.beansprout.data.model

/**
 * 错误信息
 */
data class ErrorInfo(
    // 错误代码（负数表示错误，正数是警告）
    val errorcode: String,
    // 错误信息
    val errormessage: String
)