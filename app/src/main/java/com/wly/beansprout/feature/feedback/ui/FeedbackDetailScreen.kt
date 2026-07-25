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
import com.wly.beansprout.feature.feedback.viewmodel.FeedbackDetailViewModel
import com.wly.beansprout.presentation.CommTopBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FeedbackDetailScreen(
    navController: NavController,
    feedbackId: Long,
    viewModel: FeedbackDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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

    LaunchedEffect(feedbackId) {
        viewModel.loadDetail(feedbackId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is FeedbackDetailEvent.ShowError -> {
                    ToastUtils.showToast(context, event.message)
                }
                else -> {}
            }
        }
    }

    val title = uiState.feedback?.title ?: "反馈详情"

    CommTopBar(title = title, onBack = { navController.popBackStack() }) { modifier ->
        FeedbackDetailContent(
            uiState = uiState,
            onRetry = viewModel::refresh,
            onInputChange = viewModel::updateInputText,
            onSend = viewModel::sendReply,
            onFileSelect = {
                val intent = Intent(context, AlbumPickerActivity::class.java).apply {
                    putExtra(AlbumPickerActivity.EXTRA_MAX_COUNT, 9)
                }
                albumPickerLauncher.launch(intent)
            },
            onFileRemove = viewModel::removeFile,
            onPreview = viewModel::showPreview,
            modifier = modifier
        )

        uiState.previewMedia?.let { media ->
            MediaPreviewDialog(
                media = media,
                onDismiss = viewModel::hidePreview
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackDetailPreview() {
    FeedbackDetailScreen(rememberNavController(), 1001L)
}
