package com.wly.beansprout.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Pattern;

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

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 校验手机号的有效性
     * @param phoneNumber 手机号
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public static String date2String(final Date date, @NonNull final DateFormat format) {
        return format.format(date);
    }

    /**
     * 获取版本号
     *
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
     */
    public static int getScreenWidth(Context context) {
        return getResources(context).getDisplayMetrics().widthPixels;
    }

    /**
     * 获得屏幕的高度
     *
     */
    public static int getScreenHeidth(Context context) {
        return getResources(context).getDisplayMetrics().heightPixels;
    }

    /**
     * 获取屏幕实际高度(无遮挡)
     * @param windowManager
     * @return
     */
    public static int getScreenActualHeight(WindowManager windowManager) {
        // 获取 Display 对象
        Display display = windowManager.getDefaultDisplay();

        // 获取 DisplayMetrics 对象
        DisplayMetrics displayMetrics = new DisplayMetrics();

        // 如果 API 级别 >= 17，可以使用 getRealMetrics 来获取实际高度
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            display.getRealMetrics(displayMetrics);
        } else {
            // 对于低于Jelly Bean MR2（API 17）的设备，你可能需要其他方法来获取屏幕高度
            // 但通常这些设备已经很少见了，且不一定支持AccessibilityService的高级功能
            display.getMetrics(displayMetrics);
            // 注意：getMetrics() 方法不会包括系统UI的高度
        }

        // 获取屏幕分辨率实际高度（以像素为单位）
        int screenHeight = displayMetrics.heightPixels;

        return screenHeight;
    }

    public static String getIMEI(Context context) {
        try {
            TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return "";
            }
            String ret = tel != null ? tel.getDeviceId() : null;
            if (TextUtils.isEmpty(ret))
                return "";
            else
                return ret;
        } catch (Exception ex) {
            return "";
        }
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
