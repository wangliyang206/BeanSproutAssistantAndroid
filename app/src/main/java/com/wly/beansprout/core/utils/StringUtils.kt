package com.wly.beansprout.core.utils

import java.util.regex.Pattern
import kotlin.random.Random

/**
 * 字符串处理工具类
 */
object StringUtils {

    // 手机号正则（中国大陆 1[3-9] 开头，共 11 位）
    private val PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}\$")

    /**
     * 验证手机号是否有效
     */
    fun isValidPhoneNumber(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches()
    }

    /**
     * 验证密码是否有效（6-20 位）
     */
    fun isValidPassword(password: String?): Boolean {
        if (password.isNullOrBlank()) return false
        return password.length in 6..20
    }

    /**
     * 脱敏手机号：138****5678
     */
    fun maskPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length < 7) return phoneNumber
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7)
    }

    /**
     * 生成 [min, max] 范围内的随机整数（包含两端）
     */
    fun randomInt(min: Int, max: Int): Int {
        if (min > max) return min
        return Random.nextInt(min, max + 1)
    }

    /**
     * 生成 [0, n) 范围内的随机整数
     */
    fun randomInt(n: Int): Int {
        if (n <= 0) return 0
        return Random.nextInt(n)
    }

    /**
     * 从分号分隔的字符串中随机选取一条
     * 用于自动回复脚本的随机选取
     */
    fun randomPick(script: String, delimiter: String = ";"): String {
        val items = script.split(delimiter)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (items.isEmpty()) return ""
        return items[randomInt(items.size)]
    }

    /**
     * 判断字符串是否为空或仅包含空白字符
     */
    fun isBlank(text: String?): Boolean {
        return text.isNullOrBlank()
    }

    /**
     * 安全获取字符串，null 或空返回默认值
     */
    fun orDefault(text: String?, default: String = ""): String {
        return if (text.isNullOrBlank()) default else text
    }

    /**
     * 将用户状态码转为中文描述
     */
    fun userStatusToText(status: Int): String {
        return when (status) {
            1 -> "待审核"
            2 -> "审核中"
            3 -> "已退回"
            4 -> "正式用户"
            5 -> "已停用"
            6 -> "体验用户"
            9 -> "已删除"
            else -> "未知状态"
        }
    }

    /**
     * 获取用户类型显示文本
     * @param status 用户状态码
     * @param daysRemaining 体验剩余天数（仅 status=6 时使用）
     */
    fun userTypeText(status: Int, daysRemaining: Int): String {
        return if (status == 4) {
            "正式用户"
        } else if (status == 6) {
            "体验用户(剩余${daysRemaining}天)"
        } else {
            userStatusToText(status)
        }
    }
}
