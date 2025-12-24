package com.wly.beansprout.feature.splash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wly.beansprout.feature.splash.viewmodel.SplashViewModel
import com.wly.beansprout.presentation.navigation.NavRoutes
import kotlinx.coroutines.delay

/**
 * 闪页
 */
@Composable
fun SplashScreen(
    navController: NavController,
    splashScreen: SplashScreen
) {
    // 使用 hiltViewModel() 获取 ViewModel，支持依赖注入
    val viewModel: SplashViewModel = hiltViewModel()

    // 启动数据加载任务
    LaunchedEffect(Unit) {
        viewModel.startLoadingTasks()
    }

    // 设置闪屏保持条件（核心逻辑）
    splashScreen.setKeepOnScreenCondition {
        viewModel.isLoading // 直接访问 ViewModel 的状态
    }

    // 闪屏退出动画
    splashScreen.setOnExitAnimationListener { splashScreenView ->
        // 示例：图标缩放动画
        val icon = splashScreenView.iconView
        icon.animate()
            ?.setDuration(500)
            ?.scaleX(0.5f)
            ?.scaleY(0.5f)
            ?.alpha(0f)
            ?.withEndAction {
                splashScreenView.remove() // 必须调用
            }
            ?.start()
    }

    // 导航逻辑
    LaunchedEffect(viewModel.navigateToHome) {
        // 等待动画完成后再导航
        if (viewModel.navigateToHome != null) {
            // 等待一小段时间，确保闪屏动画完成
            delay(500)

            when (viewModel.navigateToHome) {
                // 跳转到首页
                true -> navController.navigate(NavRoutes.Home.route) {
                    // 清除返回栈中的闪屏页面，避免用户按返回键回到闪屏
                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                }
                // 跳转到登录页
                false -> navController.navigate(NavRoutes.Login.route) {
                    // 清除返回栈中的闪屏页面
                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                }
                null -> Unit
            }
        }
    }
}