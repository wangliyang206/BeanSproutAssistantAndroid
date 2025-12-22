package com.wly.beansprout.presentation.navigation

/**
 * 路由常量
 */
sealed class NavRoutes(val route: String) {
    // 闪页
    object Splash : NavRoutes("splash")
    // 登录
    object Login : NavRoutes("login")
    // 注册
    object Register : NavRoutes("register")
    // 首页
    object Home : NavRoutes("home")
}