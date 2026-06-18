package com.wly.beansprout

import android.content.Intent
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

        // 读取悬浮窗导航意图
        handleNavigationIntent(intent)

        // 将应用的内容延伸到屏幕的边缘，并隐藏或透明处理状态栏和导航栏。
        enableEdgeToEdge()

        setContent {
            AppNavGraph(splashScreen)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    /**
     * 处理来自悬浮窗菜单的导航意图
     */
    private fun handleNavigationIntent(intent: Intent?) {
        val navigateTo = intent?.getStringExtra(EXTRA_NAVIGATE_TO)
        if (navigateTo != null) {
            pendingNavRoute = navigateTo
            intent.removeExtra(EXTRA_NAVIGATE_TO)
        }
    }

    // 暴露退出应用的方法
    fun exitApp() {
        finishAffinity()
        Process.killProcess(Process.myPid())
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val NAV_ADD_TOUCH_POINT = "add_touch_point"

        /**
         * 来自悬浮窗的待处理导航路由。
         * AppNavGraph 在 Home 页就绪时消费此值。
         */
        var pendingNavRoute: String? = null
    }
}