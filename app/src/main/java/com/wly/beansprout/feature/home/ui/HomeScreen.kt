package com.wly.beansprout.feature.home.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wly.beansprout.BuildConfig
import com.wly.beansprout.MainActivity
import com.wly.beansprout.R
import com.wly.beansprout.core.utils.ToastUtils.showToast
import com.wly.beansprout.presentation.CommTopBar
import com.wly.beansprout.presentation.GridRadioGroup
import com.wly.beansprout.presentation.navigation.NavRoutes
import com.wly.beansprout.presentation.theme.BtnColor
import com.wly.beansprout.presentation.theme.HomeBackground
import com.wly.beansprout.presentation.theme.JetNewsTheme

/**
 * 主界面
 */
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current // Compose中获取上下文
    // 安全转换为Activity（避免空指针）
    val activity = context as? MainActivity
    // 上次回退点击时间
    var lastBackPressTime by remember { mutableStateOf(0L) }

    JetNewsTheme {
        // 添加状态管理
        var selectedExclusive by remember { mutableStateOf(0) }

        // 顶部 标题
        CommTopBar(
            title = stringResource(id = R.string.app_name),
            onBack = {},
            content = { modifier ->
                // 内容
                Column(
                    modifier = modifier
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
                            // 分割线
                            Spacer(modifier = Modifier.padding(10.dp))

                            // 标题
                            Text(
                                text = "设置",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )

                            // 分割线
                            Spacer(modifier = Modifier.padding(10.dp))

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

                            // 分割线
                            Spacer(modifier = Modifier.padding(5.dp))
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
        )
    }

    // 拦截首页的回退事件
    BackHandler(
        enabled = navController.currentBackStackEntry?.destination?.route == NavRoutes.Home.route,
        onBack = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                // 调用Activity的退出方法
                activity?.exitApp()
            } else {
                lastBackPressTime = currentTime
                showToast(context, "再按一次返回键退出应用") // 改用Toast提示
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainLayoutPreview() {
    val navController = rememberNavController()
    HomeScreen(navController)
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
        text = "V${BuildConfig.VERSION_NAME}",
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}