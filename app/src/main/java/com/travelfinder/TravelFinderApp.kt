package com.travelfinder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口类
 * 使用 Hilt 进行依赖注入
 */
@HiltAndroidApp
class TravelFinderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化操作可以在这里进行
    }
}
