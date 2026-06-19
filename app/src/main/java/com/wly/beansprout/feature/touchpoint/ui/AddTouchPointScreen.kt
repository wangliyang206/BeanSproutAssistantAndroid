package com.wly.beansprout.feature.touchpoint.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wly.beansprout.feature.touchpoint.viewmodel.TouchPointViewModel

/**
 * 添加触点页面
 * 半透明全屏覆盖，点击屏幕任意位置捕获坐标，然后输入名称和间隔时间
 */
@Composable
fun AddTouchPointScreen(
    navController: NavController,
    viewModel: TouchPointViewModel = hiltViewModel()
) {
    var capturedX by remember { mutableIntStateOf(-1) }
    var capturedY by remember { mutableIntStateOf(-1) }
    var isCaptured by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var delayText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x77000000))
    ) {
        if (!isCaptured) {
            // 阶段 1: 点击屏幕捕获坐标
            Text(
                text = "点击屏幕任意位置记录坐标",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 30.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            // 透明覆盖层捕获点击
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val pointer = event.changes.firstOrNull()
                                if (pointer != null && pointer.pressed) {
                                    capturedX = pointer.position.x.toInt()
                                    capturedY = pointer.position.y.toInt()
                                    isCaptured = true
                                    pointer.consume()
                                    break
                                }
                            }
                        }
                    }
            )
        } else {
            // 阶段 2: 输入名称和间隔
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "添加点击位置",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "坐标: ($capturedX, $capturedY)",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = delayText,
                    onValueChange = { delayText = it },
                    label = { Text("点击间隔时间(毫秒)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "建议设置500~800毫秒，低于500手机会爆炸",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )

                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText, color = Color.Red, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 按钮行
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Button(
                        onClick = {
                            val delay = delayText.toIntOrNull() ?: 0
                            if (name.isBlank() || delay <= 0) {
                                errorText = "名字或毫秒数错误"
                                return@Button
                            }
                            viewModel.addTouchPoint(name, capturedX, capturedY, delay)
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("保存", color = Color.White)
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("取消", color = Color.White)
                    }
                }
            }
        }
    }
}
