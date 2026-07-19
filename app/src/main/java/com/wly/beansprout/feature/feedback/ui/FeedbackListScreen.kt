package com.wly.beansprout.feature.feedback.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wly.beansprout.feature.feedback.viewmodel.FeedbackListViewModel
import com.wly.beansprout.presentation.CommTopBar
import com.wly.beansprout.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest

/**
 * 反馈列表页
 */
@Composable
fun FeedbackListScreen(
    navController: NavController,
    viewModel: FeedbackListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is FeedbackListEvent.NavigateToSubmit -> {
                    navController.navigate(NavRoutes.SubmitFeedback.route)
                }
                is FeedbackListEvent.NavigateToDetail -> {
                    navController.navigate(
                        NavRoutes.FeedbackDetail.withArgs(event.feedbackId)
                    )
                }
            }
        }
    }

    // 初始加载
    LaunchedEffect(Unit) {
        viewModel.loadInitial()
    }

    CommTopBar(
        title = "咨询反馈",
        onBack = { navController.popBackStack() }
    ) { modifier ->
        FeedbackListContent(
            uiState = uiState,
            onRefresh = viewModel::refresh,
            onLoadMore = viewModel::loadMore,
            onItemClick = viewModel::navigateToDetail,
            onAddClick = viewModel::navigateToSubmit,
            onRetry = viewModel::refresh,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackListPreview() {
    val navController = rememberNavController()
    FeedbackListScreen(navController)
}
