package com.wly.beansprout.data.repository

import com.wly.beansprout.core.network.ErrorHandler
import com.wly.beansprout.data.model.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * 仓库基类（封装通用逻辑，如错误处理）
 */
abstract class BaseRepository {
    /**
     * 通用网络请求封装（核心方法）
     * @param request 具体网络请求（如 apiService.login(...)）
     * @return 接口返回的核心数据 T（自动剥离 BaseResponse）
     * @throws Exception 统一封装后的错误（供 ViewModel 捕获）
     */
    protected suspend fun <T> requestNetwork(
        request: suspend () -> BaseResponse<T>
    ): T {
        return withContext(Dispatchers.IO) { // 统一切换到 IO 线程
            try {
                val response = request() // 执行具体网络请求（由子 Repository 传入）
                when {
                    // 接口请求成功
                    response.errorinfo == null -> {
                        response.data ?: throw Exception("数据为空")
                    }
                    // 接口请求失败（code≠200，如参数错误、业务异常）
                    else -> {
                        throw Exception(response.errorinfo.errormessage)
                    }
                }
            } catch (e: Exception) {
                // 统一异常处理
                val errorMsg = ErrorHandler.handleException(e)
                throw Exception(errorMsg)
            }
        }
    }

    /**
     * 通用本地存储操作封装（示例：读取/保存数据）
     * 子 Repository 可直接调用，无需重复写 withContext
     */
    protected suspend fun <T> operateLocal(
        block: suspend () -> T
    ): T {
        return withContext(Dispatchers.IO) {
            try {
                block() // 执行具体本地操作（如 userPrefs.saveUser(...)、roomDao.queryUser(...)）
            } catch (e: Exception) {
                throw Exception("本地存储操作失败：${e.message}")
            }
        }
    }

    /**
     * 通用数据转换接口（可选，规范 DTO ↔ 实体 转换）
     * 子 Repository 可重写该方法，实现自身业务的转换逻辑
     */
    protected open fun <DTO, ENTITY> mapDtoToEntity(dto: DTO): ENTITY {
        throw UnsupportedOperationException("请在子 Repository 中实现具体转换逻辑")
    }
}