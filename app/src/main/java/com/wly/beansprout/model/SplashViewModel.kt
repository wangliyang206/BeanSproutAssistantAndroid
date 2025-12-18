package com.wly.beansprout.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    // 状态管理：是否正在加载数据
    var isLoading by mutableStateOf(true)
        private set

    // 启动数据加载（在 Activity 的 onCreate 中调用）
    fun startLoadingTasks() {
        viewModelScope.launch {
            // 模拟实际初始化任务（按需替换为真实逻辑）
            loadRemoteConfig()
            checkAuthState()
            preloadData()

            isLoading = false // 所有任务完成后关闭闪屏
        }
    }

    /**
     * 加载远程配置
     */
    private suspend fun loadRemoteConfig() {
        delay(1200) // 模拟远程配置加载
    }

    /**
     * 检查身份验证状态
     */
    private suspend fun checkAuthState() {
        delay(800) // 模拟登录态检查
    }

    /**
     * 预加载数据
     */
    private suspend fun preloadData() {
        delay(500) // 模拟数据预加载
    }
}