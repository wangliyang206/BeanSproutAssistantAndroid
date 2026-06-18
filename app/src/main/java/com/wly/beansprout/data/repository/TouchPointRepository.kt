package com.wly.beansprout.data.repository

import android.content.Context
import com.wly.beansprout.core.json.JsonUtils
import com.wly.beansprout.data.model.TouchPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 触点数据仓库
 * 使用 SharedPreferences + Gson 序列化存储 TouchPoint 列表。
 * 替代旧项目的 SpUtils 中的触点相关方法。
 */
@Singleton
class TouchPointRepository @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs = context.getSharedPreferences("touch_data", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_TOUCH_LIST = "touch_list"
        const val KEY_AUTO_REPLY_SCRIPT = "auto_reply_script"
        const val DEFAULT_AUTO_REPLY_SCRIPT = "喜欢主播的点点关注、点点赞，感谢！;感谢大家的支持！;如果觉得今天的直播不错，就请给我点个赞吧！你们的支持是我最大的动力！;欢迎各位亲们来到直播间！"
    }

    /** 获取所有已保存的触点 */
    fun getTouchPoints(): List<TouchPoint> {
        val json = prefs.getString(KEY_TOUCH_LIST, "") ?: ""
        if (json.isBlank()) return emptyList()
        return try {
            JsonUtils.fromJsonList(json, TouchPoint::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 保存触点列表（覆盖） */
    fun saveTouchPoints(touchPoints: List<TouchPoint>) {
        val json = JsonUtils.toJson(touchPoints)
        prefs.edit().putString(KEY_TOUCH_LIST, json).apply()
    }

    /** 添加一个触点 */
    fun addTouchPoint(touchPoint: TouchPoint) {
        val list = getTouchPoints().toMutableList()
        list.add(touchPoint)
        saveTouchPoints(list)
    }

    /** 删除指定位置的触点 */
    fun deleteTouchPoint(position: Int) {
        val list = getTouchPoints().toMutableList()
        if (position in list.indices) {
            list.removeAt(position)
            saveTouchPoints(list)
        }
    }

    /** 更新指定触点的点击开关 */
    fun updateTouchPointClick(position: Int, isStartClick: Boolean) {
        val list = getTouchPoints().toMutableList()
        if (position in list.indices) {
            list[position] = list[position].copy(isStartClick = isStartClick)
            saveTouchPoints(list)
        }
    }

    /** 更新指定触点的功能类型和福袋时间 */
    fun updateTouchPointConfig(position: Int, functionType: Int, luckyBagTime: Int) {
        val list = getTouchPoints().toMutableList()
        if (position in list.indices) {
            list[position] = list[position].copy(functionType = functionType, luckyBagTime = luckyBagTime)
            saveTouchPoints(list)
        }
    }

    /** 获取自动回复脚本（分号分隔的多条回复） */
    fun getAutoReplyScript(): String =
        prefs.getString(KEY_AUTO_REPLY_SCRIPT, DEFAULT_AUTO_REPLY_SCRIPT) ?: DEFAULT_AUTO_REPLY_SCRIPT

    /** 保存自动回复脚本 */
    fun setAutoReplyScript(script: String) {
        prefs.edit().putString(KEY_AUTO_REPLY_SCRIPT, script).apply()
    }
}
