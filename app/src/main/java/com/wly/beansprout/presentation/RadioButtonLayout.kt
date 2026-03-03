package com.wly.beansprout.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 单选按钮布局组件
 * @param name 选项名称
 * @param selected 是否选中
 * @param onClick 点击回调
 */
@Composable
fun RadioButtonLayout(
    name: String,
    selected: Boolean,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            Modifier
                .height(30.dp)
                .width(30.dp)
        )

        Text(
            text = name,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(end = 5.dp)
                .clickable {
                    onClick?.invoke()
                }
        )
    }
}