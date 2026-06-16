package com.wly.beansprout.feature.register.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wly.beansprout.core.utils.ToastUtils
import com.wly.beansprout.feature.register.viewmodel.RegisterViewModel
import com.wly.beansprout.presentation.dialog.CommonDialog
import kotlinx.coroutines.flow.collectLatest

/**
 * 注册页 - 入口
 */
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 处理 ViewModel 发出的事件
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is RegisterEvent.RegisterSuccess -> {
                    ToastUtils.showToast(context, "注册成功！")
                    // 注册成功后返回登录页
                    navController.popBackStack()
                }

                is RegisterEvent.RegisterFailed -> {
                    // 错误信息已在 UI 状态中更新
                }

                is RegisterEvent.NavigateBack -> {
                    navController.popBackStack()
                }

                is RegisterEvent.NavigateToServiceAgreement -> {
                    // TODO: Day 2 实现 WebView 协议页
                }

                is RegisterEvent.NavigateToPrivacyAgreement -> {
                    // TODO: Day 2 实现 WebView 协议页
                }
            }
        }
    }

    // 主内容
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部返回栏
            RegisterTopBar(onBackClick = { viewModel.navigateBack() })

            Spacer(modifier = Modifier.height(20.dp))

            // 欢迎标题
            RegisterWelcomeHeader()

            // 输入区域
            RegisterInputSection(
                phoneNumber = uiState.phoneNumber,
                onPhoneNumberChange = viewModel::updatePhoneNumber,
                password = uiState.password,
                onPasswordChange = viewModel::updatePassword,
                confirmPassword = uiState.confirmPassword,
                onConfirmPasswordChange = viewModel::updateConfirmPassword,
                isPasswordVisible = uiState.isPasswordVisible,
                onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
                isConfirmPasswordVisible = uiState.isConfirmPasswordVisible,
                onConfirmPasswordVisibilityToggle = viewModel::toggleConfirmPasswordVisibility,
                isPhoneValid = uiState.isPhoneNumberValid,
                isPasswordValid = uiState.isPasswordValid,
                isConfirmPasswordValid = uiState.isConfirmPasswordValid,
                hasStartedPhoneInput = uiState.hasStartedPhoneInput,
                hasStartedPasswordInput = uiState.hasStartedPasswordInput,
                hasStartedConfirmPasswordInput = uiState.hasStartedConfirmPasswordInput
            )

            // 注册按钮
            RegisterButton(
                isEnabled = uiState.isRegisterEnabled,
                isLoading = uiState.isLoading,
                onClick = viewModel::performRegister
            )

            // 协议勾选
            RegisterAgreementSection(
                isAgreeProtocol = uiState.isAgreeProtocol,
                onAgreeProtocolChange = viewModel::setAgreeProtocol,
                onServiceAgreementClick = viewModel::navigateToServiceAgreement,
                onPrivacyAgreementClick = viewModel::navigateToPrivacyAgreement
            )
        }
    }

    // 错误信息对话框
    uiState.errorMessage?.let { errorMessage ->
        CommonDialog(
            showDialog = true,
            onDismissRequest = viewModel::resetErrorMessage,
            title = "注册失败",
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        text = errorMessage,
                        color = androidx.compose.ui.graphics.Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmText = "确定",
            onConfirmClick = viewModel::resetErrorMessage,
            cancelText = null
        )
    }

    // 加载指示器
    if (uiState.isLoading) {
        CommonDialog(
            showDialog = true,
            onDismissRequest = { /* 加载中不可关闭 */ },
            title = "注册中",
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(48.dp),
                        color = com.wly.beansprout.presentation.theme.BtnColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Text("正在请求中，请稍候...")
                }
            },
            confirmText = null,
            cancelText = null,
            dismissible = false
        )
    }
}
