package com.wly.beansprout.presentation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 设置状态栏样式
 * @param statusBarColor 状态栏背景颜色
 * @param darkIcons 状态栏图标是否为深色（true为黑色，false为白色）
 *
 * 使用示例：
 *     StatusBarSetup(
 *         statusBarColor = BtnColor,  // 使用与 TopAppBar 相同的背景色
 *         darkIcons = false           // false 表示白色图标
 *     )
 */
@Composable
fun StatusBarSetup(
    statusBarColor: Color = Color.Transparent,
    darkIcons: Boolean = true
) {
    val context = LocalContext.current
    val window = (context as? Activity)?.window

    DisposableEffect(window, statusBarColor, darkIcons) {
        window?.let {
            // 设置状态栏背景色
            it.statusBarColor = statusBarColor.toArgb()

            // 设置状态栏图标颜色
            WindowInsetsControllerCompat(it, it.decorView).apply {
                isAppearanceLightStatusBars = darkIcons
            }
        }

        onDispose {
            // 可选：在离开时恢复默认设置
        }
    }
}
