package com.wly.beansprout.core.network

import com.wly.beansprout.data.model.LoginRequest
import com.wly.beansprout.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 所有接口定义
 */
interface ApiService {
    // 登录接口（模拟真实接口，替换为你的后端地址）
    @POST("user/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}