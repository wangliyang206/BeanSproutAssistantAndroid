package com.wly.beansprout.global;

import com.wly.beansprout.utils.CommonUtils;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout.global
 * @ClassName: Constant
 * @Description: 公共
 * @Author: WLY
 * @CreateDate: 2024/7/30 11:50
 */
public interface Constant {

    /**
     * 服务器地址
     */
    String SERVER_URL_VALUE = "http://47.115.223.27/";

    /**
     * API版本号
     */
    int version = 1;

    /**
     * 默认展示20条
     */
    int PAGESIZE = 20;


    /**
     * APP升级路径
     */
    String APP_UPDATE_PATH = CommonUtils.getSDCardPathByEnvironment() + "/ChickenAtWork/AppUpdate/";
}
