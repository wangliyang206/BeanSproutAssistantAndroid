package com.wly.beansprout.feature.splash.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wly.beansprout.core.datastore.LoginPreferences
import com.wly.beansprout.core.utils.UMengManager
import com.wly.beansprout.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    application: android.app.Application,
    private val loginRepository: LoginRepository,
    private val loginPreferences: LoginPreferences
) : AndroidViewModel(application) {

    // 是否正在加载
    var isLoading by mutableStateOf(true)
        private set

    // 导航状态：null-未决定, true-首页, false-登录页
    var navigateToHome by mutableStateOf<Boolean?>(null)
        private set

    // 隐私政策弹窗状态
    // null-不需要显示（已同意）, FIRST-首次询问, SECOND-二次确认
    var privacyDialogState by mutableStateOf<PrivacyDialogState?>(null)
        private set

    /**
     * 启动流程
     */
    fun startLoadingTasks() {
        viewModelScope.launch {
            try {
                // 1. 先检查是否已同意隐私政策
                val privacyAgreed = loginPreferences.privacyAgreedFlow.first()
                if (!privacyAgreed) {
                    // 未同意，显示隐私政策弹窗
                    privacyDialogState = PrivacyDialogState.FIRST
                    isLoading = false
                    return@launch
                }

                // 2. 已同意，执行 Token 校验流程
                validateTokenAndNavigate()
            } catch (e: Exception) {
                e.printStackTrace()
                navigateToHome = false
                isLoading = false
            }
        }
    }

    /**
     * 用户同意隐私政策
     */
    fun onPrivacyAgreed() {
        viewModelScope.launch {
            // 保存同意状态到 DataStore
            loginPreferences.setPrivacyAgreed(true)
            // 友盟完整初始化（必须在用户同意之后）
            UMengManager.init(getApplication())
            // 关闭弹窗
            privacyDialogState = null
            // 继续 Token 校验流程
            isLoading = true
            validateTokenAndNavigate()
        }
    }

    /**
     * 用户不同意隐私政策（首次）
     */
    fun onPrivacyDisagreeFirst() {
        // 显示二次确认弹窗
        privacyDialogState = PrivacyDialogState.SECOND
    }

    /**
     * 用户不同意隐私政策（二次确认）
     */
    fun onPrivacyDisagreeSecond() {
        // 退出 App
        privacyDialogState = null
        navigateToHome = false
        shouldExitApp = true
        isLoading = false
    }

    // 标记：是否需要退出 App（两次不同意隐私政策）
    var shouldExitApp by mutableStateOf(false)
        private set

    /**
     * 验证 Token 并决定导航方向
     */
    private suspend fun validateTokenAndNavigate() {
        try {
            // 检查本地是否有 Token
            val userInfo = loginRepository.getUserInfo()
            if (userInfo.token.isBlank()) {
                // 无 Token，跳登录
                navigateToHome = false
                return
            }

            // 有 Token，验证有效性
            loginRepository.validToken()
            // 验证成功，跳首页
            navigateToHome = true
            // 友盟：用户登录（使用手机号作为 userId）
            UMengManager.onProfileSignIn(userInfo.userId)
        } catch (e: Exception) {
            // 任何异常都跳登录
            e.printStackTrace()
            navigateToHome = false
        } finally {
            isLoading = false
        }
    }
}

/**
 * 隐私政策弹窗状态
 */
enum class PrivacyDialogState {
    FIRST,   // 首次询问
    SECOND   // 二次确认
}
