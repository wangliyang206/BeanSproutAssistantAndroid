package com.wly.beansprout.feature.login.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.wly.beansprout.R
import com.wly.beansprout.core.utils.StatusBarSetup
import com.wly.beansprout.presentation.theme.BtnColor
import com.wly.beansprout.presentation.theme.JetNewsTheme

/**
 * 登录页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController
) {
    // 调用通用方法设置状态栏 - 白色图标（因为 BtnColor 是深色）
    StatusBarSetup(
        statusBarColor = BtnColor,  // 使用与 TopAppBar 相同的背景色
        darkIcons = false           // false 表示白色图标
    )

    JetNewsTheme {
        // 顶部 标题
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BtnColor,           // 背景色
                        titleContentColor = Color.White,     // 标题颜色
                        actionIconContentColor = Color.White // 图标颜色
                    )
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(Color.Red)
            ) {
                Text(text = "登录", color = Color.Black)
            }
        }
    }

}