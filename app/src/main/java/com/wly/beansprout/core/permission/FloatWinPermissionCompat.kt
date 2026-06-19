package com.wly.beansprout.core.permission

import android.annotation.TargetApi
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.util.Log
import java.lang.ref.WeakReference

/**
 * 悬浮窗权限兼容类
 * 根据 SDK 版本和 ROM 类型自动选择最佳实现策略。
 *
 * 参考项目：https://github.com/zhaozepeng/FloatWindowPermission
 */
class FloatWinPermissionCompat private constructor() {

    private val compat: CompatImpl
    private var activityRef: WeakReference<Activity>? = null
    private var context: Context? = null
    private var forResult = false

    init {
        compat = if (Build.VERSION.SDK_INT < 23) {
            // 6.0 以下的处理
            when {
                RomUtils.isMiui() -> MiuiCompatImpl()
                RomUtils.isMeizu() -> MeizuCompatImpl()
                RomUtils.isHuawei() -> HuaweiCompatImpl()
                RomUtils.isQihoo() -> QihooCompatImpl()
                else -> object : BelowApi23CompatImpl() {
                    override fun isSupported(): Boolean = false
                    override fun apply(context: Context): Boolean = false
                }
            }
        } else {
            // 魅族单独适配一下
            if (RomUtils.isMeizu()) {
                MeizuCompatImpl()
            } else {
                // 6.0 之后 Google 增加了对悬浮窗权限的管理，方式统一
                Api23CompatImpl()
            }
        }
    }

    /**
     * 检查是否已开启悬浮窗权限
     */
    fun check(context: Context): Boolean = compat.check(context)

    /**
     * 是否支持打开悬浮窗授权界面
     */
    fun isSupported(): Boolean = compat.isSupported()

    /**
     * 申请悬浮窗权限
     * @return 是否成功打开授权界面
     */
    fun apply(context: Context): Boolean {
        if (!isSupported()) return false
        forResult = false
        activityRef = null
        this.context = context
        return compat.apply(context)
    }

    /**
     * 申请悬浮窗权限（从 Activity 中调用，支持 onActivityResult 回调）
     */
    fun apply(activity: Activity): Boolean {
        if (!isSupported()) return false
        activityRef = WeakReference(activity)
        this.context = activity.applicationContext
        forResult = true
        return compat.apply(this.context!!)
    }

    fun startActivity(intent: Intent?) {
        try {
            if (intent == null || context == null) return
            if (!forResult) {
                context!!.startActivity(intent)
            } else {
                activityRef?.get()?.startActivityForResult(intent, REQUEST_CODE_SYSTEM_WINDOW)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 兼容策略接口
     */
    interface CompatImpl {
        /** 检测是否已经授权 */
        fun check(context: Context): Boolean

        /** 对于该 ROM 是否已做悬浮窗授权的兼容支持 */
        fun isSupported(): Boolean

        /** 申请权限 */
        fun apply(context: Context): Boolean
    }

    companion object {
        private const val TAG = "FloatWinPermissionCompat"
        const val REQUEST_CODE_SYSTEM_WINDOW = 1001

        @Volatile
        private var instance: FloatWinPermissionCompat? = null

        fun getInstance(): FloatWinPermissionCompat {
            return instance ?: synchronized(this) {
                instance ?: FloatWinPermissionCompat().also { instance = it }
            }
        }

        /**
         * 通过反射 AppOpsManager.checkOp 检测悬浮窗是否已授权
         * @param op 操作码，悬浮窗权限 OP_SYSTEM_ALERT_WINDOW = 24
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun checkOp(context: Context, op: Int): Boolean {
            if (Build.VERSION.SDK_INT >= 19) {
                val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                try {
                    val clazz = AppOpsManager::class.java
                    val method = clazz.getDeclaredMethod("checkOp", Int::class.java, Int::class.java, String::class.java)
                    val result = method.invoke(manager, op, Binder.getCallingUid(), context.packageName) as Int
                    return result == AppOpsManager.MODE_ALLOWED
                } catch (e: Exception) {
                    Log.e(TAG, Log.getStackTraceString(e))
                }
            } else {
                Log.e(TAG, "Below API 19 cannot invoke!")
            }
            return false
        }
    }
}
