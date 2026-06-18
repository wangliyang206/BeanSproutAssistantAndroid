package com.wly.beansprout.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * APK 下载器
 *
 * 使用 OkHttp 下载 APK 文件，通过 StateFlow 报告下载进度。
 */
object ApkDownloader {

    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * 下载 APK 文件
     *
     * @param context 上下文
     * @param url APK 下载地址
     * @param fileName 文件名
     * @return 下载完成的 File，失败返回 null
     */
    suspend fun downloadApk(
        context: Context,
        url: String,
        fileName: String
    ): File? = withContext(Dispatchers.IO) {
        val targetFile = File(context.filesDir, fileName)

        // 删除旧文件
        if (targetFile.exists()) {
            targetFile.delete()
        }

        _downloadState.value = DownloadState.Downloading(0, 0)

        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                _downloadState.value = DownloadState.Failed("下载失败: HTTP ${response.code}")
                return@withContext null
            }

            val body = response.body ?: run {
                _downloadState.value = DownloadState.Failed("下载失败: 空响应体")
                return@withContext null
            }

            val totalLength = body.contentLength()
            val buffer = ByteArray(8192)
            var downloaded: Long = 0
            var lastReportTime = 0L

            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead

                        // 每秒报告一次进度
                        val now = System.currentTimeMillis()
                        if (now - lastReportTime > 500) {
                            _downloadState.value = DownloadState.Downloading(downloaded, totalLength)
                            lastReportTime = now
                        }
                    }
                }
            }

            if (targetFile.exists() && targetFile.length() > 0) {
                _downloadState.value = DownloadState.Completed(targetFile)
                targetFile
            } else {
                _downloadState.value = DownloadState.Failed("下载不完整")
                null
            }
        } catch (e: Exception) {
            _downloadState.value = DownloadState.Failed("下载失败: ${e.message}")
            null
        }
    }

    /**
     * 安装 APK
     *
     * @param context 上下文
     * @param file APK 文件
     */
    fun installApk(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val authority = "${context.packageName}$FILE_PROVIDER_SUFFIX"
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                FileProvider.getUriForFile(context, authority, file)
            } else {
                @Suppress("DEPRECATION")
                Uri.fromFile(file)
            }

            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }

    /**
     * 重置下载状态
     */
    fun resetState() {
        _downloadState.value = DownloadState.Idle
    }

    /**
     * 下载状态
     */
    sealed class DownloadState {
        object Idle : DownloadState()
        data class Downloading(val downloaded: Long, val total: Long) : DownloadState() {
            /** 下载百分比 (0-100) */
            val progress: Int
                get() = if (total > 0) (downloaded * 100 / total).toInt() else 0
        }
        data class Completed(val file: File) : DownloadState()
        data class Failed(val message: String) : DownloadState()
    }
}
