package com.wly.beansprout.global;

import android.content.Context;
import android.text.TextUtils;

import com.wly.beansprout.BuildConfig;
import com.wly.beansprout.bean.LoginResponse;
import com.wly.beansprout.utils.AppPreferencesHelper;


/**
 * @ Title: AccountManager
 * @ Package com.zqw.mobile.recycling.api
 * @ Description: 用户信息存取类：从SharedPreferences中读取用户登录信息
 * @ author: wly
 * @ date: 2017/03/13 14:24
 */
public final class AccountManager {

    /*----------------------------------------------业务常量-------------------------------------------------*/
    /**
     * 头像
     */
    private final String PHOTOURL = "photoUrl";

    /**
     * 账号
     */
    private final String ACCOUNT = "Account";

    /**
     * 密码
     */
    private final String PASSWORD = "Password";

    /**
     * Token
     */
    private final String TOKEN = "Token";

    /**
     * 用户id
     */
    private final String USERID = "Userid";

    /**
     * 用户名称(昵称)
     */
    private final String USER_NAME = "UserName";

    /**
     * 电话
     */
    private final String RECYCLE_PHONE = "recyclePhone";

    /**
     * 自动回复内容
     */
    private final String AUTO_REPLY_SCRIPT = "autoReplyScript";

    /*----------------------------------------------操作对象-------------------------------------------------*/

    private AppPreferencesHelper spHelper;

    public AccountManager(Context context) {
        this.spHelper = new AppPreferencesHelper(context.getApplicationContext(), BuildConfig.SHARED_NAME_INVEST, 1);

    }

    /**
     * 保存登录信息(登录成功后调用此方法)
     *
     * @param account       账号
     * @param password      密码
     * @param loginResponse 用户信息
     */
    public void saveAccountInfo(String account, String password, LoginResponse loginResponse) {
        spHelper.put(ACCOUNT, account);
        spHelper.put(PASSWORD, password);
        updateAccountInfo(loginResponse);
    }

    /**
     * 更新登录信息(登录成功后调用此方法)
     *
     * @param loginResponse 用户信息
     */
    public void updateAccountInfo(LoginResponse loginResponse) {
        spHelper.put(TOKEN, loginResponse.getToken());
        spHelper.put(USERID, loginResponse.getUserId());
        spHelper.put(USER_NAME, loginResponse.getUserName());
//        spHelper.put(PHOTOURL, loginResponse.getAccountImage());
        spHelper.put(RECYCLE_PHONE, loginResponse.getUserPhone());
    }

    /**
     * 清除账号信息(手动点击退出登录后调用此方法)
     */
    public void clearAccountInfo() {
        spHelper.put(ACCOUNT, "");
        spHelper.put(PASSWORD, "");
        spHelper.put(TOKEN, "");
        spHelper.put(USER_NAME, "");
        spHelper.put(USERID, "");
        spHelper.put(PHOTOURL, "");
        spHelper.put(RECYCLE_PHONE, "");
    }

    /**
     * 设置Token
     *
     * @param token token
     */
    public void setToken(String token) {
        spHelper.put(TOKEN, token);
    }

    /**
     * 获取用户名称(昵称)
     *
     * @return 如果为空则返回账号
     */
    public String getUserName() {
        String username = spHelper.getPref(USER_NAME, "");
        if (TextUtils.isEmpty(username)) {
            username = spHelper.getPref(ACCOUNT, "");
        }
        return username;
    }

    /**
     * 获取账号
     *
     * @return 回调
     */
    public String getAccount() {
        return spHelper.getPref(ACCOUNT, "");
    }

    /**
     * 获取电话
     */
    public String getCurrPhone() {
        return spHelper.getPref(RECYCLE_PHONE, "");
    }

    /**
     * 获取密码
     *
     * @return 回调
     */
    public String getPassword() {
        return spHelper.getPref(PASSWORD, "");
    }

    /**
     * 获取Token
     *
     * @return 返回数据
     */
    public String getToken() {
        return spHelper.getPref(TOKEN, "");
    }

    /**
     * 获取头像URL
     *
     * @return 返回数据
     */
    public String getPhotoUrl() {
        return spHelper.getPref(PHOTOURL, "");
    }

    /**
     * 更新头像URL
     */
    public void setPhotoUrl(String url) {
        spHelper.put(PHOTOURL, url);
    }

    /**
     * 获取用户id
     *
     * @return 返回数据
     */
    public String getUserId() {
        return spHelper.getPref(USERID, "");
    }


    /**
     * 当前是否登录
     *
     * @return token存在则表示已登录(返回true)否则未登录(返回false)
     */
    public boolean isLogin() {
        String token = spHelper.getPref(TOKEN, "");
        return !TextUtils.isEmpty(token);
    }

    /**
     * 获取自动回复内容
     */
    public String getAutoReplyScript() {
        return spHelper.getPref(AUTO_REPLY_SCRIPT, "喜欢主播的点点关注、点点赞，感谢！;感谢大家的支持！;如果觉得今天的直播不错，就请给我点个赞吧！你们的支持是我最大的动力！;欢迎各位亲们来到直播间！");
    }

    public void setAutoReplyScript(String val) {
        spHelper.put(AUTO_REPLY_SCRIPT, val);
    }
}
