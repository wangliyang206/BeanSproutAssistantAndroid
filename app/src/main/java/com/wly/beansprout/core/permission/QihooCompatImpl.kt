package com.wly.beansprout.core.permission

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 360 悬浮窗权限兼容实现
 * 优先跳转系统设置 OverlaySettingsActivity，回退到 360 手机卫士
 */
class QihooCompatImpl : BelowApi23CompatImpl() {

    private companion object {
        const val TAG = "QihooCompatImpl"
    }

    override fun isSupported(): Boolean = true

    override fun apply(context: Context): Boolean {
        var intent = Intent().apply {
            setClassName("com.android.settings", "com.android.settings.Settings\$OverlaySettingsActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (RomUtils.isIntentAvailable(context, intent)) {
            context.startActivity(intent)
            return true
        }

        intent = Intent().apply {
            setClassName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.appEnterActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (RomUtils.isIntentAvailable(context, intent)) {
            context.startActivity(intent)
            true
        } else {
            Log.e(
                TAG,
                "can't open permission page with particular name, please use " +
                        "\"adb shell dumpsys activity\" command and tell me the name of the float window permission page"
            )
            false
        }
    }
}
