package com.wly.beansprout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.wly.beansprout.bean.LoginResponse;
import com.wly.beansprout.global.AccountManager;
import com.wly.beansprout.global.Constant;
import com.wly.beansprout.http.MyHttpClient;
import com.wly.beansprout.utils.ToastUtil;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout
 * @ClassName: SplashActivity
 * @Description: 欢迎界面
 * @Author: WLY
 * @CreateDate: 2024/7/24 18:14
 */
public class SplashActivity extends AppCompatActivity {
    public static final String TAG = "SplashActivity";
    // 网络请求
    private MyHttpClient mMyHttpClient;
    private AccountManager mAccountManager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mMyHttpClient = null;
        this.mAccountManager = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 初始化Toast
        ToastUtil.init(this);
        mMyHttpClient = new MyHttpClient(getApplicationContext());
        mAccountManager = new AccountManager(getApplicationContext());


        // 友盟统计 - 同意隐私政策
        UMConfigure.submitPolicyGrantResult(getApplicationContext(), true);
        /*
         * 初始化友盟统计SDK
         *
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调
         * 用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，
         * UMConfigure.init调用中appkey和channel参数请置为null）。
         */
        UMConfigure.init(getApplicationContext(),
                BuildConfig.DEBUG ? getString(R.string.um_app_key_debug) : getString(R.string.um_app_key),
                Constant.UM_CHANNEL,
                UMConfigure.DEVICE_TYPE_PHONE,
                "");
        // 统计SDK是否支持采集在子进程中打点的自定义事件，默认不支持
        UMConfigure.setProcessEvent(true);//支持多进程打点

        // 页面数据采集模式
        // setPageCollectionMode接口参数说明：
        // 1. MobclickAgent.PageMode.AUTO: 建议大多数用户使用本采集模式，SDK在此模式下自动采集Activity
        // 页面访问路径，开发者不需要针对每一个Activity在onResume/onPause函数中进行手动埋点。在此模式下，
        // 开发者如需针对Fragment、CustomView等自定义页面进行页面统计，直接调用MobclickAgent.onPageStart/
        // MobclickAgent.onPageEnd手动埋点即可。此采集模式简化埋点工作，唯一缺点是在Android 4.0以下设备中
        // 统计不到Activity页面数据和各类基础指标(提示：目前Android 4.0以下设备市场占比已经极小)。

        // 2. MobclickAgent.PageMode.MANUAL：对于要求在Android 4.0以下设备中也能正常采集数据的App,可以使用
        // 本模式，开发者需要在每一个Activity的onResume函数中手动调用MobclickAgent.onResume接口，在Activity的
        // onPause函数中手动调用MobclickAgent.onPause接口。在此模式下，开发者如需针对Fragment、CustomView等
        // 自定义页面进行页面统计，直接调用MobclickAgent.onPageStart/MobclickAgent.onPageEnd手动埋点即可。

        // 如下两种LEGACY模式不建议首次集成友盟统计SDK的新用户选用。
        // 如果您是友盟统计SDK的老用户，App需要从老版本统计SDK升级到8.0.0版本统计SDK，
        // 并且：您的App之前MobclickAgent.onResume/onPause接口埋点分散在所有Activity
        // 中，逐个删除修改工作量很大且易出错。
        // 若您的App符合以上特征，可以选用如下两种LEGACY模式，否则不建议继续使用LEGACY模式。
        // 简单来说，升级SDK的老用户，如果不需要手动统计页面路径，选用LEGACY_AUTO模式。
        // 如果需要手动统计页面路径，选用LEGACY_MANUAL模式。
        // 3. MobclickAgent.PageMode.LEGACY_AUTO: 本模式适合不需要对Fragment、CustomView
        // 等自定义页面进行页面访问统计的开发者，SDK仅对App中所有Activity进行页面统计，开发者需要在
        // 每一个Activity的onResume函数中手动调用MobclickAgent.onResume接口，在Activity的
        // onPause函数中手动调用MobclickAgent.onPause接口。此模式下MobclickAgent.onPageStart
        // ,MobclickAgent.onPageEnd这两个接口无效。

        // 4. MobclickAgent.PageMode.LEGACY_MANUAL: 本模式适合需要对Fragment、CustomView
        // 等自定义页面进行手动页面统计的开发者，开发者如需针对Fragment、CustomView等
        // 自定义页面进行页面统计，直接调用MobclickAgent.onPageStart/MobclickAgent.onPageEnd
        // 手动埋点即可。开发者还需要在每一个Activity的onResume函数中手动调用MobclickAgent.onResume接口，
        // 在Activity的onPause函数中手动调用MobclickAgent.onPause接口。
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);


        // 校验是否登录过一次，如果没有，则直接跳转登录界面
        if (TextUtils.isEmpty(mAccountManager.getToken()) || TextUtils.isEmpty(mAccountManager.getUserId())) {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            // 关闭自己
            SplashActivity.this.finish();
        } else {
            mMyHttpClient.validToken()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<LoginResponse>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Log.i(TAG, "onSubscribe");
                        }

                        @Override
                        public void onNext(LoginResponse loginResponse) {
                            Log.i(TAG, "onNext");
                            if (loginResponse != null) {
                                // 将账号密码以及常用信息保存到缓存中
                                mAccountManager.updateAccountInfo(loginResponse);
                                // 重新保存Token
                                mAccountManager.setToken(loginResponse.getToken());

                                // 当用户使用自有账号登录时，可以这样统计：
                                MobclickAgent.onProfileSignIn(loginResponse.getUserId());
                            }

                            Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
                            mIntent.putExtra("mobile", loginResponse.getUserPhone());
                            mIntent.putExtra("status", loginResponse.getStatus());
                            mIntent.putExtra("daysRemaining", loginResponse.getDaysRemaining());
                            startActivity(mIntent);
                            // 关闭自己
                            SplashActivity.this.finish();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.i(TAG, "onError=" + e.getMessage());
                            // 直接进行登录界面
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            // 关闭自己
                            SplashActivity.this.finish();
                        }

                        @Override
                        public void onComplete() {
                            Log.i(TAG, "onComplete");
                        }
                    });
        }

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
