package com.wly.beansprout.feature.tutorial.ui

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/** 播放状态 */
private enum class PlaybackState { LOADING, PLAYING, ERROR }

/**
 * 全屏 VideoView —— 重写 onMeasure 让视频铺满父容器，不留黑边
 */
private class FullScreenVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : VideoView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}

/**
 * 视频播放页
 *
 * 支持两种播放模式：
 * - isLocal=true: 从 res/raw 加载本地视频（android.resource:// URI）
 * - isLocal=false: 从 http/https URL 加载网络视频
 */
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    title: String,
    videoUrl: String,
    isLocal: Boolean = true
) {
    val decodedTitle = remember(title) {
        URLDecoder.decode(title, "UTF-8")
    }
    val decodedUrl = remember(videoUrl) {
        URLDecoder.decode(videoUrl, "UTF-8")
    }
    val context = LocalContext.current
    val videoViewRef = remember { arrayOf<VideoView?>(null) }
    var playbackState by remember { mutableStateOf(PlaybackState.LOADING) }
    var errorDetail by remember { mutableStateOf("") }

    // 构建本地视频的 android.resource:// URI
    val localVideoUri = remember(decodedUrl) {
        if (isLocal) {
            val resId = context.resources.getIdentifier(
                decodedUrl, "raw", context.packageName
            )
            if (resId != 0) {
                Uri.parse("android.resource://${context.packageName}/$resId")
            } else {
                null
            }
        } else {
            null
        }
    }

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
            // 视频播放区域（铺满剩余空间）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {
                if (isLocal && localVideoUri == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "视频文件未找到\n请将 $decodedUrl.mp4 放入 res/raw/ 目录",
                            fontSize = 14.sp,
                            color = Color(0xFFE53935)
                        )
                    }
                } else {
                    AndroidView(
                        factory = { ctx ->
                            FullScreenVideoView(ctx).apply {
                                if (isLocal) {
                                    setVideoURI(localVideoUri)
                                } else {
                                    setVideoURI(Uri.parse(decodedUrl))
                                }
                                setMediaController(MediaController(ctx))
                                setOnPreparedListener { mp ->
                                    mp.isLooping = false
                                    mp.start()
                                    playbackState = PlaybackState.PLAYING
                                }
                                setOnErrorListener { _, what, extra ->
                                    playbackState = PlaybackState.ERROR
                                    errorDetail = "error=$what, extra=$extra"
                                    true
                                }
                                videoViewRef[0] = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 底部状态栏 —— 仅加载中或出错时显示，播放时隐藏
            if (playbackState != PlaybackState.PLAYING) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val (text, color) = when (playbackState) {
                        PlaybackState.LOADING -> "加载中，请稍候..." to Color(0xFF999999)
                        PlaybackState.ERROR -> "播放出错 ($errorDetail)" to Color(0xFFE53935)
                        else -> "" to Color.Transparent
                    }
                    Text(
                        text = text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = color
                    )
                }
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
