package com.wly.beansprout.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * 主题文件
 *
 * 1、标准 Material 颜色：MaterialTheme.colorScheme.primary
 * 2、扩展颜色：JetLaggedTheme.extraColors.header
 *
 * @author wly
 * @date 2025/3/5 12:19
 */

/* 浅色方案 */
private val LightColorScheme = lightColorScheme(
    primary = Red700,
    primaryContainer = Red900,
    onPrimary = Color.White,
    secondary = Red700,
    secondaryContainer = Red900,
    onSecondary = Color.White,
    error = Red800,
)

/* 深色配色方案 */
private val DarkColorScheme = darkColorScheme(
    primary = Red300,
    primaryContainer = Red700,
    onPrimary = Color.Black,
    secondary = Red300,
    secondaryContainer = Red700,
    onSecondary = Color.Black,
    error = Red200,
)

// 主题
@Composable
fun JetNewsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // 条件1：支持动态颜色 + Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // 条件2：深色模式
        darkTheme -> DarkColorScheme

        // 默认：浅色模式
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = JetNewsTypography,
        shapes = JetNewsShape,
        content = content
    )
}

// 扩展主题
object JetLaggedTheme {
    val extraColors: JetLaggedExtraColors
        @Composable
        get() = LocalExtraColors.current
}

// 通过 CompositionLocal 提供扩展颜色
val LocalExtraColors = staticCompositionLocalOf {
    JetLaggedExtraColors()
}

data class JetLaggedExtraColors(
    val header: Color = Yellow,
    val cardBackground: Color = Color.White,
    val bed: Color = Lilac,
    val sleep: Color = MintGreen,
    val wellness: Color = LightBlue,
    val heart: Color = Coral,
    val heartWave: List<Color> = listOf(Pink, Purple, Green),
    val heartWaveBackground: Color = Coral.copy(alpha = 0.2f),
    val sleepChartPrimary: Color = Yellow,
    val sleepChartSecondary: Color = YellowVariant,
    val sleepAwake: Color = SleepAwake,
    val sleepRem: Color = SleepRem,
    val sleepLight: Color = SleepLight,
    val sleepDeep: Color = SleepDeep,
    val iconPrimary: Color = Shadow5,
    val iconInteractive: Color = Color.White,
    val iconInteractiveInactive: Color = Neutral1,

    )