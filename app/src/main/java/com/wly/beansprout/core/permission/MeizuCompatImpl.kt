package com.wly.beansprout.core.permission

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 魅族悬浮窗权限兼容实现
 * 通过 Flyme 安全中心 Intent 打开权限页面，Flyme 6.2.5+ 回退到通用实现
 */
class MeizuCompatImpl : BelowApi23CompatImpl() {

    override fun isSupported(): Boolean = true

    override fun apply(context: Context): Boolean {
        try {
            val intent = Intent("com.meizu.safe.security.SHOW_APPSEC").apply {
                setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity")
                putExtra("packageName", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                Log.d("MeizuCompatImpl", "flyme 6.2.5+, apply permission failed")
                Api23CompatImpl.commonROMPermissionApplyInternal(context)
            } catch (eFinal: Exception) {
                eFinal.printStackTrace()
            }
        }
        return true
    }
}
