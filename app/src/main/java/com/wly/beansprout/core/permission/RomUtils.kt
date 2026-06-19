package com.wly.beansprout.core.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * ROM 工具类
 * 检测当前设备的 ROM 类型（华为/小米/魅族/OPPO/Vivo/360 等）
 */
object RomUtils {

    private const val TAG = "RomUtils"

    /**
     * 获取 EMUI 版本号
     */
    fun getEmuiVersion(): Double {
        return try {
            val emuiVersion = getSystemProperty("ro.build.version.emui") ?: return 4.0
            val version = emuiVersion.substring(emuiVersion.indexOf("_") + 1)
            version.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            4.0
        }
    }

    /**
     * 获取小米 ROM 版本号，获取失败返回 -1
     */
    fun getMiuiVersion(): Int {
        val version = getSystemProperty("ro.miui.ui.version.name")
        if (version != null) {
            try {
                return version.substring(1).toInt()
            } catch (e: Exception) {
                Log.e(TAG, "getInstance miui version code error, version : $version")
            }
        }
        return -1
    }

    /**
     * 读取系统属性
     * System.getProperties() 不返回与 getprop 相同的属性，
     * 所以通过 Runtime.exec() 执行 getprop 并读取标准输出。
     */
    fun getSystemProperty(propName: String): String? {
        var line: String? = null
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
        } catch (ex: IOException) {
            Log.e(TAG, "Unable to read sysprop $propName", ex)
            return null
        } finally {
            try {
                input?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Exception while closing InputStream", e)
            }
        }
        return line
    }

    /** 是否 OPPO 系统 */
    fun isOppo(): Boolean =
        !getSystemProperty("ro.build.version.opporom").isNullOrEmpty()

    /** 是否 Vivo 系统 */
    fun isVivo(): Boolean =
        !getSystemProperty("ro.vivo.os.version").isNullOrEmpty()

    /** 是否华为系统 */
    fun isHuawei(): Boolean =
        Build.MANUFACTURER.contains("HUAWEI")

    /** 是否小米系统 */
    fun isMiui(): Boolean =
        !getSystemProperty("ro.miui.ui.version.name").isNullOrEmpty()

    /** 是否魅族系统 */
    fun isMeizu(): Boolean {
        val meizuFlymeOSFlag = getSystemProperty("ro.build.display.id")
        return !meizuFlymeOSFlag.isNullOrEmpty() && meizuFlymeOSFlag.lowercase().contains("flyme")
    }

    /** 是否 360 系统 */
    fun isQihoo(): Boolean =
        Build.MANUFACTURER.contains("QiKU")

    /**
     * 判断 Intent 是否可用
     */
    fun isIntentAvailable(context: Context, intent: Intent?): Boolean {
        if (intent == null) return false
        return context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .isNotEmpty()
    }
}
