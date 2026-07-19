package com.wly.beansprout.feature.feedback.viewmodel

import androidx.lifecycle.viewModelScope
import com.wly.beansprout.core.base.BaseViewModel
import com.wly.beansprout.data.repository.FeedbackRepository
import com.wly.beansprout.feature.feedback.ui.FeedbackDetailEvent
import com.wly.beansprout.feature.feedback.ui.FeedbackDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 反馈详情ViewModel
 */
@HiltViewModel
class FeedbackDetailViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(FeedbackDetailUiState())
    val uiState: StateFlow<FeedbackDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeedbackDetailEvent>()
    val events: SharedFlow<FeedbackDetailEvent> = _events.asSharedFlow()

    private var currentFeedbackId: Long = 0

    /**
     * 初始化，加载反馈详情
     */
    fun loadDetail(feedbackId: Long) {
        currentFeedbackId = feedbackId
        loadFeedbackDetail()
    }

    /**
     * 刷新
     */
    fun refresh() {
        loadFeedbackDetail()
    }

    private fun loadFeedbackDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = feedbackRepository.getFeedbackDetail(currentFeedbackId)
                val feedback = response.feedback
                val replies = feedback?.replies ?: emptyList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        feedback = feedback,
                        replies = replies
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "加载失败"
                    )
                }
                _events.emit(FeedbackDetailEvent.ShowError(e.message ?: "加载失败"))
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
