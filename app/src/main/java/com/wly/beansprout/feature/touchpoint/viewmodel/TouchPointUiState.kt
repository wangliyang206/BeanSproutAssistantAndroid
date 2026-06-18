package com.wly.beansprout.feature.touchpoint.viewmodel

import com.wly.beansprout.data.model.TouchPoint

/**
 * 触点管理页面状态
 */
data class TouchPointUiState(
    val touchPoints: List<TouchPoint> = emptyList(),
    val isLoading: Boolean = false,
    val autoReplyScript: String = "",
    val showAutoReplyDialog: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 触点管理事件
 */
sealed class TouchPointEvent {
    object LoadSuccess : TouchPointEvent()
    data class LoadFailed(val message: String) : TouchPointEvent()
    object TouchPointAdded : TouchPointEvent()
    object TouchPointDeleted : TouchPointEvent()
    object ScriptSaved : TouchPointEvent()
}
