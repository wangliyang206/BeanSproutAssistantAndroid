package com.wly.beansprout.api;

import com.wly.beansprout.bean.AppUpdate;
import com.wly.beansprout.bean.CommonResponse;
import com.wly.beansprout.bean.GsonRequest;
import com.wly.beansprout.bean.GsonResponse;
import com.wly.beansprout.bean.LoginResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 包名： PACKAGE_NAME
 * 对象名： AccountApi
 * 描述：账户相关接口
 * 作者： wly
 * 邮箱：wangliyang206@163.com
 * 创建日期： 2017/3/24 10:03
 */

public interface AccountService {
    //登录
    @POST("member/login")
    Observable<GsonResponse<LoginResponse>> login(@Body GsonRequest<Map<String, String>> request);

    //验证Token有效性
    @POST("member/validToken")
    Observable<GsonResponse<LoginResponse>> validToken(@Body GsonRequest<Map<String, String>> request);

    //快捷登录
    @POST("member/quickLogin")
    Observable<GsonResponse<LoginResponse>> quickLogin(@Body GsonRequest<Map<String, String>> request);

    //注册
    @POST("member/register")
    Observable<GsonResponse<CommonResponse>> register(@Body GsonRequest<Map<String, String>> request);

    //获取APP版本信息
    @POST("system/getVersion")
    Observable<GsonResponse<AppUpdate>> getVersion(@Body GsonRequest<Map<String, Object>> request);
}
