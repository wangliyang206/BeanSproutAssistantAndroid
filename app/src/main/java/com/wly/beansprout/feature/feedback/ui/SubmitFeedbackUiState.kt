package com.wly.beansprout.feature.feedback.ui

/**
 * 提交反馈UI状态
 */
data class SubmitFeedbackUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val content: String = "",
    val errorMessage: String? = null,
    val submitSuccess: Boolean = false
)

/**
 * 提交反馈事件
 */
sealed class SubmitFeedbackEvent {
    data object SubmitSuccess : SubmitFeedbackEvent()
    data class ShowError(val message: String) : SubmitFeedbackEvent()
}
