package com.wly.beansprout.feature.home.viewmodel

import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wly.beansprout.BuildConfig
import com.wly.beansprout.core.utils.StringUtils
import com.wly.beansprout.core.utils.UMengManager
import com.wly.beansprout.core.utils.WindowUtils
import com.wly.beansprout.data.repository.LoginRepository
import com.wly.beansprout.feature.floating.FloatingService
import com.wly.beansprout.feature.home.ui.HomeEvent
import com.wly.beansprout.feature.home.ui.HomeUiState
import com.wly.beansprout.feature.home.ui.StartButtonState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: android.app.Application,
    private val loginRepository: LoginRepository
) : AndroidViewModel(application) {

    companion object {
        // 功能选择对应的友盟事件ID
        private val FUNCTION_EVENT_IDS = mapOf(
            0 to "lightlyTrigger",   // 轻点触发
            1 to "liveStreamingLikes", // 直播点赞
            2 to "slideDown",        // 向下滑
            3 to "wipeUp",           // 向上滑
            4 to "swipeLeft",        // 向左滑
            5 to "swipeRight",       // 向右滑
            6 to "autoReply",        // 自动回复
            7 to "luckyBag"          // 抢福袋
        )
    }

    // 私有状态
    private val _uiState = MutableStateFlow(
        HomeUiState(versionName = BuildConfig.VERSION_NAME)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 事件通道
    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    init {
        // 初始化时从 DataStore 加载用户信息
        loadUserInfo()
        // 友盟：进入首页事件
        UMengManager.onEvent(application, "open_main")
    }

    /**
     * 从 DataStore 加载用户信息
     */
    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val userInfo = loginRepository.getUserInfo()
                _uiState.update {
                    it.copy(
                        phoneNumber = userInfo.userPhone,
                        userType = StringUtils.userTypeText(userInfo.status, userInfo.daysRemaining)
                    )
                }
            } catch (e: Exception) {
                // 加载失败保持默认值
            }
        }
    }

    // 回退键处理
    private var lastBackPressTime = 0L

    /**
     * 更新专属平台选择
     * 切换平台时联动：若当前选中的功能在非抖音平台不存在（如抢福袋），自动回退到"直播点赞"
     */
    fun updateSelectedExclusive(exclusive: Int) {
        _uiState.update { state ->
            val currentFunction = state.selectedFunction
            // 抖音有 8 个功能（0-7，含抢福袋），其它平台只有 7 个（0-6）
            val newFunction = if (exclusive != 0 && currentFunction >= 7) 1 else currentFunction
            state.copy(selectedExclusive = exclusive, selectedFunction = newFunction)
        }
    }

    /**
     * 更新功能选择
     */
    fun updateSelectedFunction(functionIndex: Int) {
        _uiState.update { it.copy(selectedFunction = functionIndex) }
        // 友盟：功能选择事件
        val eventId = FUNCTION_EVENT_IDS.getOrElse(functionIndex) { "" }
        if (eventId.isNotBlank()) {
            UMengManager.onEvent(getApplication(), eventId)
        }
    }

    /**
     * 更新模型选择
     */
    fun updateSelectedModel(model: Int) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    /**
     * 刷新开始按钮状态（在 onResume 或权限变化时调用）
     */
    fun refreshStartButtonState() {
        val context: android.content.Context = getApplication()
        val accessibilityEnabled = isAccessibilityServiceEnabled(context)
        val overlayGranted = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M
                || android.provider.Settings.canDrawOverlays(context)
        val serviceRunning = WindowUtils.isServiceRunning(context, FloatingService::class.java.name)

        val buttonState = when {
            !accessibilityEnabled -> StartButtonState.NEED_ACCESSIBILITY
            !overlayGranted -> StartButtonState.NEED_OVERLAY
            serviceRunning -> StartButtonState.RUNNING
            else -> StartButtonState.READY
        }
        _uiState.update { it.copy(startButtonState = buttonState) }
    }

    /**
     * 检查无障碍服务是否已启用
     */
    private fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
        val serviceClass = com.wly.beansprout.feature.accessibility.AutoTouchService::class.java
        val prefString = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val expectedComponent = "${context.packageName}/${serviceClass.name}"
        return prefString.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
    }

    /**
     * 处理开始按钮点击 → 根据状态执行不同操作
     */
    fun onStartClick() {
        when (_uiState.value.startButtonState) {
            StartButtonState.NEED_ACCESSIBILITY -> {
                _uiState.update { it.copy(showAccessibilityDialog = true) }
            }
            StartButtonState.NEED_OVERLAY -> {
                _uiState.update { it.copy(showOverlayDialog = true) }
            }
            StartButtonState.READY -> {
                // 直接启动，不弹确认弹窗（与旧项目一致）
                val (functionType, chickModel, luckybagTime) = confirmStart()
                // 抢福袋功能检查每日提示
                if (functionType == 8 && !checkLuckyBagTipToday()) {
                    _uiState.update { it.copy(showLuckyBagTipDialog = true) }
                    return
                }
                launchService(functionType, chickModel, luckybagTime)
            }
            StartButtonState.RUNNING -> {
                // 停止服务
                val context: android.content.Context = getApplication()
                context.stopService(Intent(context, FloatingService::class.java))
                _uiState.update { it.copy(startButtonState = StartButtonState.READY) }
                viewModelScope.launch {
                    _events.emit(HomeEvent.ShowToast("打工鸡已停止"))
                }
            }
        }
    }

    fun dismissAccessibilityDialog() {
        _uiState.update { it.copy(showAccessibilityDialog = false) }
    }

    fun dismissOverlayDialog() {
        _uiState.update { it.copy(showOverlayDialog = false) }
    }

    fun navigateToAccessibilitySettings() {
        _uiState.update { it.copy(showAccessibilityDialog = false) }
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToAccessibilitySettings)
        }
    }

    fun navigateToOverlaySettings() {
        _uiState.update { it.copy(showOverlayDialog = false) }
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToOverlaySettings)
        }
    }

    /**
     * 关闭开始确认弹窗
     */
    fun dismissStartDialog() {
        _uiState.update { it.copy(showStartDialog = false) }
    }

    /**
     * 确认开始执行：返回当前选择的参数
     * @return Triple(functionType, chickModel, luckybagTime)
     */
    fun confirmStart(): Triple<Int, Int, Int> {
        val state = _uiState.value
        // selectedFunction: 0=轻点触发 1=直播点赞 2=向下滑 3=向上滑 4=向左滑 5=向右滑 6=自动回复 7=抢福袋(抖音专属)
        val functionType = state.selectedFunction + 1 // 映射到 TouchPoint.TYPE_*
        // selectedModel: 0=功德小鸡(闪现) 1=跳绳小鸡(溜达)
        val chickModel = if (state.selectedModel == 0) 1 else 2
        // 福袋循环间隔已固定在 AutoTouchService 内部，此处传 0
        return Triple(functionType, chickModel, 0)
    }

    /**
     * 启动悬浮窗服务并最小化应用（与旧项目一致）
     */
    private fun launchService(functionType: Int, chickModel: Int, luckybagTime: Int) {
        viewModelScope.launch {
            _events.emit(HomeEvent.StartFloatingService(functionType, chickModel, luckybagTime))
            _events.emit(HomeEvent.MinimizeApp)
        }
    }

    /**
     * 关闭福袋每日提示弹窗并启动服务
     */
    fun dismissLuckyBagTipDialog() {
        _uiState.update { it.copy(showLuckyBagTipDialog = false) }
    }

    /**
     * 确认福袋提示：记录今日已展示，然后启动服务
     */
    fun confirmLuckyBagTipDialog() {
        _uiState.update { it.copy(showLuckyBagTipDialog = false) }
        markLuckyBagTipToday()
        val (functionType, chickModel, luckybagTime) = confirmStart()
        launchService(functionType, chickModel, luckybagTime)
    }

    /**
     * 检查今天是否已展示过福袋提示
     */
    private fun checkLuckyBagTipToday(): Boolean {
        val prefs = getApplication<android.app.Application>()
            .getSharedPreferences("lucky_bag_tip", android.content.Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        return prefs.getString("last_shown_date", "") == today
    }

    /**
     * 记录今天已展示福袋提示
     */
    private fun markLuckyBagTipToday() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        getApplication<android.app.Application>()
            .getSharedPreferences("lucky_bag_tip", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("last_shown_date", today)
            .apply()
    }

    /**
     * 显示退出登录确认对话框
     */
    fun onLogoutClick() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    /**
     * 关闭退出登录对话框
     */
    fun dismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    /**
     * 确认退出登录：清除本地数据 → 导航到登录页
     */
    fun confirmLogout() {
        _uiState.update { it.copy(showLogoutDialog = false) }
        // 友盟：用户登出
        UMengManager.onProfileSignOff()
        viewModelScope.launch {
            try {
                // 清除本地存储的登录信息
                loginRepository.logout()
                // 发送导航到登录页事件
                _events.emit(HomeEvent.NavigateToLogin)
            } catch (e: Exception) {
                _events.emit(HomeEvent.ShowToast("退出登录失败：${e.message}"))
            }
        }
    }

    /**
     * 导航到服务协议
     */
    fun navigateToServiceAgreement() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToServiceAgreement)
        }
    }

    /**
     * 导航到隐私政策
     */
    fun navigateToPrivacyPolicy() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToPrivacyPolicy)
        }
    }

    /**
     * 导航到教程视频
     */
    fun navigateToTutorial() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToTutorial)
        }
    }

    /**
     * 处理返回键
     */
    fun onBackPressed(): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastBackPressTime < 2000) {
            viewModelScope.launch {
                _events.emit(HomeEvent.ShowExitAppDialog)
            }
            true
        } else {
            lastBackPressTime = currentTime
            viewModelScope.launch {
                _events.emit(HomeEvent.ShowToast("再按一次返回键退出应用"))
            }
            true
        }
    }

    /**
     * 重置所有设置
     */
    fun resetAllSettings() {
        _uiState.update {
            HomeUiState(
                phoneNumber = it.phoneNumber,
                userType = it.userType,
                versionName = it.versionName
            )
        }
    }
}