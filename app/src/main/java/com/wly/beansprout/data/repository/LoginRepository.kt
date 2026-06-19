package com.wly.beansprout.data.repository

import com.wly.beansprout.core.datastore.LoginPreferences
import com.wly.beansprout.core.network.RequestHelper
import com.wly.beansprout.core.network.RetrofitClient
import com.wly.beansprout.data.model.UserInfo
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 登录相关数据逻辑
 */
class LoginRepository @Inject constructor(
    private val retrofitClient: RetrofitClient,
    private val userPrefs: LoginPreferences,
    private val requestHelper: RequestHelper
) : BaseRepository() {

    // 验证令牌
    suspend fun validToken(): UserInfo {
        // 组装数据
        val requestData = mutableMapOf<String, String>()

        // 发送请求（携带完整信封字段 + token）
        val userData: UserInfo = requestNetwork {
            retrofitClient.apiService.validToken(
                requestHelper.buildRequest(requestData)
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
        requestData["mobile"] = mobile
        requestData["password"] = password

        // 发送请求（登录前无 token，使用匿名请求）
        val userData: UserInfo = requestNetwork {
            retrofitClient.apiService.login(
                requestHelper.buildAnonymousRequest(requestData)
            )
        }

        operateLocal {
            // 保存用户信息到 DataStore
            userPrefs.saveLoginInfo(userData)
        }

        return userData
    }

    // 退出登录（清除令牌和用户信息，保留手机号）
    suspend fun logout() {
        operateLocal {
            userPrefs.clearForLogout()
        }
    }

    // 获取本地用户信息
    suspend fun getUserInfo(): UserInfo {
        return operateLocal {
            // 读取本地用户信息
            userPrefs.userInfoFlow.first()
        }
    }
}