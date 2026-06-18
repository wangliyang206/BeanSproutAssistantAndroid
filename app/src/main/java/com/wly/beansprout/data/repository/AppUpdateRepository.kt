package com.wly.beansprout.data.repository

import com.wly.beansprout.core.network.RetrofitClient
import com.wly.beansprout.data.model.AppUpdate
import com.wly.beansprout.data.model.BaseRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用更新数据仓库
 */
@Singleton
class AppUpdateRepository @Inject constructor(
    private val retrofitClient: RetrofitClient
) {
    /**
     * 检查应用更新
     *
     * @return AppUpdate 信息，失败返回 null
     */
    suspend fun checkUpdate(): AppUpdate? {
        return try {
            val request = BaseRequest<Map<String, String>>(data = emptyMap())
            val response = retrofitClient.apiService.getVersion(request)
            if (response.code == 200) {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
