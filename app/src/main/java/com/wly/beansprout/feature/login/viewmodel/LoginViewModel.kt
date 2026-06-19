package com.wly.beansprout.feature.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wly.beansprout.core.utils.UMengManager
import com.wly.beansprout.data.repository.LoginRepository
import com.wly.beansprout.feature.login.ui.LoginEvent
import com.wly.beansprout.feature.login.ui.LoginFormValidation
import com.wly.beansprout.feature.login.ui.LoginUiState
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
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {
    // 私有状态，只能通过 ViewModel 更新
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 事件通道，用于发送一次性事件（如导航、弹窗等）
    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    /**
     * 更新手机号
     */
    fun updatePhoneNumber(phone: String) {
        _uiState.update { currentState ->
            currentState.copy(
                phoneNumber = phone.trim(),
                hasStartedInput = true
            )
        }
    }

    /**
     * 更新密码
     */
    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                hasStartedPasswordInput = true
            )
        }
    }

    /**
     * 切换密码可见性
     */
    fun togglePasswordVisibility() {
        _uiState.update { currentState ->
            currentState.copy(isPasswordVisible = !currentState.isPasswordVisible)
        }
    }

    /**
     * 切换同意协议状态
     */
    fun setAgreeProtocol(isAgree: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isAgreeProtocol = isAgree)
        }
    }

    /**
     * 显示/隐藏退出对话框
     */
    fun setExitDialogVisibility(show: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(showExitDialog = show)
        }
    }

    /**
     * 执行登录操作
     */
    fun performLogin() {
        val currentState = _uiState.value

        // 1. 验证表单
        val validation = validateLoginForm(currentState)
        if (!validation.isFormValid) {
            _uiState.update {
                it.copy(errorMessage = getValidationErrorMessage(validation))
            }
            return
        }

        // 2. 清除之前的错误信息
        _uiState.update { it.copy(errorMessage = null, isLoading = true) }

        // 3. 发起登录请求
        viewModelScope.launch {
            try {
                val result = loginRepository.login(
                    mobile = currentState.phoneNumber,
                    password = currentState.password
                )

                // 登录成功，发送事件
                _events.emit(LoginEvent.LoginSuccess(result.userName))
                // 友盟：用户登录
                UMengManager.onProfileSignIn(result.userId)
                // 重置加载状态
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "网络请求异常"
                    )
                }
                _events.emit(LoginEvent.LoginFailed(e.message ?: "网络请求异常"))
            }
        }
    }

    /**
     * 验证登录表单
     */
    private fun validateLoginForm(state: LoginUiState): LoginFormValidation {
        var isPhoneValid = true
        var phoneErrorMessage: String? = null
        var isPasswordValid = true
        var passwordErrorMessage: String? = null

        // 验证手机号
        if (state.phoneNumber.isBlank()) {
            isPhoneValid = false
            phoneErrorMessage = "请输入手机号"
        } else if (!state.isPhoneNumberValid) {
            isPhoneValid = false
            phoneErrorMessage = "请输入正确的手机号"
        }

        // 验证密码
        if (state.password.isBlank()) {
            isPasswordValid = false
            passwordErrorMessage = "请输入密码"
        } else if (!state.isPasswordValid) {
            isPasswordValid = false
            passwordErrorMessage = "密码至少6位"
        }

        return LoginFormValidation(
            isPhoneValid = isPhoneValid,
            phoneErrorMessage = phoneErrorMessage,
            isPasswordValid = isPasswordValid,
            passwordErrorMessage = passwordErrorMessage,
            isFormValid = isPhoneValid && isPasswordValid
        )
    }

    /**
     * 获取验证错误信息
     */
    private fun getValidationErrorMessage(validation: LoginFormValidation): String {
        return listOfNotNull(
            validation.phoneErrorMessage,
            validation.passwordErrorMessage
        ).joinToString("\n")
    }

    /**
     * 导航到注册页面
     */
    fun navigateToRegister() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToRegister)
        }
    }

    /**
     * 导航到服务协议
     */
    fun navigateToServiceAgreement() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToServiceAgreement)
        }
    }

    /**
     * 导航到隐私协议
     */
    fun navigateToPrivacyAgreement() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToPrivacyAgreement)
        }
    }

    /**
     * 退出应用
     */
    fun exitApp() {
        viewModelScope.launch {
            _events.emit(LoginEvent.ExitApp)
        }
    }

    /**
     * 重置错误信息
     */
    fun resetErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 清空表单
     */
    fun clearForm() {
        _uiState.update {
            LoginUiState(
                isAgreeProtocol = it.isAgreeProtocol, // 保留协议状态
                showExitDialog = it.showExitDialog // 保留对话框状态
            )
        }
    }
}