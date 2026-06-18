package com.wly.beansprout.feature.home.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.wly.beansprout.core.utils.ToastUtils.showToast
import com.wly.beansprout.core.utils.WindowUtils
import com.wly.beansprout.feature.floating.FloatingService
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
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Login.route) {
                            inclusive = true
                        }
                    }
                }

                is HomeEvent.NavigateToServiceAgreement -> {
                    navController.navigate(
                        NavRoutes.WebView.withArgs("服务协议", NavRoutes.WebView.SERVICE_AGREEMENT_URL)
                    )
                }

                is HomeEvent.NavigateToPrivacyPolicy -> {
                    navController.navigate(
                        NavRoutes.WebView.withArgs("隐私政策", NavRoutes.WebView.PRIVACY_POLICY_URL)
                    )
                }

                is HomeEvent.NavigateToTutorial -> {
                    navController.navigate(NavRoutes.TutorialVideo.route)
                }

                is HomeEvent.NavigateToAccessibilitySettings -> {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }

                is HomeEvent.NavigateToOverlaySettings -> {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }

                is HomeEvent.ShowExitAppDialog -> {
                    val activity = context as? Activity
                    activity?.finishAffinity()
                }

                is HomeEvent.ShowStartDialog -> {
                    // 已通过 state 管理，此事件保留兼容
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
            onDismissRequest = { viewModel.dismissLogoutDialog() },
            title = "退出登录",
            content = { Text("确定要退出登录吗？") },
            confirmText = "确定",
            onConfirmClick = { viewModel.confirmLogout() },
            cancelText = "取消",
            onCancelClick = { viewModel.dismissLogoutDialog() }
        )
    }

    // 开始确认对话框
    if (uiState.showStartDialog) {
        CommonDialog(
            showDialog = true,
            onDismissRequest = { viewModel.dismissStartDialog() },
            title = "开始执行",
            content = { Text("确定开始执行任务吗？") },
            confirmText = "确定",
            onConfirmClick = {
                val (functionType, chickModel, luckybagTime) = viewModel.confirmStart()
                startFloatingService(context, functionType, chickModel, luckybagTime)
            },
            cancelText = "取消",
            onCancelClick = { viewModel.dismissStartDialog() }
        )
    }

    // 无障碍服务引导弹窗
    if (uiState.showAccessibilityDialog) {
        CommonDialog(
            showDialog = true,
            onDismissRequest = { viewModel.dismissAccessibilityDialog() },
            title = "开启无障碍服务",
            content = { Text("需要开启无障碍服务才能使用本应用的核心功能，是否前往设置？") },
            confirmText = "前往设置",
            onConfirmClick = { viewModel.navigateToAccessibilitySettings() },
            cancelText = "取消",
            onCancelClick = { viewModel.dismissAccessibilityDialog() }
        )
    }

    // 悬浮窗权限引导弹窗
    if (uiState.showOverlayDialog) {
        CommonDialog(
            showDialog = true,
            onDismissRequest = { viewModel.dismissOverlayDialog() },
            title = "开启悬浮窗权限",
            content = { Text("需要开启悬浮窗权限才能显示悬浮窗小鸡，是否前往设置？") },
            confirmText = "前往设置",
            onConfirmClick = { viewModel.navigateToOverlaySettings() },
            cancelText = "取消",
            onCancelClick = { viewModel.dismissOverlayDialog() }
        )
    }
}

/**
 * 启动悬浮窗服务
 * 先检查悬浮窗权限，有权限则直接启动，否则跳转设置页
 */
private fun startFloatingService(
    context: android.content.Context,
    functionType: Int,
    chickModel: Int,
    luckybagTime: Int
) {
    // 检查悬浮窗权限
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
        showToast(context, "请先开启悬浮窗权限")
        // 跳转悬浮窗权限设置页
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return
    }

    // 检查服务是否已在运行，如果在运行则先停止
    if (WindowUtils.isServiceRunning(context, FloatingService::class.java.name)) {
        context.stopService(Intent(context, FloatingService::class.java))
    }

    // 启动悬浮窗服务
    val intent = Intent(context, FloatingService::class.java).apply {
        putExtra(FloatingService.EXTRA_FUNCTION_TYPE, functionType)
        putExtra(FloatingService.EXTRA_CHICK_MODEL, chickModel)
        putExtra(FloatingService.EXTRA_LUCKYBAG_TIME, luckybagTime)
    }
    // 启动悬浮窗服务（使用 startService 而非 startForegroundService，因为悬浮窗服务不需要前台通知）
    context.startService(intent)
    showToast(context, "悬浮窗已启动")
}

@Composable
fun HomeBackHandler(
    navController: NavController,
    onBackPress: () -> Boolean
) {
    BackHandler(
        enabled = navController.currentBackStackEntry?.destination?.route == NavRoutes.Home.route,
        onBack = { onBackPress() }
    )
}
