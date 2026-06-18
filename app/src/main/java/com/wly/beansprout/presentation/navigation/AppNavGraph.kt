package com.wly.beansprout.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wly.beansprout.feature.home.ui.HomeScreen
import com.wly.beansprout.feature.login.ui.LoginScreen
import com.wly.beansprout.feature.register.ui.RegisterScreen
import com.wly.beansprout.feature.splash.ui.SplashScreen
import com.wly.beansprout.feature.touchpoint.ui.AddTouchPointScreen
import com.wly.beansprout.feature.touchpoint.ui.TouchPointManageScreen
import com.wly.beansprout.feature.tutorial.ui.TutorialVideoScreen
import com.wly.beansprout.feature.webview.ui.WebViewScreen
import java.net.URLDecoder

/**
 * 全局导航图
 */
@Composable
fun AppNavGraph(
    splashScreen: SplashScreen
) {
    // 创建导航控制器
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route // 闪页为起始页
    ) {
        // 闪页
        composable(NavRoutes.Splash.route) {
            SplashScreen(navController, splashScreen)
        }

        // 登录页
        composable(NavRoutes.Login.route) {
            LoginScreen(navController)
        }

        // 注册页
        composable(NavRoutes.Register.route) {
            RegisterScreen(navController)
        }

        // 首页
        composable(NavRoutes.Home.route) {
            HomeScreen(navController)
        }

        // WebView 协议页
        composable(
            route = NavRoutes.WebView.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = URLDecoder.decode(
                backStackEntry.arguments?.getString("title") ?: "", "UTF-8"
            )
            val url = URLDecoder.decode(
                backStackEntry.arguments?.getString("url") ?: "", "UTF-8"
            )
            WebViewScreen(
                navController = navController,
                title = title,
                url = url
            )
        }

        // 触点管理
        composable(NavRoutes.TouchPointManage.route) {
            TouchPointManageScreen(navController)
        }

        // 添加触点
        composable(NavRoutes.AddTouchPoint.route) {
            AddTouchPointScreen(navController)
        }

        // 教程视频
        composable(NavRoutes.TutorialVideo.route) {
            TutorialVideoScreen(navController)
        }
    }
}
