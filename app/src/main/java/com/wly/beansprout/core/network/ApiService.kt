package com.wly.beansprout.core.network

import com.wly.beansprout.data.model.AppUpdate
import com.wly.beansprout.data.model.BaseRequest
import com.wly.beansprout.data.model.BaseResponse
import com.wly.beansprout.data.model.FeedbackDetailResponse
import com.wly.beansprout.data.model.FeedbackListRequest
import com.wly.beansprout.data.model.FeedbackListResponse
import com.wly.beansprout.data.model.ReplyFeedbackRequest
import com.wly.beansprout.data.model.ReplyFeedbackResponse
import com.wly.beansprout.data.model.SubmitFeedbackRequest
import com.wly.beansprout.data.model.SubmitFeedbackResponse
import com.wly.beansprout.data.model.UploadFileResponse
import com.wly.beansprout.data.model.UserInfo
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

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
    suspend fun getFeedbackList(@Body request: BaseRequest<FeedbackListRequest>): BaseResponse<FeedbackListResponse>

    /**
     * 上传反馈附件（图片/视频）
     * token 通过 Query 传递，避免 multipart 请求时服务端拦截器无法解析
     */
    @Multipart
    @POST("feedback/upload")
    suspend fun uploadFeedbackFiles(
        @Part files: List<MultipartBody.Part>,
        @Query("token") token: String
    ): BaseResponse<UploadFileResponse>

    /**
     * 提交反馈（包含附件URL）
     */
    @POST("feedback/submit")
    suspend fun submitFeedback(
        @Body request: BaseRequest<SubmitFeedbackRequest>
    ): BaseResponse<SubmitFeedbackResponse>

    @POST("feedback/detail")
    suspend fun getFeedbackDetail(@Body request: BaseRequest<Map<String, String>>): BaseResponse<FeedbackDetailResponse>

    @POST("feedback/reply")
    suspend fun replyFeedback(@Body request: BaseRequest<ReplyFeedbackRequest>): BaseResponse<ReplyFeedbackResponse>
}
