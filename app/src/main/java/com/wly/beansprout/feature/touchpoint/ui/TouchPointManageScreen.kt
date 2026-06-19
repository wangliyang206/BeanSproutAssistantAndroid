package com.wly.beansprout.feature.touchpoint.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wly.beansprout.data.model.TouchPoint
import com.wly.beansprout.feature.touchpoint.viewmodel.TouchPointEvent
import com.wly.beansprout.feature.touchpoint.viewmodel.TouchPointViewModel
import com.wly.beansprout.presentation.CommTopBar
import com.wly.beansprout.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest

/**
 * 触点管理页面
 * 展示已保存的触点列表，支持添加/删除/编辑
 */
@Composable
fun TouchPointManageScreen(
    navController: NavController,
    viewModel: TouchPointViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TouchPointEvent.TouchPointAdded -> viewModel.loadTouchPoints()
                is TouchPointEvent.TouchPointDeleted -> viewModel.loadTouchPoints()
                else -> Unit
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部导航栏
        CommTopBar(
            title = "触点管理",
            onBack = { navController.popBackStack() }
        )

        // 操作按钮行
        ActionButtonsRow(
            isTouching = uiState.touchPoints.any { it.isStartClick },
            onAddClick = { navController.navigate(NavRoutes.AddTouchPoint.route) },
            onStopClick = { /* TODO: Day 7-8 悬浮窗集成 */ },
            onReplyClick = { viewModel.showAutoReplyDialog() },
            onExitClick = { navController.popBackStack() }
        )

        // 标题
        Text(
            text = "触控点列表",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 触点列表
        if (uiState.touchPoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无触控点，点击「添加触控点」开始",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(uiState.touchPoints) { index, point ->
                    TouchPointItem(
                        touchPoint = point,
                        onStartClick = { viewModel.toggleTouchPointClick(index) },
                        onDeleteClick = { viewModel.deleteTouchPoint(index) }
                    )
                    if (index < uiState.touchPoints.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    // 自动回复话术对话框
    if (uiState.showAutoReplyDialog) {
        AutoReplyScriptDialog(
            currentScript = uiState.autoReplyScript,
            onSave = { viewModel.saveAutoReplyScript(it) },
            onDismiss = { viewModel.dismissAutoReplyDialog() }
        )
    }
}

/**
 * 操作按钮行
 */
@Composable
private fun ActionButtonsRow(
    isTouching: Boolean,
    onAddClick: () -> Unit,
    onStopClick: () -> Unit,
    onReplyClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isTouching) {
            Button(
                onClick = onStopClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("停止触控", color = Color.White, fontSize = 14.sp)
            }
        }

        Button(
            onClick = onAddClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("添加触控点", color = Color.White, fontSize = 14.sp)
        }

        Button(
            onClick = onReplyClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("回复话术", color = Color.White, fontSize = 14.sp)
        }

        Button(
            onClick = onExitClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("退出", color = Color.White, fontSize = 14.sp)
        }
    }
}

/**
 * 触点列表项
 */
@Composable
private fun TouchPointItem(
    touchPoint: TouchPoint,
    onStartClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onStartClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (touchPoint.isStartClick)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = touchPoint.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "间隔(${touchPoint.delay}ms)  坐标(${touchPoint.x},${touchPoint.y})",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (!touchPoint.isStartClick) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFFFF4444)
                    )
                }
            } else {
                Text(
                    text = "运行中",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
