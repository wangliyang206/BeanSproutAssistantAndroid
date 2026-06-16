package com.wly.beansprout.presentation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 隐私政策弹窗 - 首次询问
 *
 * 显示隐私政策说明，用户可选择"同意"或"不同意"
 */
@Composable
fun PrivacyPolicyDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit,
    onServiceAgreementClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    CommonDialog(
        showDialog = true,
        onDismissRequest = { /* 不可通过点击外部关闭 */ },
        title = "温馨提示",
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "感谢您使用打工鸡软件助手！",
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "在使用前请仔细阅读《服务协议》和《隐私政策》，以了解我们在使用你个人信息时所遵循的条款。我们将严格按照政策内容使用你的个人信息，尽全力保护您的个人信息安全。感谢信任。",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "点击同意即表示您已阅读并同意《服务协议》和《隐私政策》",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        },
        confirmText = "同意",
        onConfirmClick = onAgree,
        cancelText = "不同意",
        onCancelClick = onDisagree,
        dismissible = false
    )
}

/**
 * 不同意隐私政策弹窗 - 二次确认
 *
 * 用户第一次不同意后，再次询问确认
 */
@Composable
fun NotPrivacyPolicyDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {
    CommonDialog(
        showDialog = true,
        onDismissRequest = { /* 不可通过点击外部关闭 */ },
        title = "温馨提示",
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "不同意将无法使用我们的产品和服务，并会退出App。",
                    fontSize = 15.sp,
                    color = Color.Black,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "是否重新考虑一下？",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        confirmText = "同意并继续",
        onConfirmClick = onAgree,
        cancelText = "仍然退出",
        onCancelClick = onDisagree,
        dismissible = false
    )
}
