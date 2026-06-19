package com.wly.beansprout.feature.touchpoint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wly.beansprout.data.model.TouchPoint
import com.wly.beansprout.data.repository.TouchPointRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 触点管理 ViewModel
 */
@HiltViewModel
class TouchPointViewModel @Inject constructor(
    private val repo: TouchPointRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TouchPointUiState())
    val uiState: StateFlow<TouchPointUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TouchPointEvent>()
    val events = _events.asSharedFlow()

    init {
        loadTouchPoints()
        loadAutoReplyScript()
    }

    /** 加载触点列表 */
    fun loadTouchPoints() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val points = repo.getTouchPoints()
                _uiState.update { it.copy(touchPoints = points, isLoading = false) }
                _events.emit(TouchPointEvent.LoadSuccess)
            } catch (e: Exception) {
                _events.emit(TouchPointEvent.LoadFailed("加载失败: ${e.message}"))
            }
        }
    }

    /** 添加触点 */
    fun addTouchPoint(name: String, x: Int, y: Int, delay: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val point = TouchPoint(name = name, x = x, y = y, delay = delay)
            repo.addTouchPoint(point)
            loadTouchPoints()
            _events.emit(TouchPointEvent.TouchPointAdded)
        }
    }

    /** 删除触点 */
    fun deleteTouchPoint(position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteTouchPoint(position)
            loadTouchPoints()
            _events.emit(TouchPointEvent.TouchPointDeleted)
        }
    }

    /** 更新触点点击开关 */
    fun toggleTouchPointClick(position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value.touchPoints
            if (position in current.indices) {
                val newValue = !current[position].isStartClick
                repo.updateTouchPointClick(position, newValue)
                loadTouchPoints()
            }
        }
    }

    /** 加载自动回复脚本 */
    private fun loadAutoReplyScript() {
        _uiState.update { it.copy(autoReplyScript = repo.getAutoReplyScript()) }
    }

    /** 保存自动回复脚本 */
    fun saveAutoReplyScript(script: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.setAutoReplyScript(script)
            _uiState.update { it.copy(autoReplyScript = script, showAutoReplyDialog = false) }
            _events.emit(TouchPointEvent.ScriptSaved)
        }
    }

    fun showAutoReplyDialog() {
        _uiState.update { it.copy(showAutoReplyDialog = true) }
    }

    fun dismissAutoReplyDialog() {
        _uiState.update { it.copy(showAutoReplyDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
