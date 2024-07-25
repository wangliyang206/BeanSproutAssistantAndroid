package com.wly.beansprout.utils;

import android.app.Activity;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout.utils
 * @ClassName: Common
 * @Description: 通用工具
 * @Author: WLY
 * @CreateDate: 2024/5/24 17:59
 */
public class CommonUtils {
    // 最后一次点击时间
    private static long lastClickTime;

    /**
     * 生成随机数
     *
     * @param n 0~n之间的整数（0包含，n不包含）
     */
    public static int getRandomNum(int n) {
        return (int) ((Math.random() * n));
    }

    /**
     * 按两次Back在退出程序
     *
     * @param context 句柄
     */
    public static void exitSys(Activity context) {
        if ((System.currentTimeMillis() - lastClickTime) > 2000) {
            ToastUtil.show("再按一次退出！");
            lastClickTime = System.currentTimeMillis();
        } else {
            context.finish();
//            /*当前是退出APP后结束进程。如果不这样做，那么在APP结束后需求手动将EventBus中所注册的监听全部清除以免APP在次启动后重复注册监听*/
//            Process.killProcess(Process.myPid());
//            返回到桌面
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            context.startActivity(intent);
        }
    }
}
