package com.wly.beansprout.core.utils

import android.app.ActivityManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager

/**
 * 窗口 / 屏幕工具类
 */
object WindowUtils {

    /**
     * 获取屏幕宽度（px）
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度（px）
     */
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * 创建悬浮窗 LayoutParams
     */
    fun newWmParams(width: Int, height: Int): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        params.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_SCALED
                or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        params.type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        params.width = width
        params.height = height
        params.format = PixelFormat.TRANSLUCENT
        return params
    }

    /**
     * 获取 WindowManager 实例
     */
    fun getWindowManager(context: Context): WindowManager {
        return context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    /**
     * 判断某个 Service 是否正在运行
     *
     * @param context     上下文
     * @param serviceName 服务全限定类名
     */
    @Suppress("DEPRECATION")
    fun isServiceRunning(context: Context, serviceName: String): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = am.getRunningServices(200) ?: return false
        return runningServices.any { it.service.className == serviceName }
    }
}
