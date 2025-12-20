package com.wly.beansprout.data.repository

import com.wly.beansprout.core.datastore.LoginPreferences
import com.wly.beansprout.core.network.ApiService
import com.wly.beansprout.data.model.LoginRequest
import com.wly.beansprout.data.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPrefs: LoginPreferences
) {
    // 登录请求（网络+本地存储）
    suspend fun login(phone: String, password: String): UserInfo {
        return withContext(Dispatchers.IO) {
            val response = apiService.login(LoginRequest(phone, password))
            if (response.code != 200 || response.data == null) {
                throw Exception(response.msg ?: "登录失败")
            }
            // 登录成功，保存用户信息到DataStore
            val user = UserInfo(
                token = response.data.token,
                userId = response.data.userId,
                userName = response.data.userName,
                userPhone = response.data.userPhone,
                status = response.data.status,
                daysRemaining = response.data.daysRemaining,
                rememberPassword = response.data.rememberPassword
            )
            userPrefs.saveLoginInfo(user)
            return@withContext user
        }
    }

    // 退出登录（清除本地存储）
    suspend fun logout() {
        userPrefs.clearLoginInfo()
    }

    // 检查自动登录
    suspend fun checkAutoLogin(): UserInfo? {
        return userPrefs.userInfoFlow.first()
    }
}