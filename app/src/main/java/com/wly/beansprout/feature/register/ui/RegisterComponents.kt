package com.wly.beansprout.feature.register.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
 * 注册页 - 顶部返回栏 + 标题
 */
@Composable
fun RegisterTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.Black
            )
        }
        Text(
            text = "注册",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * 注册页 - 欢迎标题
 */
@Composable
fun RegisterWelcomeHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "创建新账号",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "请填写以下信息完成注册",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 30.dp)
        )
    }
}

/**
 * 注册页 - 输入区域（手机号 + 密码 + 确认密码）
 */
@Composable
fun RegisterInputSection(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    isConfirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    isPhoneValid: Boolean,
    isPasswordValid: Boolean,
    isConfirmPasswordValid: Boolean,
    hasStartedPhoneInput: Boolean,
    hasStartedPasswordInput: Boolean,
    hasStartedConfirmPasswordInput: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 手机号输入框
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("手机号") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = hasStartedPhoneInput && !isPhoneValid,
            supportingText = if (hasStartedPhoneInput && !isPhoneValid) {
                { Text("请输入有效的手机号！") }
            } else null,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(0.dp),
            colors = registerTextFieldColors()
        )

        // 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("密码") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(
                    onClick = onPasswordVisibilityToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = if (isPasswordVisible) "隐藏密码" else "显示密码",
                        tint = if (hasStartedPasswordInput && !isPasswordValid) Color.Red else Color.Gray
                    )
                }
            },
            isError = hasStartedPasswordInput && !isPasswordValid,
            supportingText = if (hasStartedPasswordInput && !isPasswordValid) {
                { Text("密码长度为6–20位，建议字母与数字组合") }
            } else null,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(0.dp),
            colors = registerTextFieldColors()
        )

        // 确认密码输入框
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("确认密码") },
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(
                    onClick = onConfirmPasswordVisibilityToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = if (isConfirmPasswordVisible) "隐藏密码" else "显示密码",
                        tint = if (hasStartedConfirmPasswordInput && !isConfirmPasswordValid) Color.Red else Color.Gray
                    )
                }
            },
            isError = hasStartedConfirmPasswordInput && !isConfirmPasswordValid,
            supportingText = if (hasStartedConfirmPasswordInput && !isConfirmPasswordValid) {
                { Text("请确保确认密码与密码输入一致！") }
            } else null,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            shape = RoundedCornerShape(0.dp),
            colors = registerTextFieldColors()
        )
    }
}

/**
 * 注册按钮
 */
@Composable
fun RegisterButton(
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
                    text = "注册中...",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
        } else {
            Text(
                text = "注册",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }
    }
}

/**
 * 注册页协议区域
 */
@Composable
fun RegisterAgreementSection(
    isAgreeProtocol: Boolean,
    onAgreeProtocolChange: (Boolean) -> Unit,
    onServiceAgreementClick: () -> Unit,
    onPrivacyAgreementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
        RegisterProtocolText(
            onServiceAgreementClick = onServiceAgreementClick,
            onPrivacyAgreementClick = onPrivacyAgreementClick
        )
    }
}

/**
 * 注册页协议文本（带可点击链接）
 */
@Composable
fun RegisterProtocolText(
    onServiceAgreementClick: () -> Unit,
    onPrivacyAgreementClick: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        append("我已经认真阅读、理解并同意")
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
        withStyle(style = SpanStyle(color = BtnColor)) {
            append("《隐私政策》")
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
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "service", start = offset, end = offset)
                .firstOrNull()?.let { onServiceAgreementClick() }
            annotatedText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                .firstOrNull()?.let { onPrivacyAgreementClick() }
        }
    )
}

/**
 * 统一的输入框颜色配置
 */
@Composable
private fun registerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.Black,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Color.Black,
    unfocusedLabelColor = Color.Gray,
    errorBorderColor = Color.Red,
    errorLabelColor = Color.Red
)
