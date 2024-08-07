package com.wly.beansprout.http;

import android.content.Context;
import android.util.Log;

import com.wly.beansprout.api.AccountService;
import com.wly.beansprout.bean.AppUpdate;
import com.wly.beansprout.bean.CommonResponse;
import com.wly.beansprout.bean.LoginResponse;
import com.wly.beansprout.global.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Http客户端
 */
public class MyHttpClient extends AbstractHttpClient {
    public static final String TAG = "MyHttpClient";

    private ApiOperator apiOperator;
    private AccountService accountService;

    public MyHttpClient(Context context) {
        apiOperator = new ApiOperator(new RequestMapper(context));
        accountService = retrofit.create(AccountService.class);
    }

    /**
     * 登录
     */
    public Observable<LoginResponse> login(String username, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("mobile", username);
        params.put("password", password);

        return apiOperator.chain(params, request -> accountService.login(request));
    }

    /**
     * 注册
     *
     * @param mobile   账号
     * @param password 密码
     */
    public Observable<CommonResponse> register(String mobile, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("password", password);

        return apiOperator.chain(params, request -> accountService.register(request));
    }

    /**
     * 验证Token有效性
     */
    public Observable<LoginResponse> validToken() {
        Map<String, String> params = new HashMap<>();

        return apiOperator.chain(params, request -> accountService.validToken(request));
    }

    /**
     * 获取APP版本信息
     */
    public Observable<AppUpdate> getVersion() {
        Map<String, Object> params = new HashMap<>();

        return apiOperator.chain(params, request -> accountService.getVersion(request));
    }

    @Override
    public String getBaseUrl() {
        return Constant.SERVER_URL_VALUE;
    }

    @Override
    public List<Interceptor> getApplicationInterceptors() {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(new HeaderInterceptor());
        interceptors.add(new HttpLoggingInterceptor(message -> Log.d(TAG, message)).setLevel(HttpLoggingInterceptor.Level.BODY));
        return interceptors;
    }
}
