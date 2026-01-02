package com.wly.beansprout.feature.login.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wly.beansprout.feature.login.viewmodel.LoginViewModel
import com.wly.beansprout.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest

/**
 * 登录页-入口
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // 使用 collectAsState 将 StateFlow 转换为 Compose 状态
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 处理 ViewModel 发出的事件
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> {
                    // 登录成功后的处理，例如导航到主页
                    navController.navigate(NavRoutes.Home.route) {
                        // 将Login页面从返回栈中弹出（包含Login页面本身）。这样用户无法通过返回键回到登录页面，通常用于登录成功后跳转到主页的场景
                        popUpTo(NavRoutes.Login.route) {
                            inclusive = true
                        }
                    }
                }

                is LoginEvent.LoginFailed -> {
                    // 登录失败，错误信息已在 UI 状态中更新
                    // 可以在这里添加额外的处理，如显示 Toast
                }

                is LoginEvent.NavigateToRegister -> {
                    navController.navigate(NavRoutes.Register.route)
                }

                is LoginEvent.NavigateToServiceAgreement -> {
//                    navController.navigate(NavRoutes.ServiceAgreement.route)
                }

                is LoginEvent.NavigateToPrivacyAgreement -> {
//                    navController.navigate(NavRoutes.PrivacyAgreement.route)
                }

                is LoginEvent.ExitApp -> {
                    val activity = context as? Activity
                    activity?.finishAffinity()
                }
            }
        }
    }

    // 主内容
    LoginScreenContent(
        uiState = uiState,
        onPhoneNumberChange = viewModel::updatePhoneNumber,
        onPasswordChange = viewModel::updatePassword,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onToggleAgreeProtocol = viewModel::setAgreeProtocol,
        onLoginClick = viewModel::performLogin,
        onRegisterClick = viewModel::navigateToRegister,
        onServiceAgreementClick = viewModel::navigateToServiceAgreement,
        onPrivacyAgreementClick = viewModel::navigateToPrivacyAgreement,
        onCloseClick = { viewModel.setExitDialogVisibility(true) }
    )

    // 退出确认对话框
    if (uiState.showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                viewModel.exitApp()
                viewModel.setExitDialogVisibility(false)
            },
            onDismiss = { viewModel.setExitDialogVisibility(false) }
        )
    }

    // 错误信息对话框
    uiState.errorMessage?.let { errorMessage ->
        ErrorMessageDialog(
            errorMessage = errorMessage,
            onDismiss = viewModel::resetErrorMessage
        )
    }

    // 加载指示器
    if (uiState.isLoading) {
        LoadingDialog()
    }

    // 拦截物理返回键
    BackHandler(
        enabled = navController.currentBackStackEntry?.destination?.route == NavRoutes.Login.route,
        onBack = { viewModel.setExitDialogVisibility(true) }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    val navController = rememberNavController()
    LoginScreen(navController)
}