package com.wly.beansprout.core.permission

import android.content.Context
import android.os.Build

/**
 * Android 6.0 以下的通用实现基类
 * 通过 AppOpsManager.checkOp() 检测悬浮窗权限（op 值 24 = OP_SYSTEM_ALERT_WINDOW）
 */
abstract class BelowApi23CompatImpl : FloatWinPermissionCompat.CompatImpl {

    override fun check(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 19) {
            FloatWinPermissionCompat.checkOp(context, 24) // OP_SYSTEM_ALERT_WINDOW = 24
        } else {
            true
        }
    }
}
