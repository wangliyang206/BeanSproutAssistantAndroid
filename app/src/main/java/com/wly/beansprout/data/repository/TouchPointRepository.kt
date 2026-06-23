package com.wly.beansprout.data.repository

import android.content.Context
import com.wly.beansprout.core.json.JsonUtils
import com.wly.beansprout.data.model.LuckyBagScheme
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
        const val KEY_LUCKY_BAG_SCHEMES = "lucky_bag_schemes"
        const val KEY_CURRENT_SCHEME_ID = "current_scheme_id"
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

    /** 获取指定功能类型的触点列表 */
    fun getTouchPointsByType(functionType: Int): List<TouchPoint> {
        return getTouchPoints().filter { it.functionType == functionType }
    }

    /** 获取非福袋类型的触点列表（functionType != TYPE_LUCKY_BAG） */
    fun getNonLuckyBagTouchPoints(): List<TouchPoint> {
        return getTouchPoints().filter { it.functionType != TouchPoint.TYPE_LUCKY_BAG }
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

    // ======================== 福袋方案管理 ========================

    /**
     * 获取所有福袋方案列表
     *
     * 首次调用时若为空，自动创建默认方案"方案一"(id=0)，
     * 并将所有现有 TYPE_LUCKY_BAG 坐标的 schemeId 设为 0（向后兼容）。
     */
    fun getLuckyBagSchemes(): List<LuckyBagScheme> {
        val json = prefs.getString(KEY_LUCKY_BAG_SCHEMES, "") ?: ""
        val schemes = if (json.isBlank()) {
            emptyList()
        } else {
            try {
                JsonUtils.fromJsonList(json, LuckyBagScheme::class.java)
            } catch (e: Exception) {
                emptyList()
            }
        }

        // 首次使用：自动创建默认方案并迁移旧数据
        if (schemes.isEmpty()) {
            val defaultScheme = LuckyBagScheme(id = 0, name = "方案一")
            saveLuckyBagSchemes(listOf(defaultScheme))

            // 将现有的福袋坐标全部归属到默认方案
            val allPoints = getTouchPoints()
            val hasLegacyLuckyBag = allPoints.any {
                it.functionType == TouchPoint.TYPE_LUCKY_BAG && it.schemeId == 0
            }
            if (hasLegacyLuckyBag) {
                // schemeId 默认就是 0，无需额外迁移
            }

            return listOf(defaultScheme)
        }
        return schemes
    }

    /** 保存福袋方案列表 */
    fun saveLuckyBagSchemes(schemes: List<LuckyBagScheme>) {
        val json = JsonUtils.toJson(schemes)
        prefs.edit().putString(KEY_LUCKY_BAG_SCHEMES, json).apply()
    }

    /**
     * 创建新方案
     *
     * @param name 方案名称
     * @return 新创建的方案
     */
    fun addScheme(name: String): LuckyBagScheme {
        val schemes = getLuckyBagSchemes().toMutableList()
        val maxId = schemes.maxOfOrNull { it.id } ?: -1
        val newScheme = LuckyBagScheme(id = maxId + 1, name = name)
        schemes.add(newScheme)
        saveLuckyBagSchemes(schemes)
        return newScheme
    }

    /**
     * 删除方案及其所有坐标
     *
     * 删除后，如果只剩一个方案则不允许继续删除。
     */
    fun deleteScheme(schemeId: Int) {
        val schemes = getLuckyBagSchemes().toMutableList()
        if (schemes.size <= 1) return  // 至少保留一个方案

        schemes.removeAll { it.id == schemeId }
        saveLuckyBagSchemes(schemes)

        // 同时删除该方案下的所有坐标
        val allPoints = getTouchPoints()
        val filtered = allPoints.filter {
            !(it.functionType == TouchPoint.TYPE_LUCKY_BAG && it.schemeId == schemeId)
        }
        saveTouchPoints(filtered)

        // 如果删除的是当前选中方案，切回第一个
        if (getCurrentSchemeId() == schemeId) {
            setCurrentSchemeId(schemes.first().id)
        }
    }

    /** 获取指定方案的坐标列表 */
    fun getTouchPointsByScheme(schemeId: Int): List<TouchPoint> {
        return getTouchPoints().filter {
            it.functionType == TouchPoint.TYPE_LUCKY_BAG && it.schemeId == schemeId
        }
    }

    /** 获取当前选中的方案 ID */
    fun getCurrentSchemeId(): Int {
        return prefs.getInt(KEY_CURRENT_SCHEME_ID, 0)
    }

    /** 设置当前选中的方案 ID */
    fun setCurrentSchemeId(id: Int) {
        prefs.edit().putInt(KEY_CURRENT_SCHEME_ID, id).apply()
    }
}
