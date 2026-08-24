package com.wly.beansprout.feature.feedback.viewmodel

import androidx.lifecycle.viewModelScope
import com.wly.beansprout.core.base.BaseViewModel
import com.wly.beansprout.data.model.FeedbackMedia
import com.wly.beansprout.data.model.FeedbackStatus
import com.wly.beansprout.data.repository.FeedbackRepository
import com.wly.beansprout.feature.feedback.ui.FeedbackDetailEvent
import com.wly.beansprout.feature.feedback.ui.FeedbackDetailUiState
import com.wly.beansprout.feature.feedback.ui.SelectedMedia
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
                // 已关闭的反馈不允许追问
                val canReply = feedback?.status != FeedbackStatus.CLOSED.value

                // 调试日志：打印回复中的媒体数据，排查字段名问题
                replies.forEach { reply ->
                    android.util.Log.d(
                        "FeedbackDetail",
                        "replyId=${reply.replyId}, content=${reply.replyContent}, " +
                            "mediaList=${reply.mediaList}, replyType=${reply.replyType}"
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        feedback = feedback,
                        replies = replies,
                        canReply = canReply
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
     * 更新输入框内容
     */
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
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
     * 发送追问
     */
    fun sendReply() {
        val currentState = _uiState.value
        val content = currentState.inputText.trim()
        val hasFiles = currentState.selectedFiles.isNotEmpty()

        if (content.isBlank() && !hasFiles) {
            return
        }

        if (currentState.isSending) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            try {
                feedbackRepository.replyFeedback(
                    feedbackId = currentFeedbackId,
                    replyContent = content,
                    mediaFiles = if (hasFiles) currentState.selectedFiles else null
                )

                _uiState.update {
                    it.copy(
                        isSending = false,
                        inputText = "",
                        selectedFiles = emptyList()
                    )
                }

                loadFeedbackDetail()

                _events.emit(FeedbackDetailEvent.SendSuccess)
                _events.emit(FeedbackDetailEvent.ScrollToBottom)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false) }
                _events.emit(FeedbackDetailEvent.ShowError(e.message ?: "发送失败"))
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 显示媒体预览
     */
    fun showPreview(media: FeedbackMedia) {
        _uiState.update { it.copy(previewMedia = media) }
    }

    /**
     * 隐藏媒体预览
     */
    fun hidePreview() {
        _uiState.update { it.copy(previewMedia = null) }
    }
}
