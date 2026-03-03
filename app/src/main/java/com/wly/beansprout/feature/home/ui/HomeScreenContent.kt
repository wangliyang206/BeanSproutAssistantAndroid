package com.wly.beansprout.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.wly.beansprout.R
import com.wly.beansprout.feature.home.viewmodel.HomeViewModel
import com.wly.beansprout.presentation.CommTopBar
import com.wly.beansprout.presentation.theme.HomeBackground
import com.wly.beansprout.presentation.theme.JetNewsTheme

/**
 * 主页内容
 */
@Composable
fun HomeScreenContent(
    navController: NavController,
    viewModel: HomeViewModel
) {
    // 使用状态管理
    val uiState by viewModel.uiState.collectAsState()

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
                        onExclusiveChanged = viewModel::updateSelectedExclusive,
                        onFunctionSelected = viewModel::updateSelectedFunction,
                        onModelChanged = viewModel::updateSelectedModel,
                        onStartClick = viewModel::onStartClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    // 底部版本信息
                    HomeBottom(versionName = uiState.versionName)
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
}