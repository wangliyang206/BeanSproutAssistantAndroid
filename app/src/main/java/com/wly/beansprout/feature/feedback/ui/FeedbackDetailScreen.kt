package com.wly.beansprout.feature.feedback.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wly.beansprout.core.utils.ToastUtils
import com.wly.beansprout.feature.feedback.viewmodel.FeedbackDetailViewModel
import com.wly.beansprout.presentation.CommTopBar
import kotlinx.coroutines.flow.collectLatest

/**
 * 反馈详情页
 */
@Composable
fun FeedbackDetailScreen(
    navController: NavController,
    feedbackId: Long,
    viewModel: FeedbackDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 初始加载
    LaunchedEffect(feedbackId) {
        viewModel.loadDetail(feedbackId)
    }

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is FeedbackDetailEvent.ShowError -> {
                    ToastUtils.showToast(context, event.message)
                }
            }
        }
    }

    val title = uiState.feedback?.title ?: "反馈详情"

    CommTopBar(
        title = title,
        onBack = { navController.popBackStack() }
    ) { modifier ->
        FeedbackDetailContent(
            uiState = uiState,
            onRefresh = viewModel::refresh,
            onRetry = viewModel::refresh,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackDetailPreview() {
    val navController = rememberNavController()
    FeedbackDetailScreen(navController, 1001L)
}
