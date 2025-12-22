package com.wly.beansprout.feature.splash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.splashscreen.SplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wly.beansprout.feature.splash.viewmodel.SplashViewModel

/**
 * 闪页
 */
@Composable
fun SplashScreen(
    navController: NavController,
    splashScreen: SplashScreen
) {
    // 获取 ViewModel
    val viewModel: SplashViewModel = viewModel()

    // 启动数据加载任务
    viewModel.startLoadingTasks()
    // 设置闪屏保持条件（核心逻辑）
    splashScreen.setKeepOnScreenCondition {
        viewModel.isLoading // 直接访问 ViewModel 的状态
    }
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
}