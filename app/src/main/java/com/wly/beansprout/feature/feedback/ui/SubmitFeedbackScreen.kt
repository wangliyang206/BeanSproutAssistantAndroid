package com.wly.beansprout.feature.feedback.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.wly.beansprout.feature.feedback.AlbumPickerActivity
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

    // 相册选择器
    val albumPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val uris = data?.getParcelableArrayListExtra<android.net.Uri>(AlbumPickerActivity.EXTRA_URIS)
            val isVideoFlags = data?.getIntegerArrayListExtra(AlbumPickerActivity.EXTRA_IS_VIDEO)

            if (!uris.isNullOrEmpty() && !isVideoFlags.isNullOrEmpty()) {
                val medias = uris.mapIndexed { index, uri ->
                    SelectedMedia(uri = uri, isVideo = isVideoFlags[index] == 1)
                }
                viewModel.addFiles(medias)
            }
        }
    }

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SubmitFeedbackEvent.SubmitSuccess -> {
                    ToastUtils.showToast(context, "提交成功")
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set("shouldRefreshFeedback", true)
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
            onFileSelect = {
                val intent = Intent(context, AlbumPickerActivity::class.java).apply {
                    putExtra(AlbumPickerActivity.EXTRA_MAX_COUNT, 9)
                }
                albumPickerLauncher.launch(intent)
            },
            onFileRemove = viewModel::removeFile,
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
