package com.wly.beansprout.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wly.beansprout.feature.home.ui.HomeScreen
import com.wly.beansprout.feature.login.ui.LoginScreen
import com.wly.beansprout.feature.register.ui.RegisterScreen
import com.wly.beansprout.feature.splash.ui.SplashScreen

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
    }
}