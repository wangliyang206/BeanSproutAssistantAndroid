package com.wly.beansprout.presentation.navigation

import java.net.URLEncoder

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

    // WebView 协议页（支持 URL + 标题参数）
    object WebView : NavRoutes("webview/{title}/{url}") {
        // 服务协议 URL
        const val SERVICE_AGREEMENT_URL = "http://47.115.223.27/privacypolicy.html"
        // 隐私政策 URL
        const val PRIVACY_POLICY_URL = "http://47.115.223.27/privacypolicy.html"

        /**
         * 构建带参数的导航路由
         * @param title 页面标题
         * @param url 要加载的网页地址
         */
        fun withArgs(title: String, url: String): String {
            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            return "webview/$encodedTitle/$encodedUrl"
        }
    }
}
