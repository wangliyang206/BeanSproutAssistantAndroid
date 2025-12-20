package com.wly.beansprout.data.model

/**
 * 登录接口返回数据
 */
data class UserInfo(
    val token: String,
    val userId: String,
    // 用户名
    val userName: String,
    // 手机号
    val userPhone: String,
    // 状态：1待审核、2审核中、3已退回、4使用中、5已停用、6体验中、9已删除。
    val status: Int,
    // 体验状态 - 剩余天数
    val daysRemaining: Int,
    // 是否记住密码
    val rememberPassword: Boolean = false
)