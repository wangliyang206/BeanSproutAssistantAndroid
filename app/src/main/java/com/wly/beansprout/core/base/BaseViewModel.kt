package com.wly.beansprout.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel 基类（封装加载状态、Flow 收集）
 */
open class BaseViewModel : ViewModel() {
    // 通用加载状态（可复用）
    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    protected fun <T> launchRequest(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit = { }
    ) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                val result = block()
                onSuccess(result)
            } catch (e: Exception) {
                onError(e.message ?: "操作失败")
            } finally {
                _loadingState.value = false
            }
        }
    }

    // 重置加载状态
    fun resetLoadingState() {
        _loadingState.value = false
    }
}