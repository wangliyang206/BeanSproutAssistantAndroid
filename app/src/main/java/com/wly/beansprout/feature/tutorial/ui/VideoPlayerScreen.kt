package com.wly.beansprout.feature.tutorial.ui

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.wly.beansprout.presentation.CommTopBar
import java.net.URLDecoder

/**
 * 网络视频播放页
 *
 * 接收导航参数中的视频 URL，使用 VideoView + MediaController 播放。
 * VideoView 原生支持 http/https 网络 URL，无需额外依赖。
 */
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    title: String,
    videoUrl: String
) {
    val decodedTitle = remember(title) {
        URLDecoder.decode(title, "UTF-8")
    }
    val decodedUrl = remember(videoUrl) {
        URLDecoder.decode(videoUrl, "UTF-8")
    }
    val videoViewRef = remember { arrayOf<VideoView?>(null) }

    CommTopBar(
        title = decodedTitle,
        onBack = {
            videoViewRef[0]?.stopPlayback()
            navController.popBackStack()
        }
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 视频播放区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(Uri.parse(decodedUrl))
                            setMediaController(MediaController(ctx))
                            setOnPreparedListener { mp ->
                                mp.isLooping = false
                                start()
                            }
                            setOnErrorListener { _, _, _ ->
                                true
                            }
                            videoViewRef[0] = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 底部提示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "加载中，请稍候...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF999999)
                )
            }
        }
    }

    BackHandler {
        videoViewRef[0]?.stopPlayback()
        navController.popBackStack()
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef[0]?.stopPlayback()
        }
    }
}
