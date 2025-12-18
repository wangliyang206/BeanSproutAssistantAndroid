package com.wly.beansprout.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 形状文件
 * @author wly
 * @date 2025/03/05
 */

val JetNewsShape = Shapes(
    // 切角形状：把左上角切掉
    small = CutCornerShape(topStart = 8.dp),
//    small = RoundedCornerShape(topStart = 8.dp),
    medium = CutCornerShape(topStart = 24.dp),
    // 圆角形状
    large = RoundedCornerShape(8.dp)
)