package com.wly.beansprout.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.wly.beansprout.core.datastore.LoginPreferences
import com.wly.beansprout.core.network.RequestHelper
import com.wly.beansprout.core.network.RetrofitClient
import com.wly.beansprout.data.model.FeedbackDetailResponse
import com.wly.beansprout.data.model.FeedbackListRequest
import com.wly.beansprout.data.model.FeedbackListResponse
import com.wly.beansprout.data.model.MediaUrl
import com.wly.beansprout.data.model.ReplyFeedbackRequest
import com.wly.beansprout.data.model.ReplyFeedbackResponse
import com.wly.beansprout.data.model.SubmitFeedbackRequest
import com.wly.beansprout.data.model.SubmitFeedbackResponse
import com.wly.beansprout.feature.feedback.ui.SelectedMedia
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

/**
 * 咨询反馈相关数据逻辑
 */
class FeedbackRepository @Inject constructor(
    private val retrofitClient: RetrofitClient,
    private val requestHelper: RequestHelper,
    @ApplicationContext private val context: Context,
    private val loginPreferences: LoginPreferences
) : BaseRepository() {

    /**
     * 获取反馈列表（分页）
     */
    suspend fun getFeedbackList(pageNum: Int, pageSize: Int): FeedbackListResponse {
        val request = FeedbackListRequest(pageNum = pageNum, pageSize = pageSize)

        return requestNetwork {
            retrofitClient.apiService.getFeedbackList(
                requestHelper.buildRequest(request)
            )
        }
    }

    /**
     * 提交反馈（支持文件上传）
     * 流程：先上传文件获取URL，再提交反馈
     */
    suspend fun submitFeedback(
        title: String,
        content: String,
        mediaFiles: List<SelectedMedia>? = null
    ): SubmitFeedbackResponse {
        // 第一步：如有文件，先上传获取URL
        val mediaUrls = if (!mediaFiles.isNullOrEmpty()) {
            uploadFiles(mediaFiles)
        } else {
            null
        }

        // 第二步：提交反馈（包含文件URL）
        val request = SubmitFeedbackRequest(
            title = title,
            content = content,
            mediaUrls = mediaUrls
        )

        return requestNetwork {
            retrofitClient.apiService.submitFeedback(
                requestHelper.buildRequest(request)
            )
        }
    }

    /**
     * 上传文件并返回媒体URL列表
     */
    private suspend fun uploadFiles(mediaFiles: List<SelectedMedia>): List<MediaUrl> {
        val parts = mediaFiles.mapNotNull { media ->
            val file = getFileFromUri(media.uri)
            file?.let {
                val mimeType = getMimeType(media.uri) ?: "application/octet-stream"
                val requestBody = it.asRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("files", it.name, requestBody)
            }
        }

        if (parts.isEmpty()) return emptyList()

        val token = loginPreferences.userInfoFlow.first().token

        val uploadedUrls = requestNetwork {
            retrofitClient.apiService.uploadFeedbackFiles(parts, token)
        }.urls

        // 将本地媒体类型同步到上传结果
        return uploadedUrls.mapIndexed { index, mediaUrl ->
            if (index < mediaFiles.size) {
                mediaUrl.copy(type = if (mediaFiles[index].isVideo) "2" else "1")
            } else {
                mediaUrl
            }
        }
    }

    /**
     * 获取反馈详情
     */
    suspend fun getFeedbackDetail(feedbackId: Long): FeedbackDetailResponse {
        val requestData = mutableMapOf<String, String>()
        requestData["feedbackId"] = feedbackId.toString()

        return requestNetwork {
            retrofitClient.apiService.getFeedbackDetail(
                requestHelper.buildRequest(requestData)
            )
        }
    }

    /**
     * 继续追问（支持文件上传）
     */
    suspend fun replyFeedback(
        feedbackId: Long,
        replyContent: String,
        mediaFiles: List<SelectedMedia>? = null
    ): ReplyFeedbackResponse {
        val mediaUrls = if (!mediaFiles.isNullOrEmpty()) {
            uploadFiles(mediaFiles)
        } else {
            null
        }

        val request = ReplyFeedbackRequest(
            feedbackId = feedbackId,
            replyContent = replyContent,
            mediaUrls = mediaUrls
        )

        return requestNetwork {
            retrofitClient.apiService.replyFeedback(
                requestHelper.buildRequest(request)
            )
        }
    }

    /**
     * 从 Uri 获取文件
     */
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}_${uri.lastPathSegment}")
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
                tempFile
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取文件 MIME 类型
     */
    private fun getMimeType(uri: Uri): String? {
        return context.contentResolver.getType(uri) ?: run {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }
}
