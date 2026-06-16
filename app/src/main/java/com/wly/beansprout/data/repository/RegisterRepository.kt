package com.wly.beansprout.data.repository

import com.wly.beansprout.core.network.RetrofitClient
import com.wly.beansprout.data.model.BaseRequest
import com.wly.beansprout.data.model.UserInfo
import javax.inject.Inject

/**
 * 注册相关数据逻辑
 */
class RegisterRepository @Inject constructor(
    private val retrofitClient: RetrofitClient
) : BaseRepository() {

    /**
     * 注册请求
     * @param mobile 手机号
     * @param password 密码
     * @return 注册成功后返回的用户信息
     */
    suspend fun register(mobile: String, password: String): UserInfo {
        val requestData = mutableMapOf<String, String>()
        requestData["mobile"] = mobile
        requestData["password"] = password

        return requestNetwork {
            retrofitClient.apiService.register(BaseRequest(data = requestData))
        }
    }
}
