package com.wly.beansprout.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.wly.beansprout.R
import com.wly.beansprout.feature.home.viewmodel.AppUpdateViewModel
import com.wly.beansprout.feature.home.viewmodel.HomeViewModel
import com.wly.beansprout.presentation.CommTopBar
import com.wly.beansprout.presentation.dialog.AppUpdateDialog
import com.wly.beansprout.presentation.theme.HomeBackground
import com.wly.beansprout.presentation.theme.JetNewsTheme

/**
 * 主页内容
 */
@Composable
fun HomeScreenContent(
    navController: NavController,
    viewModel: HomeViewModel,
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    // 使用状态管理
    val uiState by viewModel.uiState.collectAsState()

    // 应用更新状态
    val appUpdate by updateViewModel.appUpdate.collectAsState()
    val showUpdateDialog by updateViewModel.showUpdateDialog.collectAsState()
    val downloadState by updateViewModel.downloadState.collectAsState()

    // 进入首页时检查更新
    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdate()
    }

    // 每次 onResume 时刷新按钮状态（权限可能已变化）
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStartButtonState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    JetNewsTheme {
        CommTopBar(
            title = stringResource(id = R.string.app_name),
            content = { modifier ->
                Column(
                    modifier = modifier
                        .background(HomeBackground)
                ) {
                    // 顶部用户信息
                    HomeTop(
                        phoneNumber = uiState.phoneNumber,
                        userType = uiState.userType,
                        onLogoutClick = viewModel::onLogoutClick
                    )

                    // 中间设置区域
                    HomeCenter(
                        selectedExclusive = uiState.selectedExclusive,
                        selectedFunctions = uiState.selectedFunction,
                        selectedModel = uiState.selectedModel,
                        startButtonState = uiState.startButtonState,
                        onExclusiveChanged = viewModel::updateSelectedExclusive,
                        onFunctionSelected = viewModel::updateSelectedFunction,
                        onModelChanged = viewModel::updateSelectedModel,
                        onStartClick = viewModel::onStartClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    // 底部版本信息 + 链接栏
                    HomeBottom(
                        versionName = uiState.versionName,
                        onTutorialClick = viewModel::navigateToTutorial,
                        onServiceAgreementClick = viewModel::navigateToServiceAgreement,
                        onPrivacyPolicyClick = viewModel::navigateToPrivacyPolicy
                    )
                }
            }
        )
    }

    // 处理返回键逻辑
    HomeBackHandler(
        navController = navController,
        onBackPress = viewModel::onBackPressed
    )

    // 处理对话框和事件
    HomeDialogHandlers(
        uiState = uiState,
        navController = navController,
        viewModel = viewModel
    )

    // 应用更新弹窗
    if (showUpdateDialog && appUpdate != null) {
        AppUpdateDialog(
            appUpdate = appUpdate!!,
            downloadState = downloadState,
            onUpdateClick = { updateViewModel.startDownload() },
            onDismiss = { updateViewModel.dismissUpdateDialog() }
        )
    }
}