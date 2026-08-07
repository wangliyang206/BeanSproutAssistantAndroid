package com.wly.beansprout.feature.splash.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wly.beansprout.feature.splash.viewmodel.PrivacyDialogState
import com.wly.beansprout.feature.splash.viewmodel.SplashViewModel
import com.wly.beansprout.presentation.dialog.NotPrivacyPolicyDialog
import com.wly.beansprout.presentation.dialog.PrivacyPolicyDialog
import com.wly.beansprout.presentation.navigation.NavRoutes

/**
 * 闪页
 */
@Composable
fun SplashScreen(
    navController: NavController,
    splashScreen: SplashScreen
) {
    val viewModel: SplashViewModel = hiltViewModel()
    val context = LocalContext.current

    // 启动加载任务
    LaunchedEffect(Unit) {
        viewModel.startLoadingTasks()
    }

    // 设置闪屏保持条件
    splashScreen.setKeepOnScreenCondition {
        viewModel.isLoading
    }

    // 隐私政策弹窗
    when (viewModel.privacyDialogState) {
        PrivacyDialogState.FIRST -> {
            PrivacyPolicyDialog(
                onAgree = { viewModel.onPrivacyAgreed() },
                onDisagree = { viewModel.onPrivacyDisagreeFirst() },
                onServiceAgreementClick = {
                    // 闪屏阶段无法使用 NavGraph，用系统浏览器打开协议
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(NavRoutes.WebView.SERVICE_AGREEMENT_URL)
                    )
                    context.startActivity(intent)
                },
                onPrivacyPolicyClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(NavRoutes.WebView.PRIVACY_POLICY_URL)
                    )
                    context.startActivity(intent)
                }
            )
        }

        PrivacyDialogState.SECOND -> {
            NotPrivacyPolicyDialog(
                onAgree = { viewModel.onPrivacyAgreed() },
                onDisagree = { viewModel.onPrivacyDisagreeSecond() }
            )
        }

        null -> { /* 不显示弹窗 */ }
    }

    // 两次不同意隐私政策 → 退出 App
    LaunchedEffect(viewModel.shouldExitApp) {
        if (viewModel.shouldExitApp) {
            val activity = context as? Activity
            activity?.finishAffinity()
        }
    }

    // 导航逻辑：先导航，再处理闪屏退出动画
    // 注意：不能把导航放在 setOnExitAnimationListener 回调里，
    // 因为在部分设备（如小米 HyperOS / Android 15）上，
    // 系统可能在监听器注册前就已移除闪屏，导致回调永不触发 → 白屏
    LaunchedEffect(viewModel.navigateToHome) {
        if (viewModel.navigateToHome != null && !viewModel.isLoading) {
            // 1. 先执行导航，确保 UI 立即切换到目标页面
            navigateBasedOnCondition(navController, viewModel.navigateToHome)

            // 2. 再设置闪屏退出动画（仅做视觉过渡，不影响导航逻辑）
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                val icon = splashScreenView.iconView
                icon.animate()
                    ?.setDuration(500)
                    ?.scaleX(0.5f)
                    ?.scaleY(0.5f)
                    ?.alpha(0f)
                    ?.withEndAction {
                        splashScreenView.remove()
                    }
                    ?.start()
            }
        }
    }
}

fun navigateBasedOnCondition(navController: NavController, navigateToHome: Boolean?) {
    when (navigateToHome) {
        true -> navController.navigate(NavRoutes.Home.route) {
            popUpTo(NavRoutes.Splash.route) { inclusive = true }
        }
        false -> navController.navigate(NavRoutes.Login.route) {
            popUpTo(NavRoutes.Splash.route) { inclusive = true }
        }
        null -> Unit
    }
}
