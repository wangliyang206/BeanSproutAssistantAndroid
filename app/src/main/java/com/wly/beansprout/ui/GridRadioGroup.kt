package com.wly.beansprout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 宫格单选
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GridRadioGroup(radioOptions: List<String>, modifier: Modifier = Modifier) {
    // 2. 互斥状态管理：保存当前选中的选项索引（-1表示未选中）
    var selectedIndex by remember { mutableStateOf(0) }
    // 3. FlowRow自动换行布局（核心）
    FlowRow(
        modifier = modifier
            .padding(horizontal = 1.dp, vertical = 1.dp),
        // 水平间距
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        // 垂直间距
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // 遍历生成10个互斥RadioButton
        radioOptions.forEachIndexed { index, optionText ->
            // 单个RadioButton+文本组合
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                RadioButtonLayout(
                    optionText,
                    // 绑定选中状态
                    selectedIndex == index,
                    // 点击更新选中索引（实现互斥）
                    onClick = { selectedIndex = index }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GridRadioGroupPreview() {
    // 1. 10个选项的数据源（可自定义文本）
    val radioOptions = listOf(
        "轻点触发", "直播点赞", "向下滑动", "向上滑动", "向左滑动",
        "向右滑动", "自动回复", "抢福袋"
    )
    GridRadioGroup(radioOptions)
}