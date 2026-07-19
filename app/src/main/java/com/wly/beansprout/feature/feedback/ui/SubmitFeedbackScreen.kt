package com.wly.beansprout.feature.feedback.ui

import android.widget.Toast
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
import com.wly.beansprout.feature.feedback.viewmodel.SubmitFeedbackViewModel
import com.wly.beansprout.presentation.CommTopBar
import kotlinx.coroutines.flow.collectLatest

/**
 * 提交反馈页
 */
@Composable
fun SubmitFeedbackScreen(
    navController: NavController,
    viewModel: SubmitFeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SubmitFeedbackEvent.SubmitSuccess -> {
                    ToastUtils.showToast(context, "提交成功")
                    navController.popBackStack()
                }
                is SubmitFeedbackEvent.ShowError -> {
                    ToastUtils.showToast(context, event.message)
                }
            }
        }
    }

    CommTopBar(
        title = "提交反馈",
        onBack = { navController.popBackStack() }
    ) { modifier ->
        SubmitFeedbackContent(
            uiState = uiState,
            onTitleChange = viewModel::updateTitle,
            onContentChange = viewModel::updateContent,
            onSubmit = viewModel::submitFeedback,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubmitFeedbackPreview() {
    val navController = rememberNavController()
    SubmitFeedbackScreen(navController)
}
