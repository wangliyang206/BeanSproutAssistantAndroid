package com.wly.beansprout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.wly.beansprout.bean.AppUpdate;
import com.wly.beansprout.bean.TouchEvent;
import com.wly.beansprout.fw_permission.FloatWinPermissionCompat;
import com.wly.beansprout.http.MyHttpClient;
import com.wly.beansprout.service.AutoTouchService;
import com.wly.beansprout.service.FloatingService;
import com.wly.beansprout.utils.AccessibilityUtil;
import com.wly.beansprout.utils.CommonUtils;
import com.wly.beansprout.utils.ToastUtil;
import com.wly.beansprout.utils.WindowUtils;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    // 网络请求
    private MyHttpClient mMyHttpClient;

    private final String STRING_START = "开始";
    private final String STRING_ACCESS = "无障碍服务";
    private final String STRING_ALERT = "悬浮窗权限";
    private final String STRING_OPEN = "已开启";

    // 功能
    private RadioGroup groupFunction;
    // 动画
    private RadioGroup groupAnimation;
    // 开始
    private TextView tvStart;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMyHttpClient = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMyHttpClient = new MyHttpClient(getApplicationContext());
        tvStart = findViewById(R.id.tv_start);
        tvStart.setOnClickListener(v -> {
            switch (tvStart.getText().toString()) {
                case STRING_START:
                    ToastUtil.show(getString(R.string.app_name) + "已启用");
                    Intent mIntent = new Intent(MainActivity.this, FloatingService.class);
                    mIntent.putExtra("functionType", getFunctionType());
                    mIntent.putExtra("chickModel", getChickModel());
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

        // 专属
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

        // 功能
        groupFunction = findViewById(R.id.ragr_function);

        // 动画
        groupAnimation = findViewById(R.id.ragr_animation);

        // 检查更新
        mMyHttpClient.getVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AppUpdate>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.i("main", "onSubscribe");
                    }

                    @Override
                    public void onNext(AppUpdate appUpdate) {
                        Log.i("main", "onNext");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("main", "onError="+e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.i("main", "onComplete");
                    }
                });
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

    /**
     * 获取功能类型
     */
    private int getFunctionType() {
        if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_singleclick) {
            // 单击
            return 1;
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_like) {
            // 点赞
            return 2;
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_slidedDown) {
            // 向下滑动
            return 3;
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_slideUpAndDown) {
            // 上下滑动
            return 4;
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_slidingLeft) {
            // 向左滑动
            return 5;
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_slidingRight) {
            // 向右滑动
            return 6;
        } else {
            // 其它
            return 0;
        }
    }

    /**
     * 获取小鸡模型
     */
    private int getChickModel() {
        if (groupAnimation.getCheckedRadioButtonId() == R.id.cb_animation_goldenHairedChick) {
            // 功德小鸡
            return 1;
        } else {
            // 其它小鸡
            return 2;
        }
    }

    @Override
    public void onBackPressed() {
        CommonUtils.exitSys(this);
    }
}
