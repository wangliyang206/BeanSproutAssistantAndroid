package com.wly.beansprout.feature.home.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wly.beansprout.core.utils.ApkDownloader
import com.wly.beansprout.data.model.AppUpdate
import com.wly.beansprout.data.repository.AppUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用更新 ViewModel
 *
 * 管理版本检查和 APK 下载流程。
 */
@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val application: Application,
    private val appUpdateRepository: AppUpdateRepository
) : AndroidViewModel(application) {

    private val _appUpdate = MutableStateFlow<AppUpdate?>(null)
    val appUpdate: StateFlow<AppUpdate?> = _appUpdate.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    val downloadState = ApkDownloader.downloadState

    /**
     * 检查应用更新
     */
    fun checkForUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            val updateInfo = appUpdateRepository.checkUpdate()
            if (updateInfo != null && hasNewVersion(updateInfo.verCode)) {
                _appUpdate.value = updateInfo
                _showUpdateDialog.value = true
            }
        }
    }

    /**
     * 开始下载 APK
     */
    fun startDownload() {
        val update = _appUpdate.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            ApkDownloader.downloadApk(
                context = application,
                url = update.filePath,
                fileName = update.fileName.ifBlank { "update.apk" }
            )
        }
    }

    /**
     * 关闭更新弹窗
     */
    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
        ApkDownloader.resetState()
    }

    /**
     * 比较版本号：本地 < 服务端 = 有新版本
     */
    private fun hasNewVersion(serverVersionCode: Int): Boolean {
        return try {
            val pInfo = application.packageManager
                .getPackageInfo(application.packageName, 0)
            @Suppress("DEPRECATION")
            pInfo.versionCode < serverVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
