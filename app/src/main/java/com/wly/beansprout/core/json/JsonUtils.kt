package com.wly.beansprout.core.json

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * JSON 序列化/反序列化工具
 * 基于 Gson，提供简洁的 API 用于数据模型与 JSON 字符串互转
 */
object JsonUtils {

    private val gson = Gson()

    /** 对象转 JSON 字符串 */
    fun toJson(obj: Any): String = gson.toJson(obj)

    /** JSON 字符串转对象 */
    fun <T> fromJson(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)

    /** JSON 字符串转 List */
    fun <T> fromJsonList(json: String, clazz: Class<T>): List<T> {
        if (json.isBlank()) return emptyList()
        val type = TypeToken.getParameterized(List::class.java, clazz).type
        return gson.fromJson(json, type)
    }
}
