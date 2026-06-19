package com.wly.beansprout.core.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * 无障碍服务工具类
 * 检测无障碍服务是否已开启、跳转到系统无障碍设置页面
 */
object AccessibilityUtils {

    private const val TAG = "AccessibilityUtils"

    /**
     * 检测指定的无障碍服务是否已开启
     * @param context 上下文
     * @param serviceClass 无障碍服务的 Class
     */
    fun isServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        return try {
            val enabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED, 0
            )
            if (enabled != 1) return false

            val services = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (services.isNullOrEmpty()) return false

            val splitter = android.text.TextUtils.SimpleStringSplitter(':')
            splitter.setString(services)
            val targetService = "${context.packageName}/${serviceClass.name}"
            while (splitter.hasNext()) {
                if (splitter.next().equals(targetService, ignoreCase = true)) {
                    return true
                }
            }
            false
        } catch (e: Throwable) {
            // 厂商篡改了设置，需要适配
            Log.e(TAG, "isServiceEnabled: ${e.message}")
            false
        }
    }

    /**
     * 跳转到系统无障碍设置页面
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: Throwable) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Throwable) {
                Log.e(TAG, "openAccessibilitySettings: ${e2.message}")
            }
        }
    }
}
