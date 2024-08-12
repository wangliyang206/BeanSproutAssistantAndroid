package com.wly.beansprout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.qiangxi.checkupdatelibrary.CheckUpdateOption;
import com.qiangxi.checkupdatelibrary.Q;
import com.umeng.analytics.MobclickAgent;
import com.wly.beansprout.bean.AppUpdate;
import com.wly.beansprout.bean.TouchEvent;
import com.wly.beansprout.fw_permission.FloatWinPermissionCompat;
import com.wly.beansprout.global.AccountManager;
import com.wly.beansprout.global.TouchEventManager;
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
    private AccountManager mAccountManager;

    private final String STRING_START = "开始";
    private final String STRING_ACCESS = "无障碍服务";
    private final String STRING_ALERT = "悬浮窗权限";
    private final String STRING_OPEN = "已开启";

    // 功能
    private RadioGroup groupFunction;
    // 自动回复
    private RadioButton floatingScreen;
    // 动画
    private RadioGroup groupAnimation;
    // 开始
    private TextView tvStart;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mMyHttpClient = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 友盟统计 - 自定义事件
        MobclickAgent.onEvent(getApplicationContext(), "open_main");

        // 接收外部提供的参数
        int status = getIntent().getIntExtra("status", 0);
        int daysRemaining = getIntent().getIntExtra("daysRemaining", 0);

        // 初始化
        mMyHttpClient = new MyHttpClient(getApplicationContext());
        mAccountManager = new AccountManager(getApplicationContext());
        TextView tvUserName = findViewById(R.id.txvi_username);
        TextView tvState = findViewById(R.id.txvi_state);
        TextView tvOutLogin = findViewById(R.id.txvi_outlogin);
        tvStart = findViewById(R.id.tv_start);
        // 专属
        RadioGroup radioGroup = findViewById(R.id.ragr_exclusive);
        // 功能
        groupFunction = findViewById(R.id.ragr_function);
        floatingScreen = findViewById(R.id.cb_function_floatingScreen);
        // 动画
        groupAnimation = findViewById(R.id.ragr_animation);

        // 赋值
        tvUserName.setText(getIntent().getStringExtra("mobile"));
        tvState.setText(status == 4 ? "正式用户" : "体验用户(剩余" + daysRemaining + "天)");
        tvOutLogin.setOnClickListener(v -> {
            // 登出
            MobclickAgent.onProfileSignOff();
            mAccountManager.setToken("");
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            MainActivity.this.finish();
        });
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

        // 专属
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.cb_exclusive_tiktok) {
                // 抖音
                TouchEventManager.getInstance().setAppPackageName(1);
                floatingScreen.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.cb_exclusive_kwai) {
                // 快手
                TouchEventManager.getInstance().setAppPackageName(2);
                floatingScreen.setVisibility(View.GONE);
            } else {
                // 没有专属
                TouchEventManager.getInstance().setAppPackageName(3);
                floatingScreen.setVisibility(View.GONE);
            }
        });

        // 检查更新
        getVersion();
    }

    /**
     * 检查更新
     */
    private void getVersion() {
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
                        if (haveNew(appUpdate)) {
                            // 先提醒升级
                            askDialog(appUpdate);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("main", "onError=" + e.getMessage());
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
        int index = 0;
        String value = "";
        if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_singleclick) {
            // 轻点触发(单点)
            index = 1;
            value = "lightlyTrigger";
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_like) {
            // 直播点赞
            index = 2;
            value = "liveStreamingLikes";
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_slidedDown) {
            // 向下滑动
            index = 3;
            value = "slideDown";
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_slideUpAndDown) {
            // 上下滑动
            index = 4;
            value = "wipeUp";
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_slidingLeft) {
            // 向左滑动
            index = 5;
            value = "swipeLeft";
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_slidingRight) {
            // 向右滑动
            index = 6;
            value = "swipeRight";
        } else if (groupFunction.getCheckedRadioButtonId() == R.id.cb_function_floatingScreen) {
            // 自动回复
            index = 7;
            value = "autoReply";
        }

        // 友盟统计 - 自定义事件
        MobclickAgent.onEvent(getApplicationContext(), value);
        return index;
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

    /**
     * 版本号比较
     *
     * @return 是否升级
     */
    private boolean haveNew(AppUpdate appUpdate) {
        boolean haveNew = false;
        if (appUpdate == null) {
            return false;
        }

        int curVersionCode = CommonUtils.getVersionCode(getApplicationContext());
        if (curVersionCode < appUpdate.getVerCode()) {
            haveNew = true;
        }
        return haveNew;
    }

    public void askDialog(AppUpdate info) {
        Q.show(this, new CheckUpdateOption.Builder()
                .setAppName(info.getName())
                .setFileName("/" + info.getFileName())
                .setFilePath(getFilesDir().getPath())
//                .setImageUrl("http://imgsrc.baidu.com/imgad/pic/item/6c224f4a20a446233d216c4f9322720e0cf3d730.jpg")
                .setImageResId(R.mipmap.icon_upgrade_logo)
                .setIsForceUpdate(info.getAppForce() == 1)
                .setNewAppSize(info.getNewAppSize())
                .setNewAppUpdateDesc(info.getNewAppUpdateDesc())
                .setNewAppUrl(info.getFilePath())
                .setNewAppVersionName(info.getVerName())
                .setNotificationSuccessContent("下载成功，点击安装")
                .setNotificationFailureContent("下载失败，点击重新下载")
                .setNotificationIconResId(R.mipmap.ic_launcher)
                .setNotificationTitle(getString(R.string.app_name))
                .setMode(2)
                .build(), (view, imageUrl) -> {
            // 下载图片
//            view.setScaleType(ImageView.ScaleType.FIT_XY);
//            mImageLoader.loadImage(getActivity(),
//                    ImageConfigImpl
//                            .builder()
//                            .url(imageUrl)
//                            .imageView(view)
//                            .build());
        });
    }

    @Override
    public void onBackPressed() {
        CommonUtils.exitSys(this);
    }
}
