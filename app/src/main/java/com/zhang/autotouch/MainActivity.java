package com.zhang.autotouch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.zhang.autotouch.bean.TouchEvent;
import com.zhang.autotouch.fw_permission.FloatWinPermissionCompat;
import com.zhang.autotouch.service.AutoTouchService;
import com.zhang.autotouch.service.FloatingService;
import com.zhang.autotouch.utils.AccessibilityUtil;
import com.zhang.autotouch.utils.ToastUtil;
import com.zhang.autotouch.utils.WindowUtils;


@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    private final String STRING_START = "开始";
    private final String STRING_ACCESS = "无障碍服务";
    private final String STRING_ALERT = "悬浮窗权限";
    private final String STRING_OPEN = "已开启";

    // 开始
    private TextView tvStart;
    // 点赞
    private CheckBox ckboxFunction;
    // 弹幕欢迎
    private CheckBox ckboxBulletchat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStart = findViewById(R.id.tv_start);
        tvStart.setOnClickListener(v -> {
            switch (tvStart.getText().toString()) {
                case STRING_START:
                    ToastUtil.show(getString(R.string.app_name) + "已启用");
                    Intent mIntent = new Intent(MainActivity.this, FloatingService.class);
                    mIntent.putExtra("isFunction", ckboxFunction.isChecked());
                    startService(mIntent);
                    moveTaskToBack(true);
                    break;
                case STRING_OPEN:
                    ToastUtil.show(getString(R.string.app_name) + "已关闭");
                    stopService(new Intent(MainActivity.this, FloatingService.class));
                    TouchEvent.postStopAction();
                    tvStart.setText(STRING_START);
                    break;
                case STRING_ALERT:
                    requestPermissionAndShow();
                    break;
                case STRING_ACCESS:
                    requestAcccessibility();
                    break;
                default:
                    break;
            }
        });
        // 初始化Toast
        ToastUtil.init(this);

        RadioGroup radioGroup = findViewById(R.id.ragr_exclusive);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.cb_exclusive_tiktok) {
                // 抖音
                TouchEventManager.getInstance().setAppPackageName(1);
            } else if (checkedId == R.id.cb_exclusive_kwai) {
                // 快手
                TouchEventManager.getInstance().setAppPackageName(2);
            } else {
                // 没有专属
                TouchEventManager.getInstance().setAppPackageName(3);
            }
        });

        // 点赞
        LinearLayout functionLayout = findViewById(R.id.lila_function_layout);
        ckboxFunction = findViewById(R.id.chk_function_ckbox);
        functionLayout.setOnClickListener(v -> ckboxFunction.setChecked(!ckboxFunction.isChecked()));

        // 弹幕欢迎
        LinearLayout bulletchatLayout = findViewById(R.id.lila_bulletchat_layout);
        ckboxBulletchat = findViewById(R.id.chk_bulletchat_ckbox);
        bulletchatLayout.setOnClickListener(v -> ckboxBulletchat.setChecked(!ckboxBulletchat.isChecked()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkState();
    }

    private void checkState() {
        boolean hasAccessibility = AccessibilityUtil.isSettingOpen(AutoTouchService.class, MainActivity.this);
        boolean hasWinPermission = FloatWinPermissionCompat.getInstance().check(this);
        if (hasAccessibility) {
            if (hasWinPermission) {
                if (WindowUtils.isServiceWork(getApplicationContext(), FloatingService.class.getName())) {
                    tvStart.setText(STRING_OPEN);
                } else {
                    tvStart.setText(STRING_START);
                }

            } else {
                tvStart.setText(STRING_ALERT);
            }
        } else {
            tvStart.setText(STRING_ACCESS);
        }
    }


    private void requestAcccessibility() {
        new AlertDialog.Builder(this).setTitle("无障碍服务未开启")
                .setMessage("你的手机没有开启无障碍服务，" + getString(R.string.app_name) + "将无法正常使用")
                .setPositiveButton("去开启", (dialog, which) -> {
                    // 显示授权界面
                    try {
                        AccessibilityUtil.jumpToSetting(MainActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    /**
     * 开启悬浮窗权限
     */
    private void requestPermissionAndShow() {
        new AlertDialog.Builder(this).setTitle("悬浮窗权限未开启")
                .setMessage(getString(R.string.app_name) + "获得悬浮窗权限，才能正常使用应用")
                .setPositiveButton("去开启", (dialog, which) -> {
                    // 显示授权界面
                    try {
                        FloatWinPermissionCompat.getInstance().apply(MainActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("取消", null).show();
    }
}
