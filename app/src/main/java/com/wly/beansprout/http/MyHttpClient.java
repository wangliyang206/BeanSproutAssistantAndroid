package com.wly.beansprout.http;

import android.content.Context;

import com.wly.beansprout.api.AccountService;
import com.wly.beansprout.bean.AppUpdate;
import com.wly.beansprout.bean.LoginResponse;
import com.wly.beansprout.global.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.Interceptor;

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
        params.put("userPhone", username);
        params.put("password", password);

        return apiOperator.chain(params, request -> accountService.login(request));
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
//        interceptors.add(new HeaderInterceptor());
//        interceptors.add(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
//            @Override
//            public void log(String message) {
//                Log.d(TAG, message);
//            }
//        }).setLevel(HttpLoggingInterceptor.Level.BODY));
        return interceptors;
    }
}
