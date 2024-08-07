package com.wly.beansprout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.wly.beansprout.bean.LoginResponse;
import com.wly.beansprout.global.AccountManager;
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
                            }

                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
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
