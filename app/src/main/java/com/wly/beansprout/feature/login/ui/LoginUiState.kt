package com.wly.beansprout.feature.login.ui

import androidx.compose.runtime.*

/**
 * 登录界面 UI 状态
 * @property phoneNumber 手机号
 * @property password 密码
 * @property isPasswordVisible 密码是否可见
 * @property isAgreeProtocol 是否同意协议
 * @property showExitDialog 是否显示退出对话框
 * @property isLoading 是否正在加载
 * @property errorMessage 错误信息
 */
data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isAgreeProtocol: Boolean = false,
    val showExitDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * 登录按钮是否可用
     */
    val isLoginEnabled: Boolean
        get() = phoneNumber.isNotBlank() &&
                password.isNotBlank() &&
                isAgreeProtocol &&
                !isLoading

    /**
     * 手机号是否有效（简单验证）
     */
    val isPhoneNumberValid: Boolean
        get() = phoneNumber.matches(Regex("^1[3-9]\\d{9}\$"))

    /**
     * 密码是否有效（简单验证，至少6位）
     */
    val isPasswordValid: Boolean
        get() = password.length >= 6
}

/**
 * 登录相关事件
 */
sealed class LoginEvent {
    data class LoginSuccess(val userInfo: String) : LoginEvent()
    data class LoginFailed(val error: String) : LoginEvent()
    object NavigateToRegister : LoginEvent()
    object NavigateToServiceAgreement : LoginEvent()
    object NavigateToPrivacyAgreement : LoginEvent()
    object ExitApp : LoginEvent()
}

/**
 * 登录表单验证结果
 */
data class LoginFormValidation(
    val isPhoneValid: Boolean = true,
    val phoneErrorMessage: String? = null,
    val isPasswordValid: Boolean = true,
    val passwordErrorMessage: String? = null,
    val isFormValid: Boolean = true
)