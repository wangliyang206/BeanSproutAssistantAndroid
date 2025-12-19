package com.wly.beansprout.ui

import android.content.Context
import android.content.Intent

/**
 * 通用类
 * Created by wly on 2022/9/26.
 */


/**
 * 跳转Activity
 */
fun onJumpActivity(context: Context, clazz: Class<*>) {
    // 跳转Activity
    context.startActivity(Intent(context, clazz))
}