package com.wly.beansprout.core.network

import com.wly.beansprout.data.model.AppUpdate
import com.wly.beansprout.data.model.BaseRequest
import com.wly.beansprout.data.model.BaseResponse
import com.wly.beansprout.data.model.FeedbackDetailResponse
import com.wly.beansprout.data.model.FeedbackListResponse
import com.wly.beansprout.data.model.SubmitFeedbackRequest
import com.wly.beansprout.data.model.SubmitFeedbackResponse
import com.wly.beansprout.data.model.UserInfo
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("member/login")
    suspend fun login(@Body request: BaseRequest<Map<String, String>>): BaseResponse<UserInfo>

    @POST("member/validToken")
    suspend fun validToken(@Body request: BaseRequest<Map<String, String>>): BaseResponse<UserInfo>

    @POST("member/register")
    suspend fun register(@Body request: BaseRequest<Map<String, String>>): BaseResponse<UserInfo>

    @POST("system/getVersion")
    suspend fun getVersion(@Body request: BaseRequest<Map<String, String>>): BaseResponse<AppUpdate>

    // ==================== 咨询反馈相关接口 ====================

    @POST("feedback/list")
    suspend fun getFeedbackList(@Body request: BaseRequest<Map<String, String>>): BaseResponse<FeedbackListResponse>

    @POST("feedback/submit")
    suspend fun submitFeedback(@Body request: BaseRequest<SubmitFeedbackRequest>): BaseResponse<SubmitFeedbackResponse>

    @POST("feedback/detail")
    suspend fun getFeedbackDetail(@Body request: BaseRequest<Map<String, String>>): BaseResponse<FeedbackDetailResponse>
}
