package com.wly.beansprout.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 封装一个通用的TopBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommTopBar(
    title: String,
    onBack: () -> Unit,
    content: @Composable (Modifier) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // 背景色
                    titleContentColor = Color.White,     // 标题颜色
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White // 图标颜色
                ),
                navigationIcon = {
                    // 返回按钮
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}