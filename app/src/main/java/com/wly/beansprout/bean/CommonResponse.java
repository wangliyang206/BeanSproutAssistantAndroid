package com.wly.beansprout.bean;

/**
 * 包名： com.zqw.mobile.recycling.model
 * 对象名： CommonResponse
 * 描述：通用响应
 * 作者： wly
 * 邮箱：wangliyang206@163.com
 * 创建日期： 2017/3/28 17:52
 */

public class CommonResponse {
    public CommonResponse() {
    }

    // 1表示成功，0表示失败
    private int succ = 0;
    private int result = 0;

    public int getSucc() {
        return succ;
    }

    public void setSucc(int succ) {
        this.succ = succ;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
