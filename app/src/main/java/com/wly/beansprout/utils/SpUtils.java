package com.wly.beansprout.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.wly.beansprout.bean.TouchPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * SharedPreferences 工具类
 */
public class SpUtils {

    /**
     * 保存在手机里面的文件名
     */
    private static final String FILE_NAME = "share_date";
    private static final String KEY_TOUCH_LIST = "touch_list";

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void setParam(Context context, String key, Object object) {

        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if ("String".equals(type)) {
            editor.putString(key, (String) object);
        } else if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) object);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) object);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) object);
        }
        editor.apply();
    }


    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object getParam(Context context, String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        if ("String".equals(type)) {
            return sp.getString(key, (String) defaultObject);
        } else if ("Integer".equals(type)) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if ("Boolean".equals(type)) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if ("Float".equals(type)) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if ("Long".equals(type)) {
            return sp.getLong(key, (Long) defaultObject);
        }

        return null;
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear().apply();
    }

    /**
     * 清除指定数据
     *
     * @param context
     */
    public static void clearAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("定义的键名");
        editor.apply();
    }

    /**
     * 累积添加
     */
    public static void addTouchPoint(Context context, TouchPoint touchPoint) {
        List<TouchPoint> touchPoints = getTouchPoints(context);
        touchPoints.add(touchPoint);
        setTouchPoints(context, touchPoints);
    }

    /**
     * 添加
     */
    public static void setTouchPoints(Context context, List<TouchPoint> touchPoints) {
        String string = GsonUtils.beanToJson(touchPoints);
        setParam(context, KEY_TOUCH_LIST, string);
    }

    /**
     * 获取已添加的数据
     */
    public static List<TouchPoint> getTouchPoints(Context context) {
        String string = (String) getParam(context, KEY_TOUCH_LIST, "");
        if (TextUtils.isEmpty(string)) {
            return new ArrayList<>();
        }
        return GsonUtils.jsonToList(string, TouchPoint.class);
    }

    /**
     * 删除记录
     */
    public static void delTouchPoints(Context context, int position) {
        List<TouchPoint> touchPoints = getTouchPoints(context);
        touchPoints.remove(position);
        setTouchPoints(context, touchPoints);
    }

    /**
     * 修改数据
     */
    public static void updateTouchPoints(Context context, int position, boolean isStart) {
        List<TouchPoint> touchPoints = getTouchPoints(context);
        touchPoints.get(position).setStartClick(isStart);
        setTouchPoints(context, touchPoints);
    }
}
