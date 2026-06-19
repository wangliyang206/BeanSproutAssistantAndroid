package com.wly.beansprout.core.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

/**
 * MIUI 悬浮窗权限兼容实现
 * 根据 MIUI 版本（5~8）使用不同的 Intent 跳转权限编辑页面
 */
class MiuiCompatImpl : BelowApi23CompatImpl() {

    private companion object {
        const val TAG = "MiuiCompatImpl"
    }

    private val miuiVersion: Int = RomUtils.getMiuiVersion()

    override fun isSupported(): Boolean = miuiVersion in 5..8

    override fun apply(context: Context): Boolean {
        return when (miuiVersion) {
            5 -> applyV5(context)
            6 -> applyV6(context)
            7 -> applyV7(context)
            8 -> applyV8(context)
            else -> {
                Log.e(TAG, "this is a special MIUI rom version, its version code $miuiVersion")
                false
            }
        }
    }

    /** MIUI 8 下申请权限 */
    private fun applyV8(context: Context): Boolean {
        var intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
            putExtra("extra_pkgname", context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (RomUtils.isIntentAvailable(context, intent)) {
            context.startActivity(intent)
            return true
        }

        intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setPackage("com.miui.securitycenter")
            putExtra("extra_pkgname", context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (RomUtils.isIntentAvailable(context, intent)) {
            context.startActivity(intent)
            true
        } else {
            // 小米平板2上没有安全中心，可以打开应用详情页开启权限
            applyV5(context)
        }
    }

    /** MIUI 7 下申请权限 */
    private fun applyV7(context: Context): Boolean {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity")
            putExtra("extra_pkgname", context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (RomUtils.isIntentAvailable(context, intent)) {
            context.startActivity(intent)
            true
        } else {
            Log.e(TAG, "applyV7 Intent is not available!")
            false
        }
    }

    /** MIUI 6 下申请权限 */
    private fun applyV6(context: Context): Boolean {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity")
            putExtra("extra_pkgname", context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (RomUtils.isIntentAvailable(context, intent)) {
            context.startActivity(intent)
            true
        } else {
            Log.e(TAG, "applyV6 Intent is not available!")
            false
        }
    }

    /** MIUI 5 下申请权限 —— 跳转到应用详情页 */
    private fun applyV5(context: Context): Boolean {
        val packageName = context.packageName
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (RomUtils.isIntentAvailable(context, intent)) {
            context.startActivity(intent)
            true
        } else {
            Log.e(TAG, "applyV5 intent is not available!")
            false
        }
    }
}
