package com.wly.beansprout.feature.feedback.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import com.wly.beansprout.presentation.theme.BtnColor
import com.wly.beansprout.presentation.theme.HomeBackground

/**
 * 提交反馈内容
 */
@Composable
fun SubmitFeedbackContent(
    uiState: SubmitFeedbackUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onFileSelect: () -> Unit,
    onFileRemove: (SelectedMedia) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题输入
            Text(
                text = "反馈标题",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("请简要描述问题", fontSize = 14.sp, color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BtnColor,
                    focusedLabelColor = BtnColor,
                    cursorColor = BtnColor
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 内容输入
            Text(
                text = "详细描述",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = uiState.content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = {
                    Text(
                        "请详细描述您遇到的问题或建议...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BtnColor,
                    focusedLabelColor = BtnColor,
                    cursorColor = BtnColor
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 文件选择区域
            Text(
                text = "图片/视频（可选）",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.selectedFiles) { media ->
                    FilePreviewItem(
                        media = media,
                        onRemove = { onFileRemove(media) }
                    )
                }

                item {
                    AddFileItem(onClick = onFileSelect)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 提交按钮
            Surface(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                color = if (uiState.isLoading) Color.Gray else BtnColor
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "提交反馈",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * 文件预览项
 */
@Composable
private fun FilePreviewItem(
    media: SelectedMedia,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = media.uri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 视频标识
        if (media.isVideo) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        // 删除按钮
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
        ) {
            Surface(
                onClick = onRemove,
                modifier = Modifier.size(20.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }
    }
}

/**
 * 添加文件项
 */
@Composable
private fun AddFileItem(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp)),
        color = Color.LightGray.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.Gray)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("添加", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubmitFeedbackContentPreview() {
    val uiState = SubmitFeedbackUiState(
        title = "登录问题",
        content = "打开APP后登录一直加载中..."
    )
    SubmitFeedbackContent(
        uiState = uiState,
        onTitleChange = {},
        onContentChange = {},
        onFileSelect = {},
        onFileRemove = {},
        onSubmit = {}
    )
}
