package com.wly.beansprout.feature.login.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wly.beansprout.presentation.theme.BtnColor

/**
 * 所有UI组件的定义
 */
@Composable
fun LoginTopBar(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "关闭",
                tint = Color.Black
            )
        }
    }
}

@Composable
fun LoginWelcomeHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "您好，",
            fontSize = 28.sp,
            color = Color.Black
        )
        Text(
            text = "欢迎使用打工鸡辅助助手",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 40.dp)
        )
    }
}

@Composable
fun LoginInputSection(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    isPhoneValid: Boolean = true,
    isPasswordValid: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 手机号输入框
        LoginTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = "手机号",
            keyboardType = KeyboardType.Phone,
            isError = !isPhoneValid,
            supportingText = if (!isPhoneValid) {
                { Text(text = "请输入正确的手机号") }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // 密码输入框
        PasswordTextField(
            value = password,
            onValueChange = onPasswordChange,
            isPasswordVisible = isPasswordVisible,
            onVisibilityToggle = onPasswordVisibilityToggle,
            isError = !isPasswordValid,
            supportingText = if (!isPasswordValid) {
                { Text(text = "密码至少6位") }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        )
    }
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        supportingText = supportingText,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Gray,
            errorBorderColor = Color.Red,
            errorLabelColor = Color.Red
        )
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("密码") },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        trailingIcon = {
            IconButton(
                onClick = onVisibilityToggle,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff
                    else Icons.Filled.Visibility,
                    contentDescription = if (isPasswordVisible) {
                        "隐藏密码"
                    } else {
                        "显示密码"
                    },
                    tint = if (isError) Color.Red else Color.Gray
                )
            }
        },
        isError = isError,
        supportingText = supportingText,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Gray,
            errorBorderColor = Color.Red,
            errorLabelColor = Color.Red
        )
    )
}

@Composable
fun LoginButton(
    isEnabled: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BtnColor,
            disabledContainerColor = BtnColor.copy(alpha = 0.5f)
        ),
        enabled = isEnabled
    ) {
        if (isLoading) {
            // 加载状态
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "登录中...",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White
                )
            }
        } else {
            // 正常状态
            Text(
                text = "登录",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun AgreementSection(
    isAgreeProtocol: Boolean,
    onAgreeProtocolChange: (Boolean) -> Unit,
    onServiceAgreementClick: () -> Unit,
    onPrivacyAgreementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isAgreeProtocol,
            onCheckedChange = onAgreeProtocolChange,
            colors = CheckboxDefaults.colors(
                checkedColor = BtnColor,
                uncheckedColor = Color.Gray
            )
        )
        ProtocolText(
            onServiceAgreementClick = onServiceAgreementClick,
            onPrivacyAgreementClick = onPrivacyAgreementClick
        )
    }
}

@Composable
fun RegisterLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "新用户注册",
        fontSize = 16.sp,
        color = BtnColor,
        modifier = modifier
            .padding(bottom = 30.dp)
            .clickable(onClick = onClick),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
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