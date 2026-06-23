package com.wly.beansprout.feature.tutorial.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wly.beansprout.presentation.CommTopBar
import com.wly.beansprout.presentation.navigation.NavRoutes
import com.wly.beansprout.presentation.theme.HomeBackground
import java.net.URLEncoder

/**
 * 教程列表页
 *
 * 展示所有教学项（无障碍服务、直播点赞、自动回复、抢福袋等），
 * 点击某项后导航到视频播放页。
 */
@Composable
fun TutorialListScreen(navController: NavController) {
    CommTopBar(
        title = "使用教程",
        onBack = { navController.popBackStack() }
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            items(defaultTutorials) { item ->
                TutorialCard(item = item) {
                    val encodedUrl = URLEncoder.encode(item.videoUrl, "UTF-8")
                    val encodedTitle = URLEncoder.encode(item.title, "UTF-8")
                    navController.navigate("video_player/$encodedTitle/$encodedUrl")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

/**
 * 单个教程卡片
 */
@Composable
private fun TutorialCard(
    item: TutorialItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = "播放",
                tint = Color(0xFF008577),
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}
