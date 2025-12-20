package com.wly.beansprout.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey


/**
 * 登录信息的键（根据需要扩展）
 */
object LoginKeys {
    // 用户 Token
    val USER_TOKEN = stringPreferencesKey("user_token")
    // 用户 ID
    val USER_ID = stringPreferencesKey("user_id")
    // 用户名
    val USER_NAME = stringPreferencesKey("user_name")
    // 手机号
    val USER_PHONE = stringPreferencesKey("user_phone")
    // 是否记住密码
    val REMEMBER_PASSWORD = booleanPreferencesKey("remember_password")
    // 状态
    val STATUS = intPreferencesKey("status")
    // 剩余天数
    val DAYS_REMAINING = intPreferencesKey("days_remaining")
    // 是否登录
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
}