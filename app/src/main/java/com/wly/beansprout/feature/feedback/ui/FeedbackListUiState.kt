package com.wly.beansprout.feature.feedback.ui

import com.wly.beansprout.data.model.Feedback

/**
 * 反馈列表UI状态
 */
data class FeedbackListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val feedbackList: List<Feedback> = emptyList(),
    val errorMessage: String? = null,
    val total: Int = 0
)

/**
 * 反馈列表事件
 */
sealed class FeedbackListEvent {
    data object NavigateToSubmit : FeedbackListEvent()
    data class NavigateToDetail(val feedbackId: Long) : FeedbackListEvent()
}
