package com.wly.beansprout.feature.feedback.viewmodel

import androidx.lifecycle.viewModelScope
import com.wly.beansprout.core.base.BaseViewModel
import com.wly.beansprout.data.repository.FeedbackRepository
import com.wly.beansprout.feature.feedback.ui.FeedbackListEvent
import com.wly.beansprout.feature.feedback.ui.FeedbackListUiState
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
 * 反馈列表ViewModel
 */
@HiltViewModel
class FeedbackListViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(FeedbackListUiState())
    val uiState: StateFlow<FeedbackListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeedbackListEvent>()
    val events: SharedFlow<FeedbackListEvent> = _events.asSharedFlow()

    /**
     * 初始化加载
     */
    fun loadInitial() {
        if (_uiState.value.feedbackList.isEmpty()) {
            refresh()
        }
    }

    /**
     * 下拉刷新
     */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadFeedbackList()
    }

    private fun loadFeedbackList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = feedbackRepository.getFeedbackList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        feedbackList = response.list,
                        total = response.total
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "加载失败"
                    )
                }
            }
        }
    }

    /**
     * 导航到提交反馈页
     */
    fun navigateToSubmit() {
        viewModelScope.launch {
            _events.emit(FeedbackListEvent.NavigateToSubmit)
        }
    }

    /**
     * 导航到反馈详情
     */
    fun navigateToDetail(feedbackId: Long) {
        viewModelScope.launch {
            _events.emit(FeedbackListEvent.NavigateToDetail(feedbackId))
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
