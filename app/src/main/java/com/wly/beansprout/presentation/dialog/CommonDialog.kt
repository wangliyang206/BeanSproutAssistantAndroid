package com.wly.beansprout.presentation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
    cornerRadius: Dp = 8.dp,
    title: String? = null,
    content: @Composable () -> Unit,
    confirmText: String? = "确定",
    cancelText: String? = "取消",
    onConfirmClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    dismissible: Boolean = true
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = {
                if (dismissible) {
                    onDismissRequest()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = dismissible,
                dismissOnClickOutside = dismissible,
                usePlatformDefaultWidth = false
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .background(Color.White, RoundedCornerShape(cornerRadius)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题区域
                title?.let {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                    )
                }

                // 内容区域：最低约3行高度，最高约8行高度，超出可滚动
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .heightIn(min = 76.dp, max = 200.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    content()
                }

                // 按钮区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 20.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 取消按钮
                    cancelText?.let {
                        Button(
                            onClick = {
                                onCancelClick?.invoke()
                                onDismissRequest()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(text = it, fontSize = 16.sp)
                        }
                    }

                    // 确认按钮
                    confirmText?.let {
                        Button(
                            onClick = {
                                onConfirmClick?.invoke()
                                onDismissRequest()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF008080),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(text = it, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}