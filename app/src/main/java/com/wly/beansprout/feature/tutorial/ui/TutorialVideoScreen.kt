package com.wly.beansprout.feature.tutorial.ui

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.wly.beansprout.R

/**
 * 教程视频播放页面
 *
 * 使用 AndroidView 包裹 VideoView 实现全屏视频播放，
 * 支持 MediaController（播放/暂停/进度条控制）。
 */
@Composable
fun TutorialVideoScreen(navController: NavController) {
    val context = LocalContext.current
    val videoViewRef = remember { arrayOf<VideoView?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    val uri = Uri.parse(
                        "android.resource://${ctx.packageName}/${R.raw.tutorial_video}"
                    )
                    setVideoURI(uri)
                    setMediaController(MediaController(ctx))
                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        start()
                    }
                    setOnErrorListener { _, what, extra ->
                        // 播放出错时不崩溃，静默处理
                        true
                    }
                    videoViewRef[0] = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // 返回键处理：先停止视频再返回
    BackHandler {
        videoViewRef[0]?.stopPlayback()
        navController.popBackStack()
    }

    // 页面销毁时释放资源
    DisposableEffect(Unit) {
        onDispose {
            videoViewRef[0]?.stopPlayback()
        }
    }
}
