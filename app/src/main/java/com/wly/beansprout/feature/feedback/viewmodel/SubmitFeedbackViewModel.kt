package com.wly.beansprout.feature.feedback.viewmodel

import androidx.lifecycle.viewModelScope
import com.wly.beansprout.core.base.BaseViewModel
import com.wly.beansprout.data.repository.FeedbackRepository
import com.wly.beansprout.feature.feedback.ui.SelectedMedia
import com.wly.beansprout.feature.feedback.ui.SubmitFeedbackEvent
import com.wly.beansprout.feature.feedback.ui.SubmitFeedbackUiState
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
 * 提交反馈ViewModel
 */
@HiltViewModel
class SubmitFeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SubmitFeedbackUiState())
    val uiState: StateFlow<SubmitFeedbackUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SubmitFeedbackEvent>()
    val events: SharedFlow<SubmitFeedbackEvent> = _events.asSharedFlow()

    /**
     * 更新标题
     */
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    /**
     * 更新内容
     */
    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    /**
     * 添加选中的媒体文件
     */
    fun addFiles(medias: List<SelectedMedia>) {
        _uiState.update {
            val currentFiles = it.selectedFiles.toMutableList()
            currentFiles.addAll(medias)
            it.copy(selectedFiles = currentFiles)
        }
    }

    /**
     * 移除文件
     */
    fun removeFile(media: SelectedMedia) {
        _uiState.update {
            it.copy(selectedFiles = it.selectedFiles.filter { m -> m != media })
        }
    }

    /**
     * 提交反馈
     */
    fun submitFeedback() {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            viewModelScope.launch {
                _events.emit(SubmitFeedbackEvent.ShowError("请输入反馈标题"))
            }
            return
        }

        if (currentState.content.isBlank()) {
            viewModelScope.launch {
                _events.emit(SubmitFeedbackEvent.ShowError("请输入反馈内容"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = feedbackRepository.submitFeedback(
                    title = currentState.title,
                    content = currentState.content,
                    mediaFiles = if (currentState.selectedFiles.isNotEmpty()) currentState.selectedFiles else null
                )

                if (result.success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            submitSuccess = true
                        )
                    }
                    _events.emit(SubmitFeedbackEvent.SubmitSuccess)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "提交失败"
                        )
                    }
                    _events.emit(SubmitFeedbackEvent.ShowError("提交失败"))
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "提交失败"
                    )
                }
                _events.emit(SubmitFeedbackEvent.ShowError(e.message ?: "提交失败"))
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
