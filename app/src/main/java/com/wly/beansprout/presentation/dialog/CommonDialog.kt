package com.wly.beansprout.presentation.dialog

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 通用 Dialog 组件（封装版）
 * @param showDialog 是否显示 Dialog
 * @param onDismissRequest 关闭 Dialog 回调（点击外部/返回键/取消按钮都会触发）
 * @param title Dialog 标题（传 null 则隐藏标题）
 * @param content Dialog 内容区域（自定义 Composable）
 * @param confirmText 确认按钮文本（必填）
 * @param onConfirmClick 确认按钮点击回调
 * @param cancelText 取消按钮文本（传 null 则隐藏取消按钮）
 * @param onCancelClick 取消按钮点击回调（不传则默认触发 onDismissRequest）
 * @param dismissible 是否点击外部关闭 Dialog（默认 true）
 */
@Composable
fun CommonDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    cornerRadius: Dp = 8.dp, // 自定义 Dialog 圆角
    title: String? = null,
    content: @Composable () -> Unit,
    confirmText: String? = "确定", // 改为可空
    cancelText: String? = "取消",  // 改为可空
    onConfirmClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    dismissible: Boolean = true // 是否允许外部点击关闭
) {
    // 仅当 showDialog 为 true 时渲染 Dialog
    if (showDialog) {
        AlertDialog(
            shape = RoundedCornerShape(cornerRadius), // 圆角
            onDismissRequest = {
                if (dismissible) {
                    onDismissRequest()
                }
            },
            title = title?.let {
                {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            },
            text = content, // 自定义内容区域
            confirmButton = {
                confirmText?.let {
                    Button(
                        onClick = {
                            onConfirmClick?.invoke()
                            onDismissRequest() // 点击确认后默认关闭 Dialog
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF008080), // 匹配登录界面的深绿色
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(text = it, fontSize = 16.sp)
                    }
                }
            },
            dismissButton = cancelText?.let {
                {
                    Button(
                        onClick = {
                            onCancelClick?.invoke()
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(text = it, fontSize = 16.sp)
                    }
                }
            },
            containerColor = Color.White, // Dialog 背景色
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}