package com.wly.beansprout.core.base

/**
 * UI 状态基类（Loading/Success/Error）
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>() // 初始状态
    object Loading : UiState<Nothing>() // 加载中
    data class Success<out T>(val data: T) : UiState<T>() // 成功
    data class Error(val message: String) : UiState<Nothing>() // 失败
}