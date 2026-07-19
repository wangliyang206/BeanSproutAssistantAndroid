package com.wly.beansprout.data.repository

import com.wly.beansprout.core.network.RequestHelper
import com.wly.beansprout.core.network.RetrofitClient
import com.wly.beansprout.data.model.FeedbackDetailResponse
import com.wly.beansprout.data.model.FeedbackListResponse
import com.wly.beansprout.data.model.SubmitFeedbackRequest
import com.wly.beansprout.data.model.SubmitFeedbackResponse
import javax.inject.Inject

/**
 * 咨询反馈相关数据逻辑
 */
class FeedbackRepository @Inject constructor(
    private val retrofitClient: RetrofitClient,
    private val requestHelper: RequestHelper
) : BaseRepository() {

    /**
     * 获取反馈列表
     */
    suspend fun getFeedbackList(): FeedbackListResponse {
        val requestData = mutableMapOf<String, String>()

        return requestNetwork {
            retrofitClient.apiService.getFeedbackList(
                requestHelper.buildRequest(requestData)
            )
        }
    }

    /**
     * 提交反馈
     */
    suspend fun submitFeedback(
        title: String,
        content: String
    ): SubmitFeedbackResponse {
        val request = SubmitFeedbackRequest(
            title = title,
            content = content
        )

        return requestNetwork {
            retrofitClient.apiService.submitFeedback(
                requestHelper.buildRequest(request)
            )
        }
    }

    /**
     * 获取反馈详情
     */
    suspend fun getFeedbackDetail(feedbackId: Long): FeedbackDetailResponse {
        val requestData = mutableMapOf<String, String>()
        requestData["feedbackId"] = feedbackId.toString()

        return requestNetwork {
            retrofitClient.apiService.getFeedbackDetail(
                requestHelper.buildRequest(requestData)
            )
        }
    }
}
