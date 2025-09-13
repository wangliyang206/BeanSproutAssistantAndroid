package com.wly.beansprout;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayerActivity extends AppCompatActivity {
    /*--------------------------------控件信息--------------------------------*/
    private VideoView videoView;

    /*--------------------------------业务信息--------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏模式
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        // 隐藏ActionBar（如果存在）
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_videoplayer);

        // 初始化视图
        initViews();

        // 设置视频源（res/raw目录下的video.mp4文件）
        setupVideoSource();
    }

    private void initViews() {
        // 初始化控件
        findViewById(R.id.view_videoplayer_back).setOnClickListener(v -> finish());
        videoView = findViewById(R.id.view_videoplayer_video);
    }

    private void setupVideoSource() {
        try {
            // 设置视频路径，指向res/raw目录下的video.mp4
            // 注意：不需要文件扩展名，只需文件名
//            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.tutorial_video;
//            // 设置视频源
//            videoView.setVideoPath(videoPath);

            // 注意：这里的"video"是你的视频文件名（不含.mp4扩展名）
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_video);

            // 设置视频源
            videoView.setVideoURI(videoUri);

            // 添加媒体控制器（包含播放/暂停、进度条等）
            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);

            // 准备完成后自动播放
            videoView.setOnPreparedListener(mp -> {
                Toast.makeText(VideoPlayerActivity.this, "准备就绪，开始播放", Toast.LENGTH_SHORT).show();
                videoView.start();
            });

            // 播放完成监听
            videoView.setOnCompletionListener(mp -> {
                Toast.makeText(VideoPlayerActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
            });

            // 错误监听
            videoView.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(VideoPlayerActivity.this, "播放错误，请检查视频文件", Toast.LENGTH_SHORT).show();
                return false;
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "初始化视频失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停播放
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        videoView.stopPlayback();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // 再次进入全屏模式
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}