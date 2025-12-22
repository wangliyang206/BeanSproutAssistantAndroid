package com.wly.beansprout.feature.splash.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wly.beansprout.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val loginRepository: LoginRepository // 注入 LoginRepository
) : ViewModel() {

    // 状态管理：是否正在加载数据
    var isLoading by mutableStateOf(true)
        private set

    // 启动数据加载（在 Activity 的 onCreate 中调用）
    fun startLoadingTasks() {
        viewModelScope.launch {
            try {
                // 调用 LoginRepository 的 validToken 方法（suspend 函数）
                val userInfo = loginRepository.validToken()
                // 可以根据返回的 userInfo 做进一步处理（如验证令牌有效性）
            } catch (e: Exception) {
                // 处理异常（如令牌无效、网络错误等）
                e.printStackTrace()
            } finally {
                // 无论成功/失败，最终都结束加载，关闭闪屏
                isLoading = false
            }
        }
    }
}