package com.wly.beansprout.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wly.beansprout.BuildConfig
import com.wly.beansprout.core.utils.StringUtils
import com.wly.beansprout.data.repository.LoginRepository
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
    private val loginRepository: LoginRepository
) : ViewModel() {
    // 私有状态
    private val _uiState = MutableStateFlow(
        HomeUiState(versionName = BuildConfig.VERSION_NAME)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 事件通道
    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    init {
        // 初始化时从 DataStore 加载用户信息
        loadUserInfo()
    }

    /**
     * 从 DataStore 加载用户信息
     */
    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val userInfo = loginRepository.getUserInfo()
                _uiState.update {
                    it.copy(
                        phoneNumber = userInfo.userPhone,
                        userType = StringUtils.userTypeText(userInfo.status, userInfo.daysRemaining)
                    )
                }
            } catch (e: Exception) {
                // 加载失败保持默认值
            }
        }
    }

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
     * 显示退出登录确认对话框
     */
    fun onLogoutClick() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    /**
     * 关闭退出登录对话框
     */
    fun dismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    /**
     * 确认退出登录：清除本地数据 → 导航到登录页
     */
    fun confirmLogout() {
        _uiState.update { it.copy(showLogoutDialog = false) }
        viewModelScope.launch {
            try {
                // 清除本地存储的登录信息
                loginRepository.logout()
                // 发送导航到登录页事件
                _events.emit(HomeEvent.NavigateToLogin)
            } catch (e: Exception) {
                _events.emit(HomeEvent.ShowToast("退出登录失败：${e.message}"))
            }
        }
    }

    /**
     * 导航到服务协议
     */
    fun navigateToServiceAgreement() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToServiceAgreement)
        }
    }

    /**
     * 导航到隐私政策
     */
    fun navigateToPrivacyPolicy() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToPrivacyPolicy)
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