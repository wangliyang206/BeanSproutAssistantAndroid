package com.wly.beansprout.presentation.dialog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wly.beansprout.core.utils.ApkDownloader
import com.wly.beansprout.data.model.AppUpdate

/**
 * 应用更新弹窗
 *
 * @param appUpdate 服务端返回的更新信息
 * @param downloadState 当前下载状态
 * @param onUpdateClick 用户点击"立即更新"
 * @param onDismiss 用户关闭弹窗（非强制更新时允许）
 */
@Composable
fun AppUpdateDialog(
    appUpdate: AppUpdate,
    downloadState: ApkDownloader.DownloadState,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isForceUpdate = appUpdate.appForce == 1
    val isDownloading = downloadState is ApkDownloader.DownloadState.Downloading
    val isCompleted = downloadState is ApkDownloader.DownloadState.Completed
    val isFailed = downloadState is ApkDownloader.DownloadState.Failed

    Dialog(
        onDismissRequest = {
            if (!isForceUpdate && !isDownloading) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isForceUpdate && !isDownloading,
            dismissOnClickOutside = !isForceUpdate && !isDownloading
        )
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            // 标题
            Text(
                text = "发现新版本",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 版本号 + 大小
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = appUpdate.verName,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                if (appUpdate.newAppSize > 0) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${"%.1f".format(appUpdate.newAppSize)} MB",
                        fontSize = 14.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 更新说明
            Text(
                text = "更新日志",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = appUpdate.newAppUpdateDesc,
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 下载进度条
            if (isDownloading) {
                val progress = (downloadState as ApkDownloader.DownloadState.Downloading).progress
                val animatedProgress by animateFloatAsState(
                    targetValue = progress / 100f,
                    label = "download_progress"
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE0E0E0),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "下载中... $progress%",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 下载失败提示
            if (isFailed) {
                Text(
                    text = (downloadState as ApkDownloader.DownloadState.Failed).message,
                    fontSize = 12.sp,
                    color = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isForceUpdate && !isDownloading) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "暂不更新",
                            color = Color(0xFF999999),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = {
                        if (isCompleted) {
                            // 已下载完成 → 安装
                            ApkDownloader.installApk(
                                context = context,
                                file = (downloadState as ApkDownloader.DownloadState.Completed).file
                            )
                        } else {
                            onUpdateClick()
                        }
                    },
                    enabled = !isDownloading
                ) {
                    val buttonText = when {
                        isCompleted -> "安装更新"
                        isFailed -> "重新下载"
                        else -> "立即更新"
                    }
                    Text(
                        text = buttonText,
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 强制更新时退出按钮
            if (isForceUpdate && !isDownloading) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        android.os.Process.killProcess(android.os.Process.myPid())
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "退出应用",
                        color = Color(0xFFF44336),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
