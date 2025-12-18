package com.wly.beansprout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.wly.beansprout.R
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
            Column(modifier = Modifier.padding(innerPadding)) {
                // 顶部
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // 创建引用
                    val (letTopRef, letBottomRef, rightRef) = createRefs()

                    Text(text = "电话", modifier = Modifier
                        .constrainAs(letTopRef) {
                            top.linkTo(parent.top, margin = 6.dp)
                            start.linkTo(parent.start, margin = 6.dp)
                            bottom.linkTo(letBottomRef.top)
                        }
                        .background(Color.Red))

                    Text(text = "提示", modifier = Modifier
                        .constrainAs(letBottomRef) {
                            top.linkTo(letTopRef.bottom, margin = 6.dp)
                            start.linkTo(parent.start, margin = 6.dp)
                            bottom.linkTo(parent.bottom, margin = 6.dp)
                        }
                        .background(Color.Blue))

                    Button(onClick = { /*TODO*/ }, modifier = Modifier.constrainAs(rightRef) {
                        // 靠右
                        end.linkTo(parent.end, margin = 6.dp)
                        // 垂直居中
                        centerVerticallyTo(parent)
                    }, colors = ButtonDefaults.buttonColors()) {
                        Text(text = "退出登录")
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShowPreview() {
    MainLayout()
}