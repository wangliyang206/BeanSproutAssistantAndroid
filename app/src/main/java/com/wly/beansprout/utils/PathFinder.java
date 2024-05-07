package com.wly.beansprout.utils;

import com.wly.beansprout.bean.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动寻路方法，只设置起点(x、y)和终点(x、y)
 */
public class PathFinder {
    // 起点和终点
    private int startX = 0;
    private int startY = 0;
    private int endX = 0;
    private int endY = 0;

    // 生成路径的步骤数量，可以根据需要调整
    private int numSteps = 10;

    /**
     * 设置起点与终点
     * @param startX   起点x
     * @param startY   起点y
     * @param endX     终点x
     * @param endY     终点y
     */
    public PathFinder(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    /**
     * 设置起点与终点，自定义路径步数
     * @param startX   起点x
     * @param startY   起点y
     * @param endX     终点x
     * @param endY     终点y
     * @param numSteps 需要生成的路径步数
     */
    public PathFinder(int startX, int startY, int endX, int endY, int numSteps) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.numSteps = numSteps;
    }

    // 获取路径点的方法
    public List<Point> getPath() {
        List<Point> path = new ArrayList<>();

        // 计算X和Y的增量
        float dx = (endX - startX) / (float) numSteps;
        float dy = (endY - startY) / (float) numSteps;

        // 从起点开始，逐步增加X和Y的值，直到到达终点
        for (int i = 0; i <= numSteps; i++) {
            int x = (int) (startX + dx * i);
            int y = (int) (startY + dy * i);
            path.add(new Point(x, y));
        }

        return path;
    }
}