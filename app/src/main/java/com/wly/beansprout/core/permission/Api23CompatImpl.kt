package com.wly.beansprout.core.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.lang.reflect.Field

/**
 * Android 6.0 以上悬浮窗权限申请实现
 * 使用 Settings.canDrawOverlays() 检测，ACTION_MANAGE_OVERLAY_PERMISSION 申请
 */
class Api23CompatImpl : FloatWinPermissionCompat.CompatImpl {

    override fun check(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            return try {
                val clazz = Settings::class.java
                val canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                canDrawOverlays.invoke(null, context) as Boolean
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
                true
            }
        }
        return true
    }

    override fun isSupported(): Boolean = true

    override fun apply(context: Context): Boolean {
        return try {
            commonROMPermissionApplyInternal(context)
            true
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            false
        }
    }

    companion object {
        private const val TAG = "Api23CompatImpl"

        /**
         * 通用 ROM 权限申请 —— 打开系统悬浮窗权限设置页
         */
        @Throws(NoSuchFieldException::class, IllegalAccessException::class)
        fun commonROMPermissionApplyInternal(context: Context) {
            val clazz = Settings::class.java
            val field: Field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION")
            val intent = Intent(field.get(null).toString()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
