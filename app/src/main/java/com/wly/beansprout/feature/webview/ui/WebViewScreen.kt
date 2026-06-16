package com.wly.beansprout.feature.webview.ui

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.wly.beansprout.presentation.CommTopBar

/**
 * WebView 协议页
 *
 * 用于显示服务协议、隐私政策等网页内容
 */
@Composable
fun WebViewScreen(
    navController: NavController,
    title: String,
    url: String
) {
    CommTopBar(
        title = title,
        onBack = { navController.popBackStack() },
        content = { modifier ->
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                // 使用 AndroidView 嵌入原生 WebView
                androidx.compose.ui.viewinterop.AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            // WebView 配置
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                setSupportZoom(true)
                                builtInZoomControls = false
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                            }

                            // 页面加载回调
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    url: String
                                ): Boolean {
                                    // 在当前 WebView 中加载链接
                                    view.loadUrl(url)
                                    return true
                                }
                            }

                            // 进度条支持
                            webChromeClient = WebChromeClient()

                            // 加载目标 URL
                            loadUrl(url)
                        }
                    }
                )
            }
        }
    )
}
