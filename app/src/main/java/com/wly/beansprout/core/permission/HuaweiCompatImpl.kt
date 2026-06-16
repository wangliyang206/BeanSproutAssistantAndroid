package com.wly.beansprout.core.permission

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 华为悬浮窗权限兼容实现
 * 根据 EMUI 版本（3.0 / 3.1 / 其他）跳转到不同的悬浮窗管理页面
 */
class HuaweiCompatImpl : BelowApi23CompatImpl() {

    private companion object {
        const val TAG = "HuaweiCompatImpl"
    }

    override fun isSupported(): Boolean = true

    override fun apply(context: Context): Boolean {
        try {
            val intent = Intent().apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            // 悬浮窗管理页面
            var comp = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity"
            )
            intent.component = comp

            val emuiVer = RomUtils.getEmuiVersion()
            if (emuiVer in 3.05..3.15) {
                // EMUI 3.1 的适配
                startActivity(context, intent)
            } else {
                // EMUI 3.0 的适配
                comp = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.notificationmanager.ui.NotificationManagmentActivity"
                )
                intent.component = comp
                startActivity(context, intent)
            }
        } catch (e: SecurityException) {
            // 华为权限管理，跳转到本 app 的权限管理页面
            val intent = Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity"
                )
            }
            startActivity(context, intent)
            Log.e(TAG, Log.getStackTraceString(e))
        } catch (e: ActivityNotFoundException) {
            // 手机管家版本较低 HUAWEI SC-UL10
            val intent = Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                component = ComponentName(
                    "com.Android.settings",
                    "com.android.settings.permission.TabItem"
                )
            }
            startActivity(context, intent)
            Log.e(TAG, Log.getStackTraceString(e))
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
        return false
    }

    private fun startActivity(context: Context, intent: Intent) {
        context.startActivity(intent)
    }
}
