package com.wly.beansprout.core.network

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.DisplayMetrics
import com.wly.beansprout.core.datastore.LoginPreferences
import com.wly.beansprout.data.model.BaseRequest
import com.wly.beansprout.data.model.ClientInfo
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 请求构建助手（与旧项目 RequestMapper 对应）
 *
 * 统一为每个 API 请求填充信封字段：userId、token、version、client、language。
 */
@Singleton
class RequestHelper @Inject constructor(
    private val application: Application,
    private val userPrefs: LoginPreferences
) {

    /**
     * 设备唯一标识（懒加载，整个进程生命周期内只算一次）
     *
     * 优先使用 ANDROID_ID（无需权限、重装不变），
     * 如果拿到 null 或者是已知 bug 值，则生成一个 UUID 持久化存储。
     */
    private val deviceId: String by lazy {
        val androidId = Settings.Secure.getString(
            application.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        if (!androidId.isNullOrBlank() && androidId != BUGGY_ANDROID_ID) {
            androidId
        } else {
            val prefs = application.getSharedPreferences("device_info", Context.MODE_PRIVATE)
            var id = prefs.getString("device_id", null)
            if (id.isNullOrBlank()) {
                id = UUID.randomUUID().toString().replace("-", "")
                prefs.edit().putString("device_id", id).apply()
            }
            id
        }
    }

    /**
     * 构建完整的 BaseRequest（包含所有信封字段）
     *
     * @param data 请求业务数据
     * @return 完整的 BaseRequest
     */
    suspend fun <T> buildRequest(data: T): BaseRequest<T> {
        val userInfo = userPrefs.userInfoFlow.first()
        return BaseRequest(
            userId = userInfo.userId.ifBlank { null },
            token = userInfo.token.ifBlank { null },
            version = API_VERSION,
            client = buildClientInfo(),
            language = LANGUAGE_ZH,
            data = data
        )
    }

    /**
     * 构建不需要登录态的请求（如注册、检查更新）
     */
    fun <T> buildAnonymousRequest(data: T): BaseRequest<T> {
        return BaseRequest(
            version = API_VERSION,
            client = buildClientInfo(),
            language = LANGUAGE_ZH,
            data = data
        )
    }

    /**
     * 构建设备信息（与旧项目 RequestMapper.getPhoneInfo() 对应）
     */
    private fun buildClientInfo(): ClientInfo {
        val screenMetrics = getScreenMetrics()
        val (verCode, verName) = getAppVersionInfo()

        return ClientInfo(
            os = "android",
            osver = android.os.Build.VERSION.SDK_INT.toString(),
            vercode = verCode,
            vername = verName,
            ppiheight = screenMetrics.first,
            ppiwidth = screenMetrics.second,
            deviceid = deviceId
        )
    }

    /**
     * 获取屏幕像素尺寸
     */
    private fun getScreenMetrics(): Pair<Int, Int> {
        return try {
            val dm: DisplayMetrics = application.resources.displayMetrics
            Pair(dm.heightPixels, dm.widthPixels)
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }

    /**
     * 获取应用版本号
     */
    private fun getAppVersionInfo(): Pair<Int, String> {
        return try {
            val pInfo = application.packageManager
                .getPackageInfo(application.packageName, 0)
            @Suppress("DEPRECATION")
            Pair(pInfo.versionCode, pInfo.versionName ?: "")
        } catch (e: PackageManager.NameNotFoundException) {
            Pair(0, "")
        }
    }

    companion object {
        // 接口版本号（与旧项目 Constant.version = 1 一致）
        private const val API_VERSION = 1
        private const val LANGUAGE_ZH = "ZH"
        // ANDROID_ID 经典 bug 值，很多设备会返回这个固定值
        private const val BUGGY_ANDROID_ID = "9774d56d682e549c"
    }
}