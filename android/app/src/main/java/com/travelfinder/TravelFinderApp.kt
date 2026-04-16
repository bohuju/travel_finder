package com.travelfinder

import android.app.Application
import com.baidu.location.LocationClient
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口类
 * 使用 Hilt 进行依赖注入
 */
@HiltAndroidApp
class TravelFinderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        LocationClient.setAgreePrivacy(true)
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.setCoordType(CoordType.BD09LL)
        SDKInitializer.initialize(this)
    }
}
