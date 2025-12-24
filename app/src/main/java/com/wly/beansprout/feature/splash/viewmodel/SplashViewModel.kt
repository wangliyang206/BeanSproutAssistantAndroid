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

    // 导航状态：null-未决定, true-首页, false-登录页
    var navigateToHome by mutableStateOf<Boolean?>(null)
        private set


    // 启动数据加载（在 Activity 的 onCreate 中调用）
    fun startLoadingTasks() {
        viewModelScope.launch {
            try {
                // 1. 先检查本地是否有Token
                val userInfo = loginRepository.checkAutoLogin()
                if (userInfo.token.isBlank()) {
                    // 无Token，直接跳登录
                    navigateToHome = false
                    return@launch
                }

                // 2. 有Token，验证有效性
                loginRepository.validToken()
                // 验证成功，跳首页
                navigateToHome = true
            } catch (e: Exception) {
                // 任何异常都跳登录（包括Token无效、网络错误等）
                e.printStackTrace()
                navigateToHome = false
            } finally {
                // 无论成功/失败，最终都结束加载，关闭闪屏
                isLoading = false
            }
        }
    }
}