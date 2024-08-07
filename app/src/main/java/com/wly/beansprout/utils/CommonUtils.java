package com.wly.beansprout.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.core.app.ActivityCompat;

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

    // 隐藏软键盘
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // 获取当前焦点所在的视图，如果为null则不隐藏软键盘
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            0).versionCode;
        } catch (PackageManager.NameNotFoundException ex) {
            versionCode = 0;
        }
        return versionCode;
    }

    /**
     * 获得资源
     */
    public static Resources getResources(Context context) {
        return context.getResources();
    }

    /**
     * 获得屏幕的宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        return getResources(context).getDisplayMetrics().widthPixels;
    }

    /**
     * 获得屏幕的高度
     *
     * @return
     */
    public static int getScreenHeidth(Context context) {
        return getResources(context).getDisplayMetrics().heightPixels;
    }

    public static String getIMEI(Context context) {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // 无权限
            return "";
        }
        return tel.getDeviceId();
    }

    /***
     * Sim卡序列号
     */
    public static String getSimSerialNumber(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // 无权限
                return "";
            }
            String ret = tm.getSimSerialNumber();
            if (ret != null)
                return ret;
            else
                return "";
        } catch (Exception ex) {
            return "";
        }
    }

    /***
     * 拿到电话号码(此接口不会100%的获取手机号码，原因是手机号码是映射到sim卡中的。要想100%获取手机号码只能通过靠对接运营商接口获得)
     */
    public static String getPhoneNumber(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // 无权限
                return "";
            }
            String ret = tm.getLine1Number();
            if (ret != null)
                return ret;
            else
                return "";
        } catch (Exception ex) {
            return "";
        }
    }

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
