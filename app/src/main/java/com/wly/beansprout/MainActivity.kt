package com.wly.beansprout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wly.beansprout.presentation.navigation.AppNavGraph

/**
 * APP入口
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate 之前安装
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            AppNavGraph(splashScreen)
        }
    }
}