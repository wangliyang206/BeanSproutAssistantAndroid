package com.wly.beansprout

import android.app.Application
import com.wly.beansprout.core.utils.UMengManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 友盟预初始化（在用户同意隐私政策之前）
        UMengManager.preInit(this)
    }
}