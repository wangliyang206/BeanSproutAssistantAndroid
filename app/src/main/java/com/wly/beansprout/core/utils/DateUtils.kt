package com.wly.beansprout.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 日期处理工具类
 */
object DateUtils {

    private const val DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val DATE_ONLY_FORMAT = "yyyy-MM-dd"
    private const val TIME_ONLY_FORMAT = "HH:mm:ss"

    /**
     * 将 Date 格式化为默认字符串 "yyyy-MM-dd HH:mm:ss"
     */
    fun dateToString(date: Date, format: String = DEFAULT_FORMAT): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(date)
    }

    /**
     * 将时间戳格式化为字符串
     */
    fun timestampToString(timestamp: Long, format: String = DEFAULT_FORMAT): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(Date(timestamp))
    }

    /**
     * 将字符串解析为 Date
     */
    fun stringToDate(dateString: String, format: String = DEFAULT_FORMAT): Date? {
        return try {
            SimpleDateFormat(format, Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取当前日期的字符串表示
     */
    fun getTodayString(format: String = DATE_ONLY_FORMAT): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(Date())
    }

    /**
     * 计算两个日期之间的天数差
     */
    fun daysBetween(date1: Date, date2: Date): Long {
        val diff = Math.abs(date2.time - date1.time)
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    /**
     * 判断是否是今天
     */
    fun isToday(date: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 获取 N 天前的日期
     */
    fun daysAgo(days: Int): Date {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.time
    }

    /**
     * 获取 N 天后的日期
     */
    fun daysLater(days: Int): Date {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, days)
        }.time
    }
}
