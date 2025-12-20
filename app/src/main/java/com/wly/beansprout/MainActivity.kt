package com.wly.beansprout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import com.wly.beansprout.feature.home.viewmodel.SplashViewModel
import com.wly.beansprout.presentation.MainLayout

class MainActivity : ComponentActivity() {
    private val viewModel: SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate 之前安装
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 将应用的内容延伸到屏幕的边缘，并隐藏或透明处理状态栏和导航栏。
        enableEdgeToEdge()

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
        setContent {
            MainLayout()
            // 主界面恢复为白色字体
            DisposableEffect(Unit) {
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.isAppearanceLightStatusBars = false
                onDispose { }
            }
        }
    }
}

/** 预览 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainLayout()
}