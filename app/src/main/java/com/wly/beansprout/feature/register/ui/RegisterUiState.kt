package com.wly.beansprout.feature.register.ui

/**
 * 注册界面 UI 状态
 */
data class RegisterUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isAgreeProtocol: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasStartedPhoneInput: Boolean = false,
    val hasStartedPasswordInput: Boolean = false,
    val hasStartedConfirmPasswordInput: Boolean = false
) {
    /**
     * 注册按钮是否可用
     */
    val isRegisterEnabled: Boolean
        get() = phoneNumber.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                isAgreeProtocol &&
                !isLoading

    /**
     * 手机号是否有效
     */
    val isPhoneNumberValid: Boolean
        get() = if (!hasStartedPhoneInput || phoneNumber.isBlank()) {
            true
        } else {
            phoneNumber.matches(Regex("^1[3-9]\\d{9}\$"))
        }

    /**
     * 密码是否有效（6-20位）
     */
    val isPasswordValid: Boolean
        get() = if (!hasStartedPasswordInput || password.isBlank()) {
            true
        } else {
            password.length in 6..20
        }

    /**
     * 确认密码是否有效（与密码一致）
     */
    val isConfirmPasswordValid: Boolean
        get() = if (!hasStartedConfirmPasswordInput || confirmPassword.isBlank()) {
            true
        } else {
            confirmPassword == password
        }
}

/**
 * 注册相关事件
 */
sealed class RegisterEvent {
    /** 注册成功 */
    object RegisterSuccess : RegisterEvent()
    /** 注册失败 */
    data class RegisterFailed(val error: String) : RegisterEvent()
    /** 返回上一页 */
    object NavigateBack : RegisterEvent()
    /** 导航到服务协议 */
    object NavigateToServiceAgreement : RegisterEvent()
    /** 导航到隐私协议 */
    object NavigateToPrivacyAgreement : RegisterEvent()
}

/**
 * 注册表单验证结果
 */
data class RegisterFormValidation(
    val isPhoneValid: Boolean = true,
    val phoneErrorMessage: String? = null,
    val isPasswordValid: Boolean = true,
    val passwordErrorMessage: String? = null,
    val isConfirmPasswordValid: Boolean = true,
    val confirmPasswordErrorMessage: String? = null,
    val isFormValid: Boolean = true
)
