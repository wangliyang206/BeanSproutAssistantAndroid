package com.wly.beansprout.http;

import com.wly.beansprout.bean.GsonRequest;

import io.reactivex.Observable;

/**
 * @Title: ApiAction
 * @Package com.cj.mobile.common.api
 * @Description: Api请求接口
 * @author: 王力杨
 * @date: 16/5/24 上午10:26
 */
public interface ApiOtherAction<T> {
    public Observable chain(GsonRequest<T> request);
}
