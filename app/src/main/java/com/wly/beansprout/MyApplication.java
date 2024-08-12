package com.wly.beansprout;

import android.app.Application;

import androidx.annotation.NonNull;

import com.umeng.commonsdk.UMConfigure;
import com.wly.beansprout.global.Constant;

/**
 * @ProjectName: BeanSproutAssistantAndroid
 * @Package: com.wly.beansprout
 * @ClassName: MyApplication
 * @Description:
 * @Author: WLY
 * @CreateDate: 2024/8/12 15:01
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initUM(this);
    }

    /**
     * 初始化友盟统计
     */
    private void initUM(@NonNull Application application) {
        //设置LOG开关，默认为false
        UMConfigure.setLogEnabled(true);
        // 友盟统计的隐私政策，未通过不可以初始化友盟统计。
        UMConfigure.preInit(application.getApplicationContext(),
                BuildConfig.DEBUG ? application.getString(R.string.um_app_key_debug) : application.getString(R.string.um_app_key),
                Constant.UM_CHANNEL);
    }
}
