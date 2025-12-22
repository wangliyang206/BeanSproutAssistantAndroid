package com.wly.beansprout.data.repository

import com.wly.beansprout.core.datastore.LoginPreferences
import com.wly.beansprout.core.network.ApiService
import com.wly.beansprout.core.network.RetrofitClient
import com.wly.beansprout.data.model.BaseRequest
import com.wly.beansprout.data.model.UserInfo
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 登录相关数据逻辑
 */
class LoginRepository @Inject constructor(
    private val retrofitClient: RetrofitClient,
    private val userPrefs: LoginPreferences
) : BaseRepository() {

    // 验证令牌
    suspend fun validToken(): UserInfo{
        // 组装数据
        val requestData = mutableMapOf<String, String>()

        // 发送登录请求
        val userData: UserInfo = requestNetwork {
            retrofitClient.apiService.validToken(
                BaseRequest(
                    token = userPrefs.userInfoFlow.first().token,
                    data = requestData
                )
            )
        }

        operateLocal {
            // 保存用户信息到 DataStore
            userPrefs.saveLoginInfo(userData)
        }

        return userData
    }

    // 登录请求（网络+本地存储）
    suspend fun login(mobile: String, password: String): UserInfo {

        // 组装数据
        val requestData = mutableMapOf<String, String>()
        requestData.put("mobile", mobile)
        requestData.put("password", password)

        // 发送登录请求
        val userData: UserInfo = requestNetwork {
            retrofitClient.apiService.login(BaseRequest(data = requestData))
        }

        operateLocal {
            // 保存用户信息到 DataStore
            userPrefs.saveLoginInfo(userData)
        }

        return userData
    }

    // 退出登录（清除本地存储）
    suspend fun logout() {
        operateLocal {
            userPrefs.clearLoginInfo()
        }

    }

    // 检查自动登录
    suspend fun checkAutoLogin(): UserInfo {
        return operateLocal {
            // 读取本地用户信息
            userPrefs.userInfoFlow.first()
        }
    }
}