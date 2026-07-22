package com.wly.beansprout.data.model

/**
 * 自定义序列模型
 *
 * 类似 LuckyBagScheme，用于管理多组自定义动作序列。
 * 每个序列包含一组 TouchPoint（通过 sequenceId 关联）。
 */
data class CustomSequence(
    val id: Int,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
