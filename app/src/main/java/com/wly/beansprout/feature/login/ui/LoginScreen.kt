package com.wly.beansprout.feature.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.wly.beansprout.presentation.theme.JetNewsTheme

/**
 * 登录页
 */
@Composable
fun LoginScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier.background(Color.Red)
    ) {
        Text(text = "登录", color = Color.Black)
    }
}