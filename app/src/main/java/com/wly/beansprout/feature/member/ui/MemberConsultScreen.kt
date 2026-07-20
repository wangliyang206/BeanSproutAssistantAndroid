package com.wly.beansprout.feature.member.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wly.beansprout.R
import com.wly.beansprout.presentation.CommTopBar
import com.wly.beansprout.presentation.theme.HomeBackground

/**
 * 开通会员页（显示微信二维码）
 */
@Composable
fun MemberConsultScreen(
    navController: NavController
) {
    CommTopBar(
        title = "开通会员",
        onBack = { navController.popBackStack() }
    ) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(HomeBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 标题
                Text(
                    text = "长按或扫描二维码添加微信",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "添加微信后可咨询以下服务：",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 二维码图片
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(240.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_wechat_qrcode),
                        contentDescription = "微信二维码",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 服务说明
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServiceItem(text = "会员开通与续费咨询")
                    ServiceItem(text = "在线支付与优惠活动")
                    ServiceItem(text = "功能使用问题解答")
                    ServiceItem(text = "其他商务合作咨询")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 提示
                Text(
                    text = "客服在线时间：09:00 - 22:00\n添加后请简要说明您的需求",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ServiceItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "·",
            fontSize = 16.sp,
            color = Color(0xFF4CAF50),
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color.DarkGray
        )
    }
}
