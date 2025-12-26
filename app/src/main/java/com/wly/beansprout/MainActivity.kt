package com.wly.beansprout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wly.beansprout.presentation.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import android.os.Process

/**
 * APP入口
 */
// 添加 hit 注解
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate 之前安装
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 将应用的内容延伸到屏幕的边缘，并隐藏或透明处理状态栏和导航栏。
        enableEdgeToEdge()

        setContent {
            AppNavGraph(splashScreen)
        }
    }

    // 暴露退出应用的方法
    fun exitApp() {
        finishAffinity()
        Process.killProcess(Process.myPid())
    }
}