package com.wly.beansprout.feature.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 界面布局组合
 */
@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleAgreeProtocol: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onServiceAgreementClick: () -> Unit,
    onPrivacyAgreementClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部关闭按钮
            LoginTopBar(onCloseClick = onCloseClick)

            Spacer(modifier = Modifier.height(40.dp))

            // 欢迎标题
            LoginWelcomeHeader()

            // 输入区域
            LoginInputSection(
                phoneNumber = uiState.phoneNumber,
                onPhoneNumberChange = onPhoneNumberChange,
                password = uiState.password,
                onPasswordChange = onPasswordChange,
                isPasswordVisible = uiState.isPasswordVisible,
                onPasswordVisibilityToggle = onTogglePasswordVisibility,
                isPhoneValid = uiState.isPhoneNumberValid,
                isPasswordValid = uiState.isPasswordValid
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 登录按钮
            LoginButton(
                isEnabled = uiState.isLoginEnabled,
                isLoading = uiState.isLoading,
                onClick = onLoginClick
            )

            // 协议选择
            AgreementSection(
                isAgreeProtocol = uiState.isAgreeProtocol,
                onAgreeProtocolChange = onToggleAgreeProtocol,
                onServiceAgreementClick = onServiceAgreementClick,
                onPrivacyAgreementClick = onPrivacyAgreementClick
            )

            Spacer(modifier = Modifier.weight(1f))

            // 注册链接
            RegisterLink(onClick = onRegisterClick)
        }
    }
}