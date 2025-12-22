package com.wly.beansprout.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.wly.beansprout.data.model.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * DataStore 操作封装
 */
class LoginPreferences @Inject constructor(
    private val loginDataStore: DataStore<Preferences> // 注入 DataStore 实例
) {

    // 读取登录信息（Flow：可观察数据变化）
    val userInfoFlow: Flow<UserInfo> = loginDataStore.data
        // 捕获 IO 异常（如存储文件损坏）
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences()) // 异常时返回空配置
            } else {
                throw exception
            }
        }
        // 将 Preferences 映射为 LoginInfo 数据类
        .map { preferences ->
            UserInfo(
                token = preferences[LoginKeys.USER_TOKEN] ?: "",
                userId = preferences[LoginKeys.USER_ID] ?: "",
                userName = preferences[LoginKeys.USER_NAME] ?: "",
                userPhone = preferences[LoginKeys.USER_PHONE] ?: "",
                status = preferences[LoginKeys.STATUS] ?: -1,
                daysRemaining = preferences[LoginKeys.DAYS_REMAINING] ?: -1,
                rememberPassword = preferences[LoginKeys.REMEMBER_PASSWORD] ?: false
            )
        }

    // 保存登录信息（登录成功后调用）
    suspend fun saveLoginInfo(userInfo: UserInfo) {
        loginDataStore.edit { preferences ->
            preferences[LoginKeys.USER_TOKEN] = userInfo.token
            preferences[LoginKeys.USER_ID] = userInfo.userId
            preferences[LoginKeys.USER_NAME] = userInfo.userName
            preferences[LoginKeys.USER_PHONE] = userInfo.userPhone
            preferences[LoginKeys.STATUS] = userInfo.status
            preferences[LoginKeys.DAYS_REMAINING] = userInfo.daysRemaining
            preferences[LoginKeys.REMEMBER_PASSWORD] = userInfo.rememberPassword
        }
    }

    // 清除登录信息（退出登录时调用）
    suspend fun clearLoginInfo() {
        loginDataStore.edit { preferences ->
            preferences.remove(LoginKeys.USER_TOKEN)
            preferences.remove(LoginKeys.USER_ID)
            preferences.remove(LoginKeys.USER_NAME)
            preferences.remove(LoginKeys.USER_PHONE)
            preferences.remove(LoginKeys.STATUS)
            preferences.remove(LoginKeys.DAYS_REMAINING)
            preferences.remove(LoginKeys.REMEMBER_PASSWORD)
        }
    }

    // 检查是否已登录
    suspend fun isLoggedIn(): Boolean {
        return loginDataStore.data.first()[LoginKeys.IS_LOGGED_IN] ?: false
    }
}