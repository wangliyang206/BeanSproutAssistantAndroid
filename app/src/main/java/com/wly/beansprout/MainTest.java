package com.wly.beansprout;

import com.wly.beansprout.utils.CommonUtils;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout
 * @ClassName: MainTest
 * @Description:
 * @Author: WLY
 * @CreateDate: 2025/1/3 10:46
 */
public class MainTest {
    public static void main(String[] args) {
        int index = 0;
        System.out.println("----------------------------------------------------");
        while (index < 10){
            // 测试 getRandomNum 方法
            int randomNum = CommonUtils.getRandomNum(5, 10);
            System.out.println("随机生成5-10的数字: " + randomNum);
            index++;
        }

        System.out.println("----------------------------------------------------");
        index = 0;
        while (index < 10){
            // 测试 getRandomNum 方法
            int randomNum = CommonUtils.getRandomNum(0, 5);
            System.out.println("随机生成0-5的数字: " + randomNum);
            index++;
        }

    }
}
