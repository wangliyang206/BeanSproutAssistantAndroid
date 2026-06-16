package com.wly.beansprout.feature.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wly.beansprout.data.repository.RegisterRepository
import com.wly.beansprout.feature.register.ui.RegisterEvent
import com.wly.beansprout.feature.register.ui.RegisterFormValidation
import com.wly.beansprout.feature.register.ui.RegisterUiState
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
class RegisterViewModel @Inject constructor(
    private val registerRepository: RegisterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RegisterEvent>()
    val events = _events.asSharedFlow()

    /**
     * 更新手机号
     */
    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone.trim(), hasStartedPhoneInput = true) }
    }

    /**
     * 更新密码
     */
    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, hasStartedPasswordInput = true) }
    }

    /**
     * 更新确认密码
     */
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update {
            it.copy(confirmPassword = confirmPassword, hasStartedConfirmPasswordInput = true)
        }
    }

    /**
     * 切换密码可见性
     */
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * 切换确认密码可见性
     */
    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    /**
     * 切换同意协议状态
     */
    fun setAgreeProtocol(isAgree: Boolean) {
        _uiState.update { it.copy(isAgreeProtocol = isAgree) }
    }

    /**
     * 执行注册操作
     */
    fun performRegister() {
        val currentState = _uiState.value

        // 1. 验证表单
        val validation = validateRegisterForm(currentState)
        if (!validation.isFormValid) {
            _uiState.update {
                it.copy(errorMessage = getValidationErrorMessage(validation))
            }
            return
        }

        // 2. 检查协议勾选
        if (!currentState.isAgreeProtocol) {
            _uiState.update { it.copy(errorMessage = "请阅读并同意相关协议") }
            return
        }

        // 3. 清除错误信息并开始加载
        _uiState.update { it.copy(errorMessage = null, isLoading = true) }

        // 4. 发起注册请求
        viewModelScope.launch {
            try {
                registerRepository.register(
                    mobile = currentState.phoneNumber,
                    password = currentState.password
                )
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(RegisterEvent.RegisterSuccess)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "注册失败，请稍后重试"
                    )
                }
                _events.emit(RegisterEvent.RegisterFailed(e.message ?: "注册失败"))
            }
        }
    }

    /**
     * 验证注册表单
     */
    private fun validateRegisterForm(state: RegisterUiState): RegisterFormValidation {
        var isPhoneValid = true
        var phoneErrorMessage: String? = null
        var isPasswordValid = true
        var passwordErrorMessage: String? = null
        var isConfirmPasswordValid = true
        var confirmPasswordErrorMessage: String? = null

        // 验证手机号
        if (state.phoneNumber.isBlank()) {
            isPhoneValid = false
            phoneErrorMessage = "您输入的账号不能为空！"
        } else if (!state.isPhoneNumberValid) {
            isPhoneValid = false
            phoneErrorMessage = "请输入有效的手机号！"
        }

        // 验证密码
        if (state.password.isBlank()) {
            isPasswordValid = false
            passwordErrorMessage = "您输入的密码不能为空！"
        } else if (state.password.length < 6 || state.password.length > 20) {
            isPasswordValid = false
            passwordErrorMessage = "密码长度为6–20位，建议字母与数字组合"
        }

        // 验证确认密码
        if (state.confirmPassword.isBlank()) {
            isConfirmPasswordValid = false
            confirmPasswordErrorMessage = "您输入的确认密码不能为空！"
        } else if (state.confirmPassword != state.password) {
            isConfirmPasswordValid = false
            confirmPasswordErrorMessage = "请确保确认密码与密码输入一致！"
        }

        return RegisterFormValidation(
            isPhoneValid = isPhoneValid,
            phoneErrorMessage = phoneErrorMessage,
            isPasswordValid = isPasswordValid,
            passwordErrorMessage = passwordErrorMessage,
            isConfirmPasswordValid = isConfirmPasswordValid,
            confirmPasswordErrorMessage = confirmPasswordErrorMessage,
            isFormValid = isPhoneValid && isPasswordValid && isConfirmPasswordValid
        )
    }

    /**
     * 获取验证错误信息
     */
    private fun getValidationErrorMessage(validation: RegisterFormValidation): String {
        return listOfNotNull(
            validation.phoneErrorMessage,
            validation.passwordErrorMessage,
            validation.confirmPasswordErrorMessage
        ).joinToString("\n")
    }

    /**
     * 导航返回
     */
    fun navigateBack() {
        viewModelScope.launch {
            _events.emit(RegisterEvent.NavigateBack)
        }
    }

    /**
     * 导航到服务协议
     */
    fun navigateToServiceAgreement() {
        viewModelScope.launch {
            _events.emit(RegisterEvent.NavigateToServiceAgreement)
        }
    }

    /**
     * 导航到隐私协议
     */
    fun navigateToPrivacyAgreement() {
        viewModelScope.launch {
            _events.emit(RegisterEvent.NavigateToPrivacyAgreement)
        }
    }

    /**
     * 重置错误信息
     */
    fun resetErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
