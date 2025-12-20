package com.wly.beansprout.data.model

data class LoginResponse(
    // 200 = 成功，其他 = 失败
    val code: Int,
    val msg: String,
    // 成功返回用户数据
    val data: UserInfo?
)