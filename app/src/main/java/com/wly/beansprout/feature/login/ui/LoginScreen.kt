package com.wly.beansprout.feature.login.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wly.beansprout.presentation.navigation.NavRoutes
import com.wly.beansprout.presentation.theme.BtnColor
import com.wly.beansprout.presentation.theme.JetNewsTheme

/**
 * 登录页
 */
@Composable
fun LoginScreen(
    navController: NavController
) {
    // 状态管理
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isAgreeProtocol by remember { mutableStateOf(false) }

    // 登录按钮是否可点击（手机号+密码非空 + 同意协议）
    val isLoginEnable = phoneNumber.isNotBlank() && password.isNotBlank() && isAgreeProtocol

    JetNewsTheme {
        // 顶部 标题
        Scaffold { innerPadding ->
            // 整体布局
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 顶部关闭按钮
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    IconButton(
                        onClick = {

                        },
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 标题文本
                Text(
                    text = "您好，",
                    fontSize = 28.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.Start)
                )

                // 副标题
                Text(
                    text = "欢迎使用打工鸡辅助助手",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp, bottom = 40.dp)
                )

                // 手机号输入框
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("手机号") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(0.dp), // 下划线样式（去掉圆角）
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                // 密码输入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val icon =
                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = "切换密码可见性")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 30.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                // 登录按钮
                Button(
                    onClick = {
//                        onLoginClick(phoneNumber, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp), // 直角按钮
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BtnColor, // 深绿色（匹配设计）
                        disabledContainerColor = BtnColor.copy(alpha = 0.5f)
                    ),
                    enabled = isLoginEnable
                ) {
                    Text(
                        text = "登录",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 协议勾选框
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAgreeProtocol,
                        onCheckedChange = { isAgreeProtocol = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = BtnColor,
                            uncheckedColor = Color.Gray
                        )
                    )
                    // 协议文本（带可点击的服务协议/隐私协议）
                    ProtocolText(
                        onServiceAgreementClick = {

                        },
                        onPrivacyAgreementClick = {

                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 新用户注册
                Text(
                    text = "新用户注册",
                    fontSize = 16.sp,
                    color = BtnColor,
                    modifier = Modifier
                        .padding(bottom = 30.dp)
                        .clickable(onClick = {
                            // 导航到注册页面
                            navController.navigate(NavRoutes.Register.route)
                        }),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

}

/**
 * 协议文本（带可点击的服务协议和隐私协议）
 */
@Composable
fun ProtocolText(
    onServiceAgreementClick: () -> Unit,
    onPrivacyAgreementClick: () -> Unit
) {
    // 构建带点击事件的注解文本
    val annotatedText = buildAnnotatedString {
        append("我已经认真阅读、理解并同意")
        // 服务协议（可点击）
        withStyle(style = SpanStyle(color = BtnColor)) {
            append("《服务协议》")
            addStringAnnotation(
                tag = "service",
                annotation = "service_agreement",
                start = length - 6,
                end = length
            )
        }
        append("和")
        // 隐私协议（可点击）
        withStyle(style = SpanStyle(color = BtnColor)) {
            append("《隐私协议》")
            addStringAnnotation(
                tag = "privacy",
                annotation = "privacy_agreement",
                start = length - 6,
                end = length
            )
        }
    }

    ClickableText(
        text = annotatedText,
//        fontSize = 14.sp,
//        color = Color.Gray,
        onClick = { offset ->
            // 处理服务协议点击
            annotatedText.getStringAnnotations(tag = "service", start = offset, end = offset)
                .firstOrNull()?.let {
                    onServiceAgreementClick()
                }
            // 处理隐私协议点击
            annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                .firstOrNull()?.let {
                    onPrivacyAgreementClick()
                }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    val navController = rememberNavController()
    LoginScreen(navController)
}