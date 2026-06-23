package com.wly.beansprout.data.model

/**
 * 福袋方案数据类
 *
 * @property id 方案 ID（唯一标识）
 * @property name 方案名称（如"方案一"、"三坐标通用"等）
 * @property createdAt 创建时间戳
 */
data class LuckyBagScheme(
    val id: Int,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
