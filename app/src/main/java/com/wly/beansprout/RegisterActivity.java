package com.wly.beansprout;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.wly.beansprout.bean.CommonResponse;
import com.wly.beansprout.global.AccountManager;
import com.wly.beansprout.http.MyHttpClient;
import com.wly.beansprout.utils.CommonUtils;
import com.wly.beansprout.utils.StatusBarCompatUtils;
import com.wly.beansprout.utils.ToastUtil;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import qiu.niorgai.StatusBarCompat;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout
 * @ClassName: RegisterActivity
 * @Description: 注册
 * @Author: WLY
 * @CreateDate: 2024/8/7 10:10
 */
public class RegisterActivity extends AppCompatActivity {
    /*--------------------------------控件信息--------------------------------*/
    private TextInputLayout mTextInputMobile;                                                       // 账号
    private TextInputEditText mEditTextMobile;

    private TextInputLayout mTextInputPassword;                                                     // 密码
    private TextInputEditText mEditTextPassword;

    private TextInputLayout mTextInputConfirmPassword;                                              // 确认密码
    private TextInputEditText mEditTextConfirmPassword;
    /*--------------------------------业务信息--------------------------------*/
    public static final String TAG = "RegisterActivity";
    // 用来销毁 手机号 RxBinding
    private Disposable disposMobile;
    // 用来销毁 密码 RxBinding
    private Disposable disposPassword;
    private Disposable disposConfirmPassword;

    // 网络请求
    private MyHttpClient mMyHttpClient;
    private AccountManager mAccountManager;
    // 登录对话框
    private MaterialDialog mDialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.disposMobile = null;
        this.disposPassword = null;
        this.disposConfirmPassword = null;
        this.mMyHttpClient = null;
        this.mAccountManager = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setStatusBar();
        // 初始化Loading对话框
        mDialog = new MaterialDialog.Builder(this).content("正在请求中，请稍候……").progress(true, 0).build();
        mMyHttpClient = new MyHttpClient(getApplicationContext());
        mAccountManager = new AccountManager(getApplicationContext());
        initView();
        initEvent();
    }

    /**
     * 设置状态栏
     */
    public void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //SDK >= 21时, 取消状态栏的阴影
            StatusBarCompat.translucentStatusBar(this, true);
        } else {
            //透明状态栏
            StatusBarCompat.translucentStatusBar(this);
        }

        StatusBarCompatUtils.cancelLightStatusBar(this);
    }

    /**
     * 初始化 View
     */
    private void initView() {
        mTextInputMobile = findViewById(R.id.input_register_mobile);
        mEditTextMobile = findViewById(R.id.edit_register_mobile);
        mTextInputPassword = findViewById(R.id.input_register_password);
        mEditTextPassword = findViewById(R.id.edit_register_password);
        mTextInputConfirmPassword = findViewById(R.id.input_register_confirmpassword);
        mEditTextConfirmPassword = findViewById(R.id.edit_register_confirmpassword);
        Button btnRegister = findViewById(R.id.btn_register);
        ImageView imviBack = findViewById(R.id.imgvi_back);
        TextView txviTitle = findViewById(R.id.txvi_title);

        txviTitle.setText("注册");
        // 关闭按钮
        imviBack.setOnClickListener(v -> onBackPressed());
        // 注册按钮
        btnRegister.setOnClickListener(v -> {
            hideInput();
            if (checkInput()) {
                String username = Objects.requireNonNull(mEditTextMobile.getText()).toString().trim();
                String password = Objects.requireNonNull(mEditTextPassword.getText()).toString().trim();
                mDialog.show();

                mMyHttpClient.register(username, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<CommonResponse>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                Log.i(TAG, "onSubscribe");
                            }

                            @Override
                            public void onNext(CommonResponse loginResponse) {
                                Log.i(TAG, "onNext");
                                mDialog.dismiss();
                                runOnUiThread(() -> ToastUtil.show("注册成功！"));
                                // 关闭自己
                                RegisterActivity.this.finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.i(TAG, "onError=" + e.getMessage());
                                mDialog.dismiss();
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


        disposConfirmPassword = RxTextView.textChanges(mEditTextConfirmPassword)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> {
                    int num = Objects.requireNonNull(mEditTextConfirmPassword.getText()).length();
                    mTextInputConfirmPassword.setErrorEnabled(false);

                    if (num == 0) {
                        mTextInputConfirmPassword.setError("请输入密码！");
                        mTextInputConfirmPassword.setErrorEnabled(true);
                    }

                    if (!mEditTextConfirmPassword.getText().toString().equalsIgnoreCase(mEditTextPassword.getText().toString())) {
                        mTextInputConfirmPassword.setError("请确保确认密码与密码输入一至！");
                        mTextInputConfirmPassword.setErrorEnabled(true);
                    }
                });
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

        if (mTextInputPassword.isErrorEnabled()) {
            return false;
        }

        String confirmPassword = Objects.requireNonNull(mEditTextConfirmPassword.getText()).toString().trim();
        if ("".equals(confirmPassword)) {
            mTextInputConfirmPassword.setError("您输入的确认密码不能为空！");
            mTextInputConfirmPassword.setErrorEnabled(true);
            return false;
        }

        if (mTextInputConfirmPassword.isErrorEnabled()) {
            return false;
        }

        return true;
    }

    /**
     * 隐藏软键盘
     */
    private void hideInput() {
        CommonUtils.hideSoftKeyboard(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideInput();
        return super.onTouchEvent(event);
    }
}
