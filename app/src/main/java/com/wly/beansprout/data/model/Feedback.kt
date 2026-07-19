package com.wly.beansprout.data.model

/**
 * 反馈状态
 */
enum class FeedbackStatus(val value: String, val displayName: String) {
    PENDING("0", "待处理"),
    PROCESSING("1", "处理中"),
    REPLIED("2", "已回复"),
    CLOSED("3", "已关闭");

    companion object {
        fun fromValue(value: String?): FeedbackStatus {
            return values().find { it.value == value } ?: PENDING
        }
    }
}

/**
 * 反馈回复
 */
data class FeedbackReply(
    val replyId: Long = 0,
    val feedbackId: Long = 0,
    val replyUserId: Long? = null,
    val replyUserName: String? = null,
    val replyContent: String? = null,
    val createTime: String? = null
)

/**
 * 反馈
 */
data class Feedback(
    val feedbackId: Long = 0,
    val userId: Int? = null,
    val userName: String? = null,
    val userPhone: String? = null,
    val title: String = "",
    val content: String = "",
    val status: String? = null,
    val replyContent: String? = null,
    val replyTime: String? = null,
    val replyUserId: Long? = null,
    val replyUserName: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val replies: List<FeedbackReply>? = null
)

/**
 * 反馈列表响应
 */
data class FeedbackListResponse(
    val list: List<Feedback> = emptyList(),
    val total: Int = 0
)

/**
 * 反馈详情响应
 */
data class FeedbackDetailResponse(
    val feedback: Feedback? = null
)

/**
 * 提交反馈请求
 */
data class SubmitFeedbackRequest(
    val title: String = "",
    val content: String = ""
)

/**
 * 提交反馈响应
 */
data class SubmitFeedbackResponse(
    val success: Boolean = false
)
