package com.wly.beansprout.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wly.beansprout.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit 实例化（OkHttp 配置）
 */
@Singleton
class RetrofitClient @Inject constructor() {

    // 从 BuildConfig 中读取当前构建类型对应的服务器地址
    private val BASE_URL = BuildConfig.BASE_URL

    private val gson: Gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setLenient()
            .serializeNulls()
            .create()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}