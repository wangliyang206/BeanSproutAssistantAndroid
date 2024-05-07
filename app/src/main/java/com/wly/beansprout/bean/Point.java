package com.wly.beansprout.bean;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout.bean
 * @ClassName: Point
 * @Description:
 * @Author: WLY
 * @CreateDate: 2024/4/29 18:02
 */
public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // 可以添加toString等方法以便更好地打印或调试
    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
