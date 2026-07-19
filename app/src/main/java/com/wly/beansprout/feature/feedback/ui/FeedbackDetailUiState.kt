package com.wly.beansprout.feature.feedback.ui

import com.wly.beansprout.data.model.Feedback
import com.wly.beansprout.data.model.FeedbackReply

/**
 * 反馈详情UI状态
 */
data class FeedbackDetailUiState(
    val isLoading: Boolean = false,
    val feedback: Feedback? = null,
    val replies: List<FeedbackReply> = emptyList(),
    val errorMessage: String? = null
)

/**
 * 反馈详情事件
 */
sealed class FeedbackDetailEvent {
    data class ShowError(val message: String) : FeedbackDetailEvent()
}
