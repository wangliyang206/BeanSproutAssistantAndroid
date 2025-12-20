package com.wly.beansprout.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * 全局 DataStore 实例
 */

// 定义 DataStore 实例（name 对应存储文件的名称，比如 "user_login_info"）
val Context.loginDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_login_info"
)
