package com.wly.beansprout.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wly.beansprout.feature.home.viewmodel.HomeViewModel

/**
 * 主界面
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    HomeScreenContent(
        navController = navController,
        viewModel = viewModel
    )
}

@Preview(showBackground = true)
@Composable
fun MainLayoutPreview() {
    val navController = rememberNavController()
    HomeScreen(navController)
}