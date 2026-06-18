package com.wly.beansprout.core.utils

import android.content.Context
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.wly.beansprout.BuildConfig
import com.wly.beansprout.R

/**
 * 友盟统计管理器
 *
 * 两阶段初始化：
 * 1. preInit() — Application.onCreate() 中调用，用户同意隐私政策之前
 * 2. init() — 用户同意隐私政策之后调用
 */
object UMengManager {

    private const val CHANNEL = "test_channel"

    /**
     * 预初始化（在用户同意隐私政策之前调用）
     * 仅开启日志，不调用 preInit 以避免合规问题
     */
    fun preInit(context: Context) {
        UMConfigure.setLogEnabled(true)
    }

    /**
     * 完整初始化（在用户同意隐私政策之后调用）
     */
    fun init(context: Context) {
        val appContext = context.applicationContext

        // 1. 提交隐私政策同意结果
        UMConfigure.submitPolicyGrantResult(appContext, true)

        // 2. 初始化 SDK
        val appKey = if (BuildConfig.DEBUG) {
            context.getString(R.string.um_app_key_debug)
        } else {
            context.getString(R.string.um_app_key)
        }
        UMConfigure.init(
            appContext,
            appKey,
            CHANNEL,
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )

        // 3. 支持多进程事件
        UMConfigure.setProcessEvent(true)

        // 4. 自动页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
    }

    /**
     * 上报自定义事件
     */
    fun onEvent(context: Context, eventId: String) {
        MobclickAgent.onEvent(context, eventId)
    }

    /**
     * 用户登录
     */
    fun onProfileSignIn(userId: String) {
        MobclickAgent.onProfileSignIn(userId)
    }

    /**
     * 用户登出
     */
    fun onProfileSignOff() {
        MobclickAgent.onProfileSignOff()
    }
}
