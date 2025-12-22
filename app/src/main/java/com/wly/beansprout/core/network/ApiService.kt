package com.wly.beansprout.core.network

import com.wly.beansprout.data.model.BaseRequest
import com.wly.beansprout.data.model.BaseResponse
import com.wly.beansprout.data.model.UserInfo
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 所有接口定义
 */
interface ApiService {
    // 登录接口
    @POST("member/login")
    suspend fun login(@Body request: BaseRequest<Map<String, String>>): BaseResponse<UserInfo>

    // 验证Token
    @POST("member/validToken")
    suspend fun validToken(@Body request: BaseRequest<Map<String, String>>): BaseResponse<UserInfo>
}