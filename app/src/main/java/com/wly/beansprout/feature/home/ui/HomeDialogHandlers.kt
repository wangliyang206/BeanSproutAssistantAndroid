package com.wly.beansprout.feature.home.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.wly.beansprout.core.utils.ToastUtils.showToast
import com.wly.beansprout.feature.home.viewmodel.HomeViewModel
import com.wly.beansprout.presentation.dialog.CommonDialog
import com.wly.beansprout.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeDialogHandlers(
    uiState: HomeUiState,
    navController: NavController,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current

    // 处理 ViewModel 事件
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is HomeEvent.NavigateToLogin -> {
                    // 导航到登录页面
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Home.route) {
                            inclusive = true
                        }
                    }
                }
                is HomeEvent.ShowExitAppDialog -> {
                    val activity = context as? Activity
                    activity?.finishAffinity()
                }
                is HomeEvent.ShowStartDialog -> {
                    // 显示开始对话框或启动服务
                    showToast(context, "开始执行任务")
                }
                is HomeEvent.ShowToast -> {
                    showToast(context, event.message)
                }
            }
        }
    }

    // 退出登录确认对话框
    if (uiState.showLogoutDialog) {
        CommonDialog(
            showDialog = true,
            onDismissRequest = { /* TODO */ },
            title = "退出登录",
            content = { Text("确定要退出登录吗？") },
            confirmText = "确定",
            cancelText = "取消",
            onConfirmClick = { /* TODO */ },
            onCancelClick = { /* TODO */ }
        )
    }

    // 开始确认对话框
    if (uiState.showStartDialog) {
        CommonDialog(
            showDialog = true,
            onDismissRequest = { /* TODO */ },
            title = "开始执行",
            content = { Text("确定开始执行任务吗？") },
            confirmText = "确定",
            cancelText = "取消",
            onConfirmClick = { /* TODO */ },
            onCancelClick = { /* TODO */ }
        )
    }
}

@Composable
fun HomeBackHandler(
    navController: NavController,
    onBackPress: () -> Boolean
) {
    BackHandler(
        enabled = navController.currentBackStackEntry?.destination?.route == NavRoutes.Home.route,
        onBack = {
            val handled = onBackPress()
            // 如果返回true表示已处理，不需要执行默认返回逻辑
            if (handled) {
                // 什么都不做，让ViewModel处理
            }
        }
    )
}