package com.wly.beansprout.ui

import android.widget.RadioButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import com.wly.beansprout.R
import com.wly.beansprout.ui.theme.BtnPress
import com.wly.beansprout.ui.theme.HomeBackground
import com.wly.beansprout.ui.theme.JetNewsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout() {
    JetNewsTheme {
        // 顶部 标题
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6200EE), // 背景色
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
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // 创建引用
                    val (letTopRef, letBottomRef, rightRef) = createRefs()

                    Text(text = "15032134297", modifier = Modifier
                        .constrainAs(letTopRef) {
                            top.linkTo(parent.top, margin = 6.dp)
                            start.linkTo(parent.start, margin = 6.dp)
                            bottom.linkTo(letBottomRef.top)
                        })

                    Text(
                        text = "体验用户(剩余12天)", modifier = Modifier
                            .constrainAs(letBottomRef) {
                                top.linkTo(letTopRef.bottom)
                                start.linkTo(parent.start, margin = 6.dp)
                                bottom.linkTo(parent.bottom, margin = 6.dp)
                            }, color = BtnPress
                    )

                    Button(onClick = { /*TODO*/ }, modifier = Modifier.constrainAs(rightRef) {
                        // 靠右
                        end.linkTo(parent.end, margin = 6.dp)
                        // 垂直居中
                        centerVerticallyTo(parent)
                    }, colors = ButtonDefaults.buttonColors(containerColor = BtnPress)) {
                        Text(text = "退出登录")
                    }
                }

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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )

                        GridRadioGroup()
                    }

                }

                // 底部
                Text(
                    text = "V1.3.1测试版",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShowPreview() {
    MainLayout()
}