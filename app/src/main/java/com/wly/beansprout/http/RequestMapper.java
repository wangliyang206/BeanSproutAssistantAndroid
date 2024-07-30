package com.wly.beansprout.http;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.wly.beansprout.bean.ClientInfo;
import com.wly.beansprout.bean.GsonRequest;
import com.wly.beansprout.global.AccountManager;
import com.wly.beansprout.global.Constant;
import com.wly.beansprout.utils.CommonUtils;

/**
 * 包名： com.zqw.mobile.recycling.api
 * 对象名： RequestMapper
 * 描述：请求映射
 * 作者： wly
 * 邮箱：wangliyang206@163.com
 * 创建日期： 2017/3/24 14:36
 */

public final class RequestMapper implements IRequestMapper {

    private Context context;
    private AccountManager accountManager;

    public RequestMapper(Context context) {
        this.context = context;
        this.accountManager = new AccountManager(context);
    }

    @Override
    public <T> GsonRequest<T> transform(T t) {
        GsonRequest<T> request = new GsonRequest<>();
        String token = accountManager.getToken();
        request.setUserId(accountManager.getUserId());
        request.setData(t);
        request.setToken(token);
        request.setVersion(Constant.version);

        request.setClient(getPhoneInfo(context));
        return request;
    }

    /**
     * 获取设备信息
     *
     * @param context 句柄
     * @return 返回 设备信息
     */
    private static ClientInfo getPhoneInfo(Context context) {
        ClientInfo loginBeanIn = new ClientInfo();
        loginBeanIn.setCell(CommonUtils.getPhoneNumber(context));
        loginBeanIn.setDeviceid(CommonUtils.getIMEI(context));
        loginBeanIn.setSimid(CommonUtils.getSimSerialNumber(context));
        loginBeanIn.setOs("android");
        loginBeanIn.setOsver(android.os.Build.VERSION.SDK_INT + "");
        loginBeanIn.setPpiheight(String.valueOf(CommonUtils.getScreenHeidth(context)));
        loginBeanIn.setPpiwidth(String.valueOf(CommonUtils.getScreenWidth(context)));
        loginBeanIn.setOsver(android.os.Build.VERSION.SDK_INT + "");
        // loginBeanIn.setDeskey(StringUtil.getRand(8));

        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            loginBeanIn.setVercode(pi.versionCode + "");
            loginBeanIn.setVername(pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return loginBeanIn;
    }
}
