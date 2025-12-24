package com.wly.beansprout.feature.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.wly.beansprout.presentation.theme.JetNewsTheme

/**
 * 登录页
 */
@Composable
fun LoginScreen(
    navController: NavController
) {
    JetNewsTheme {
        Column {
            Text(text = "登录")
        }
    }
}