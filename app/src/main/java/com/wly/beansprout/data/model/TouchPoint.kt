package com.wly.beansprout.data.model

/**
 * 触点数据模型
 *
 * @property name 触点名称
 * @property x 屏幕 X 坐标
 * @property y 屏幕 Y 坐标
 * @property delay 点击间隔（毫秒）
 * @property isStartClick 是否开启点击
 * @property functionType 功能类型：0其它 1单击 2点赞 3向下滑 4向上滑 5向左滑 6向右滑 7自动回复 8抢福袋
 * @property luckyBagTime 抢福袋时间（分钟）：-1不设置, 998=5~10随机, 997=0~5随机, 999=不限时
 */
data class TouchPoint(
    val name: String = "",
    val x: Int = 0,
    val y: Int = 0,
    val delay: Int = 0,
    val isStartClick: Boolean = false,
    val functionType: Int = 0,
    val luckyBagTime: Int = -1,
    val schemeId: Int = 0  // 福袋方案 ID，0 = 默认方案
) {
    companion object {
        const val TYPE_OTHER = 0
        const val TYPE_SINGLE_CLICK = 1
        const val TYPE_LIKE = 2
        const val TYPE_SLIDE_DOWN = 3
        const val TYPE_SLIDE_UP = 4
        const val TYPE_SLIDE_LEFT = 5
        const val TYPE_SLIDE_RIGHT = 6
        const val TYPE_AUTO_REPLY = 7
        const val TYPE_LUCKY_BAG = 8
    }
}
