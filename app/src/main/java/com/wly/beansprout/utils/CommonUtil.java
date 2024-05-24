package com.wly.beansprout.utils;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout.utils
 * @ClassName: Common
 * @Description: 通用工具
 * @Author: WLY
 * @CreateDate: 2024/5/24 17:59
 */
public class CommonUtil {
    /**
     * 生成随机数
     *
     * @param n 0~n之间的整数
     */
    public static int getRandomNum(int n) {
        return (int) ((Math.random() * n));
    }
}
