package com.wly.beansprout.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wly.beansprout.BuildConfig
import com.wly.beansprout.feature.home.ui.HomeEvent
import com.wly.beansprout.feature.home.ui.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(

): ViewModel() {
    // 私有状态
    private val _uiState = MutableStateFlow(
        HomeUiState(versionName = BuildConfig.VERSION_NAME)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 事件通道
    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    // 回退键处理
    private var lastBackPressTime = 0L

    /**
     * 更新专属平台选择
     */
    fun updateSelectedExclusive(exclusive: Int) {
        _uiState.update { it.copy(selectedExclusive = exclusive) }
    }

    /**
     * 更新功能选择
     */
    fun updateSelectedFunction(functionIndex: Int) {
        _uiState.update { it.copy(selectedFunction = functionIndex) }
    }

    /**
     * 更新模型选择
     */
    fun updateSelectedModel(model: Int) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    /**
     * 处理开始按钮点击
     */
    fun onStartClick() {
        viewModelScope.launch {
            _events.emit(HomeEvent.ShowStartDialog)
            // 这里可以启动后台服务或执行其他逻辑
        }
    }

    /**
     * 处理退出登录
     */
    fun onLogoutClick() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToLogin)
        }
    }

    /**
     * 处理返回键
     */
    fun onBackPressed(): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastBackPressTime < 2000) {
            viewModelScope.launch {
                _events.emit(HomeEvent.ShowExitAppDialog)
            }
            true
        } else {
            lastBackPressTime = currentTime
            viewModelScope.launch {
                _events.emit(HomeEvent.ShowToast("再按一次返回键退出应用"))
            }
            true
        }
    }

    /**
     * 重置所有设置
     */
    fun resetAllSettings() {
        _uiState.update {
            HomeUiState(
                phoneNumber = it.phoneNumber,
                userType = it.userType,
                versionName = it.versionName
            )
        }
    }
}