package com.wly.beansprout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.wly.beansprout.R
import com.wly.beansprout.ui.theme.BtnColor
import com.wly.beansprout.ui.theme.HomeBackground
import com.wly.beansprout.ui.theme.JetNewsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout() {
    JetNewsTheme {
        // 添加状态管理
        var selectedExclusive by remember { mutableStateOf(0) }

        // 顶部 标题
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BtnColor,           // 背景色
                        titleContentColor = Color.White,     // 标题颜色
                        actionIconContentColor = Color.White // 图标颜色
                    )
                )
            },
        ) { innerPadding ->
            // 内容
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(HomeBackground)
            ) {
                // 顶部
                HomeTop()

                // 中间
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    // 设置垂直居中
                    verticalArrangement = Arrangement.Center,
                    // 设置水平居中
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(8.dp)) // 添加圆角
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 标题
                        Text(
                            text = "设置",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )

                        // 专属
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(start = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "专属：", fontSize = 14.sp)
                            val oneOpt = listOf(
                                "抖音", "快手", "其它"
                            )
                            GridRadioGroup(oneOpt) { selected ->
                                // 处理专属选择变化
                                selectedExclusive = selected
                            }
                        }

                        // 功能
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "功能：", fontSize = 14.sp)
                            // 1. 初始化选项的数据源
                            val baseRadioOptions = listOf(
                                "轻点触发",
                                "直播点赞",
                                "向下滑动",
                                "向上滑动",
                                "向左滑动",
                                "向右滑动",
                                "自动回复"
                            )

                            val radioOptions = if (selectedExclusive == 0) {
                                baseRadioOptions + "抢福袋"
                            } else {
                                baseRadioOptions
                            }

                            GridRadioGroup(radioOptions, 0)
                        }

                        // 模型
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(start = 10.dp, top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "模型：", fontSize = 14.sp)
                            val oneOpt = listOf(
                                "功德小鸡", "跳绳小鸡"
                            )
                            GridRadioGroup(oneOpt)
                        }
                    }

                    // 分割线
                    Spacer(modifier = Modifier.padding(20.dp))

                    // 开始按钮
                    Button(
                        modifier = Modifier
                            // 宽度为屏幕的70%
                            .fillMaxWidth(0.5f)
                            // 高度与宽度一致
                            .aspectRatio(1f),
                        onClick = { /*TODO*/ },
                        // 设置为圆形
                        shape = CircleShape
                    ) {
                        Text(
                            text = "开始", fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

                // 底部
                HomeBottom()
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainLayoutPreview() {
    MainLayout()
}

/**
 * 顶部
 */
@Composable
fun HomeTop() {
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 创建引用
        val (letTopRef, letBottomRef, rightRef) = createRefs()

        Text(text = "15032134297", modifier = Modifier.constrainAs(letTopRef) {
            top.linkTo(parent.top, margin = 6.dp)
            start.linkTo(parent.start, margin = 6.dp)
            bottom.linkTo(letBottomRef.top)
        })

        Text(
            text = "体验用户(剩余12天)", modifier = Modifier.constrainAs(letBottomRef) {
                top.linkTo(letTopRef.bottom)
                start.linkTo(parent.start, margin = 6.dp)
                bottom.linkTo(parent.bottom, margin = 6.dp)
            }, color = BtnColor
        )

        Button(onClick = { /*TODO*/ }, modifier = Modifier.constrainAs(rightRef) {
            // 靠右
            end.linkTo(parent.end, margin = 6.dp)
            // 垂直居中
            centerVerticallyTo(parent)
        }) {
            Text(text = "退出登录")
        }
    }
}

/**
 * 底部
 */
@Composable
fun HomeBottom() {
    Text(
        text = "V1.3.1测试版",
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}