package com.wly.beansprout.feature.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wly.beansprout.presentation.dialog.CommonDialog

/**
 * 退出确认对话框
 */
@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    CommonDialog(
        showDialog = true,
        onDismissRequest = onDismiss,
        title = "温馨提示",
        content = { Text("你真的要退出吗？") },
        confirmText = "确认",
        onConfirmClick = onConfirm,
        cancelText = "取消",
        onCancelClick = onDismiss
    )
}

@Composable
fun ErrorMessageDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    CommonDialog(
        showDialog = true,
        onDismissRequest = onDismiss,
        title = "登录失败",
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmText = "确定",
        onConfirmClick = onDismiss
    )
}

@Composable
fun LoadingDialog() {
    CommonDialog(
        showDialog = true,
        onDismissRequest = {
            /* 加载中对话框不能手动关闭 */
        },
        title = "登录中",
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("请稍候...")
            }
        },
        confirmText = null, // 不显示确认按钮
        cancelText = null,  // 不显示取消按钮
        dismissible = false // 不可关闭
    )
}