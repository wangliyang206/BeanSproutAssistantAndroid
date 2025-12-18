package com.wly.beansprout.ui

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
 * 单选按钮
 */
@Composable
fun RadioButtonLayout(name: String, selected: Boolean, onClick: (() -> Unit)?) {
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
            fontSize = 13.sp,
            modifier = Modifier
                .padding(end = 5.dp)
                .clickable {
                    onClick?.invoke()
                }
        )
    }
}