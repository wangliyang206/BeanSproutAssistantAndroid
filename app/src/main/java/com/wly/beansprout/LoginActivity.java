package com.wly.beansprout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.wly.beansprout.bean.LoginResponse;
import com.wly.beansprout.dialog.CommTipsDialog;
import com.wly.beansprout.global.AccountManager;
import com.wly.beansprout.http.MyHttpClient;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout
 * @ClassName: LoginActivity
 * @Description: 登录
 * @Author: WLY
 * @CreateDate: 2024/8/6 16:23
 */
public class LoginActivity extends AppCompatActivity {
    /*--------------------------------控件信息--------------------------------*/
    private TextInputLayout mTextInputMobile;                                                       // 账号
    private TextInputEditText mEditTextMobile;

    private TextInputLayout mTextInputPassword;                                                     // 密码
    private TextInputEditText mEditTextPassword;
    /*--------------------------------业务信息--------------------------------*/
    public static final String TAG = "LoginActivity";
    // 用来销毁 手机号 RxBinding
    private Disposable disposMobile;
    // 用来销毁 密码 RxBinding
    private Disposable disposPassword;

    // 网络请求
    private MyHttpClient mMyHttpClient;
    private AccountManager mAccountManager;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.disposMobile = null;
        this.disposPassword = null;
        this.mMyHttpClient = null;
        this.mAccountManager = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mMyHttpClient = new MyHttpClient(getApplicationContext());
        mAccountManager = new AccountManager(getApplicationContext());
        initView();
        initEvent();
    }

    /**
     * 初始化 View
     */
    private void initView() {
        ImageView btnClose = findViewById(R.id.imvi_login_close);
        mTextInputMobile = findViewById(R.id.input_login_mobile);
        mEditTextMobile = findViewById(R.id.edit_login_mobile);
        mTextInputPassword = findViewById(R.id.input_login_password);
        mEditTextPassword = findViewById(R.id.edit_login_password);
        Button btnLogin = findViewById(R.id.btn_login);

        btnClose.setOnClickListener(v -> onBackPressed());
        btnLogin.setOnClickListener(v -> {
            if (checkInput()) {
                String username = Objects.requireNonNull(mEditTextMobile.getText()).toString().trim();
                String password = Objects.requireNonNull(mEditTextPassword.getText()).toString().trim();

                mMyHttpClient.login(username, password)
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
                                mAccountManager.saveAccountInfo(username, password, loginResponse);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                // 关闭自己
                                LoginActivity.this.finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.i(TAG, "onError=" + e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                Log.i(TAG, "onComplete");
                            }
                        });

            }
        });
    }

    /**
     * 监听事件
     */
    private void initEvent() {
        disposMobile = RxTextView.textChanges(mEditTextMobile)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> {
                    mTextInputMobile.setErrorEnabled(false);
                });

        disposPassword = RxTextView.textChanges(mEditTextPassword)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> {
                    int num = Objects.requireNonNull(mEditTextPassword.getText()).length();
                    mTextInputPassword.setErrorEnabled(false);

                    if (num < 6 || num > 20) {
                        mTextInputPassword.setError("密码长度为6–20位，建议字母与数字组合");
                        mTextInputPassword.setErrorEnabled(true);
                    }
                });
    }

    /**
     * 监听返回键
     */
    @Override
    public void onBackPressed() {
        CommTipsDialog phoneDialog = new CommTipsDialog(this, "温馨提示", "你真的要退出吗？", isVal -> {
            if (isVal) {
                System.exit(0);
            }
        });
        phoneDialog.show();

    }


    /**
     * 用户输入有效性验证
     *
     * @return 校验是否通过
     */
    private boolean checkInput() {

        // 用户名和密码不能为空，为空时返回false同时给出提示。
        String username = Objects.requireNonNull(mEditTextMobile.getText()).toString().trim();
        if ("".equals(username)) {
            mTextInputMobile.setError("您输入的账号不能为空！");
            mTextInputMobile.setErrorEnabled(true);
            return false;
        }

        if (mTextInputMobile.isErrorEnabled()) {
            return false;
        }

        // 账号密码登录
        String password = Objects.requireNonNull(mEditTextPassword.getText()).toString().trim();
        if ("".equals(password)) {
            mTextInputPassword.setError("您输入的密码不能为空！");
            mTextInputPassword.setErrorEnabled(true);
            return false;
        }

        return !mTextInputPassword.isErrorEnabled();
    }
}
