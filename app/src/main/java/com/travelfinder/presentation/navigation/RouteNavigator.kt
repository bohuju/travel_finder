package com.travelfinder.presentation.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.travelfinder.domain.model.POI

interface RouteNavigator {
    fun navigateTo(poi: POI): NavigationResult
}

sealed interface NavigationResult {
    data object Launched : NavigationResult
    data class Unavailable(val reason: String) : NavigationResult
}

class ExternalMapRouteNavigator(
    private val context: Context
) : RouteNavigator {

    override fun navigateTo(poi: POI): NavigationResult {
        val latitude = poi.location.latitude
        val longitude = poi.location.longitude

        if (latitude == 0.0 && longitude == 0.0) {
            return NavigationResult.Unavailable("该地点缺少坐标信息")
        }

        val intents = listOf(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "androidamap://route?sourceApplication=TravelFinder" +
                        "&dlat=$latitude&dlon=$longitude&dname=${Uri.encode(poi.name)}&dev=0&t=0"
                )
            ).setPackage("com.autonavi.minimap"),
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "baidumap://map/direction?destination=name:${Uri.encode(poi.name)}|latlng:$latitude,$longitude" +
                        "&coord_type=gcj02&mode=driving"
                )
            ).setPackage("com.baidu.BaiduMap"),
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(poi.name)})")
            )
        )

        intents.forEach { intent ->
            if (intent.resolveActivity(context.packageManager) != null) {
                try {
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    return NavigationResult.Launched
                } catch (_: ActivityNotFoundException) {
                    // Continue trying the next provider.
                }
            }
        }

        return NavigationResult.Unavailable("未找到可用的地图应用")
    }
}
