package com.wly.beansprout.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.wly.beansprout.presentation.GridRadioGroup
import com.wly.beansprout.presentation.theme.BtnColor


/**
 * 顶部
 */
@Composable
fun HomeTop(
    phoneNumber: String,
    userType: String,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier.fillMaxWidth()
    ) {
        // 创建引用
        val (letTopRef, letBottomRef, rightRef) = createRefs()

        // 手机号
        Text(text = phoneNumber,
            fontSize = 14.sp,
            modifier = Modifier.constrainAs(letTopRef) {
                top.linkTo(parent.top, margin = 6.dp)
                start.linkTo(parent.start, margin = 6.dp)
                bottom.linkTo(letBottomRef.top)
            })

        // 用户类型和剩余天数
        Text(
            text = userType,
            fontSize = 12.sp,
            color = BtnColor,
            modifier = Modifier.constrainAs(letBottomRef) {
                top.linkTo(letTopRef.bottom)
                start.linkTo(parent.start, margin = 6.dp)
                bottom.linkTo(parent.bottom, margin = 6.dp)
            }
        )

        // 退出登录按钮
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.constrainAs(rightRef) {
                // 靠右
                end.linkTo(parent.end, margin = 6.dp)
                // 垂直居中
                centerVerticallyTo(parent)
            }) {
            Text(text = "退出登录", fontSize = 12.sp)
        }
    }
}

@Composable
fun HomeCenter(
    selectedExclusive: Int,
    selectedFunctions: Int,
    selectedModel: Int,
    startButtonState: StartButtonState,
    onExclusiveChanged: (Int) -> Unit,
    onFunctionSelected: (Int) -> Unit,
    onModelChanged: (Int) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 设置面板
        SettingsPanel(
            selectedExclusive = selectedExclusive,
            selectedFunctions = selectedFunctions,
            selectedModel = selectedModel,
            onExclusiveChanged = onExclusiveChanged,
            onFunctionSelected = onFunctionSelected,
            onModelChanged = onModelChanged
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 开始按钮
        StartButton(startButtonState = startButtonState, onClick = onStartClick)
    }
}

@Composable
fun SettingsPanel(
    selectedExclusive: Int,
    selectedFunctions: Int,
    selectedModel: Int,
    onExclusiveChanged: (Int) -> Unit,
    onFunctionSelected: (Int) -> Unit,
    onModelChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题
        SettingsPanelTitle(title = "设置")

        // 专属设置
        ExclusiveSetting(
            selectedExclusive = selectedExclusive,
            onExclusiveChanged = onExclusiveChanged
        )

        // 功能设置
        FunctionSetting(
            selectedExclusive = selectedExclusive,
            selectedFunctions = selectedFunctions,
            onFunctionSelected = onFunctionSelected
        )

        // 模型设置
        ModelSetting(
            selectedModel = selectedModel,
            onModelChanged = onModelChanged
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun SettingsPanelTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun ExclusiveSetting(
    selectedExclusive: Int,
    onExclusiveChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "专属：", fontSize = 14.sp)

        GridRadioGroup(
            options = HomeFunctionOptions.exclusiveOptions,
            selected = selectedExclusive,
            onOptionSelected = onExclusiveChanged
        )
    }
}

@Composable
fun FunctionSetting(
    selectedExclusive: Int,
    selectedFunctions: Int,
    onFunctionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "功能：", fontSize = 14.sp)

        // 获取当前平台的功能选项
        val functionOptions = HomeFunctionOptions.getFunctionsForExclusive(selectedExclusive)

        GridRadioGroup(
            options = functionOptions,
            selected = selectedFunctions,
            onOptionSelected = onFunctionSelected
        )
    }
}

@Composable
fun ModelSetting(
    selectedModel: Int,
    onModelChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "模型：", fontSize = 14.sp)

        GridRadioGroup(
            options = HomeFunctionOptions.modelOptions,
            selected = selectedModel,
            onOptionSelected = onModelChanged
        )
    }
}

@Composable
fun StartButton(
    startButtonState: StartButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (buttonText, buttonColor) = when (startButtonState) {
        StartButtonState.NEED_ACCESSIBILITY -> "无障碍服务" to Color(0xFFFF9800)
        StartButtonState.NEED_OVERLAY -> "悬浮窗权限" to Color(0xFFFF9800)
        StartButtonState.READY -> "开始" to BtnColor
        StartButtonState.RUNNING -> "已开启" to Color(0xFFE53935)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.5f)
            .aspectRatio(1f),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text = buttonText,
            fontSize = if (buttonText.length > 2) 20.sp else 30.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 底部
 */
@Composable
fun HomeBottom(
    versionName: String,
    onTutorialClick: () -> Unit = {},
    onServiceAgreementClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 版本号
        Text(
            text = "V$versionName",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 底部链接栏：服务协议 | 使用教程 | 隐私政策
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "服务协议",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier
                    .clickable { onServiceAgreementClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(text = "|", fontSize = 12.sp, color = Color(0xFFCCCCCC))
            Text(
                text = "使用教程",
                fontSize = 12.sp,
                color = Color(0xFF1E88E5),
                modifier = Modifier
                    .clickable { onTutorialClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(text = "|", fontSize = 12.sp, color = Color(0xFFCCCCCC))
            Text(
                text = "隐私政策",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier
                    .clickable { onPrivacyPolicyClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}