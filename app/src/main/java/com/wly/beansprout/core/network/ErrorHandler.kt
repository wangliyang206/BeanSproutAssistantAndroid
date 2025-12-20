package com.wly.beansprout.core.network

/**
 * 网络错误统一处理
 */
object ErrorHandler {
    fun handleException(e: Exception): String {
        return when (e) {
            is retrofit2.HttpException -> {
                when (e.code()) {
                    401 -> "登录失效，请重新登录"
                    403 -> "权限不足"
                    500 -> "服务器错误"
                    else -> "网络请求失败（${e.code()}）"
                }
            }
            is java.net.ConnectException -> "网络连接失败，请检查网络"
            is java.net.SocketTimeoutException -> "网络超时"
            else -> e.message ?: "未知错误"
        }
    }
}