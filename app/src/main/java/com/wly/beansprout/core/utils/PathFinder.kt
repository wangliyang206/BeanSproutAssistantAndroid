package com.wly.beansprout.core.utils

import com.wly.beansprout.data.model.Point

/**
 * 自动寻路方法：线性插值生成路径点
 *
 * 给定起点和终点，按步数均匀插值生成中间路径点列表。
 *
 * @param startX 起点 X 坐标
 * @param startY 起点 Y 坐标
 * @param endX   终点 X 坐标
 * @param endY   终点 Y 坐标
 * @param numSteps 路径步数（默认 10 步）
 */
class PathFinder(
    private val startX: Int,
    private val startY: Int,
    private val endX: Int,
    private val endY: Int,
    private val numSteps: Int = DEFAULT_STEPS
) {

    /**
     * 获取从起点到终点的插值路径点列表
     *
     * @return 包含起点和终点在内的 (numSteps + 1) 个路径点
     */
    fun getPath(): List<Point> {
        val path = mutableListOf<Point>()
        val dx = (endX - startX).toFloat() / numSteps
        val dy = (endY - startY).toFloat() / numSteps

        for (i in 0..numSteps) {
            val x = (startX + dx * i).toInt()
            val y = (startY + dy * i).toInt()
            path.add(Point(x, y))
        }

        return path
    }

    companion object {
        const val DEFAULT_STEPS = 10
    }
}
