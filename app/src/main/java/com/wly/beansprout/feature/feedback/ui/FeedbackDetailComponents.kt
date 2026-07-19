package com.wly.beansprout.feature.feedback.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wly.beansprout.data.model.Feedback
import com.wly.beansprout.data.model.FeedbackReply
import com.wly.beansprout.data.model.FeedbackStatus
import com.wly.beansprout.data.model.ReplyType
import com.wly.beansprout.presentation.theme.BtnColor
import com.wly.beansprout.presentation.theme.HomeBackground

/**
 * 反馈详情内容（对话流 + 追问输入栏）
 */
@Composable
fun FeedbackDetailContent(
    uiState: FeedbackDetailUiState,
    onRetry: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val repliesSize = uiState.replies.size

    // 有新回复时滚动到底部
    LaunchedEffect(repliesSize) {
        if (repliesSize > 0) {
            listState.animateScrollToItem(repliesSize - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBackground)
    ) {
        when {
            // 加载中
            uiState.isLoading && uiState.feedback == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BtnColor)
                }
            }
            // 错误
            uiState.errorMessage != null && uiState.feedback == null -> {
                val errorMsg = uiState.errorMessage
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMsg.takeIf { it.isNotBlank() } ?: "加载失败",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        onClick = onRetry,
                        shape = RoundedCornerShape(20.dp),
                        color = BtnColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "重试",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            // 有数据
            else -> {
                uiState.feedback?.let { feedback ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 反馈信息卡片
                        item {
                            FeedbackInfoCard(feedback = feedback)
                        }

                        // 回复列表（对话流，时间正序）
                        if (uiState.replies.isNotEmpty()) {
                            item {
                                Text(
                                    text = "对话记录",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            items(uiState.replies, key = { it.replyId }) { reply ->
                                ReplyBubble(reply = reply)
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "暂无回复，可在下方输入消息继续追问",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // 底部追问输入栏
                    ReplyInputBar(
                        text = uiState.inputText,
                        isSending = uiState.isSending,
                        canReply = uiState.canReply,
                        onInputChange = onInputChange,
                        onSend = onSend
                    )
                }
            }
        }
    }
}

/**
 * 反馈信息卡片
 */
@Composable
fun FeedbackInfoCard(feedback: Feedback) {
    val status = FeedbackStatus.fromValue(feedback.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 第一行：状态 + 时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusColor = when (status) {
                    FeedbackStatus.PENDING -> Color(0xFFFF9800)
                    FeedbackStatus.PROCESSING -> Color(0xFF2196F3)
                    FeedbackStatus.REPLIED -> Color(0xFF4CAF50)
                    FeedbackStatus.CLOSED -> Color.Gray
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = status.displayName,
                        fontSize = 12.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = feedback.createTime ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 标题
            Text(
                text = feedback.title,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 内容
            Text(
                text = feedback.content,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * 回复气泡（用户在右，管理员在左）
 */
@Composable
fun ReplyBubble(reply: FeedbackReply) {
    val replyType = ReplyType.fromValue(reply.replyType)
    val isAdmin = replyType == ReplyType.ADMIN

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAdmin) Arrangement.Start else Arrangement.End
    ) {
        if (isAdmin) {
            // 管理员头像（左侧）
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = BtnColor.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (reply.replyUserName?.firstOrNull() ?: "管").toString(),
                        fontSize = 14.sp,
                        color = BtnColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 260.dp),
            horizontalAlignment = if (isAdmin) Alignment.Start else Alignment.End
        ) {
            // 名称 + 时间
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAdmin) {
                    Text(
                        text = reply.replyUserName ?: "管理员",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reply.createTime ?: "",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        text = reply.createTime ?: "",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reply.replyUserName ?: "我",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 气泡内容：管理员白色气泡在左，用户绿色气泡在右
            val bubbleColor = if (isAdmin) Color.White else BtnColor
            val textColor = if (isAdmin) Color(0xFF333333) else Color.White
            val bubbleShape = if (isAdmin) {
                RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
            } else {
                RoundedCornerShape(topStart = 12.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
            }

            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                shadowElevation = if (isAdmin) 1.dp else 0.dp
            ) {
                Text(
                    text = reply.replyContent ?: "",
                    fontSize = 14.sp,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    lineHeight = 20.sp
                )
            }
        }

        if (!isAdmin) {
            Spacer(modifier = Modifier.width(8.dp))
            // 用户头像（右侧）
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = BtnColor
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (reply.replyUserName?.firstOrNull() ?: "我").toString().take(1),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 追问输入栏
 */
@Composable
fun ReplyInputBar(
    text: String,
    isSending: Boolean,
    canReply: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        if (canReply) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入追问内容...", fontSize = 14.sp, color = Color.Gray) },
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BtnColor,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        cursorColor = BtnColor
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (text.isNotBlank() && !isSending) {
                                onSend()
                                keyboardController?.hide()
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (text.isNotBlank() && !isSending) {
                            onSend()
                            keyboardController?.hide()
                        }
                    },
                    enabled = text.isNotBlank() && !isSending,
                    modifier = Modifier.size(44.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = BtnColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送",
                            tint = if (text.isNotBlank()) BtnColor else Color.Gray
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "该反馈已关闭，无法继续追问",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackDetailContentPreview() {
    val feedback = Feedback(
        feedbackId = 1001,
        title = "登录问题",
        content = "打开APP后登录一直加载中，已经检查过网络了，其他APP都能正常上网，就是这个APP登录不了，希望能尽快解决。",
        status = FeedbackStatus.REPLIED.value,
        createTime = "2026-07-18 10:00:00"
    )
    val replies = listOf(
        FeedbackReply(
            replyId = 1,
            replyUserName = "管理员",
            replyContent = "您好，感谢您的反馈。请问您使用的是哪个运营商的网络？是否尝试过切换WiFi和移动数据？",
            replyType = "2",
            createTime = "2026-07-18 11:30:00"
        ),
        FeedbackReply(
            replyId = 2,
            replyUserName = "张三",
            replyContent = "我用的移动网络，切换WiFi也不行，其他APP都正常。",
            replyType = "1",
            createTime = "2026-07-18 12:00:00"
        ),
        FeedbackReply(
            replyId = 3,
            replyUserName = "管理员",
            replyContent = "问题已定位，是服务器负载过高导致的，现已扩容修复，请您重新尝试登录。",
            replyType = "2",
            createTime = "2026-07-18 15:20:00"
        )
    )
    val uiState = FeedbackDetailUiState(
        feedback = feedback,
        replies = replies
    )
    FeedbackDetailContent(
        uiState = uiState,
        onRetry = {},
        onInputChange = {},
        onSend = {}
    )
}
