package com.wly.beansprout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout
 * @ClassName: SplashActivity
 * @Description: 欢迎界面
 * @Author: WLY
 * @CreateDate: 2024/7/24 18:14
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // 创建一个Handler
        Handler handler = new Handler();
        // 定义一个Runnable
        Runnable runnable = () -> {
            // 延迟两秒后跳转
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            // 关闭自己
            SplashActivity.this.finish();
        };
        // 延迟2秒执行
        handler.postDelayed(runnable, 2000); // 延迟时间为2秒（2000毫秒）
    }

    /**
     * 屏蔽返回按钮
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return false;
    }

    /**
     * 屏蔽返回按钮
     */
    @Override
    public void onBackPressed() {

    }
}
