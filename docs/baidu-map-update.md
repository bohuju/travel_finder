# 百度地图改动说明

## 目标

将项目中原本的占位地图页面替换为百度地图 Android SDK，并统一为百度地图交互方式：

- 使用百度 `MapView` 渲染真实地图
- 使用百度定位 SDK 获取当前位置
- 在地图上渲染 POI marker
- 点击 marker 或列表项时弹出百度地图风格的 `InfoWindow`
- 从地图气泡中直接执行导航和加入行程

## 本次改动

### 1. 百度地图 SDK 接入

已在 [app/build.gradle.kts](/home/bohuju/self_project/travel_finder/app/build.gradle.kts) 新增依赖：

- `com.baidu.lbsyun:BaiduMapSDK_Map:7.6.7`
- `com.baidu.lbsyun:BaiduMapSDK_Location_All:9.6.7`

同时通过 `manifestPlaceholders` 注入百度地图 AK：

- `BAIDU_MAP_API_KEY`

### 2. Application 初始化

在 [TravelFinderApp.kt](/home/bohuju/self_project/travel_finder/app/src/main/java/com/travelfinder/TravelFinderApp.kt) 中完成了以下初始化：

- 同意定位 SDK 隐私协议
- 同意地图 SDK 隐私协议
- 设置坐标系为 `BD09LL`
- 调用 `SDKInitializer.initialize(this)`

### 3. Manifest 配置

在 [AndroidManifest.xml](/home/bohuju/self_project/travel_finder/app/src/main/AndroidManifest.xml) 中新增：

- 百度地图 AK `meta-data`
- 百度地图/定位依赖的 Wi-Fi 权限

### 4. 地图页面重构

在 [MapFragment.kt](/home/bohuju/self_project/travel_finder/app/src/main/java/com/travelfinder/presentation/ui/map/MapFragment.kt) 中完成：

- 接入百度 `MapView`
- 管理 `MapView` 生命周期
- 接入 `LocationClient` 获取当前位置
- 在地图上渲染 POI marker
- marker 点击与列表点击统一联动
- 地图空白处点击关闭当前选中态
- 使用 `InfoWindow` 替换底部详情卡

### 5. 原生地图气泡交互

新增文件：

- [view_map_info_window.xml](/home/bohuju/self_project/travel_finder/app/src/main/res/layout/view_map_info_window.xml)
- [bg_map_info_pointer.xml](/home/bohuju/self_project/travel_finder/app/src/main/res/drawable/bg_map_info_pointer.xml)

现在 POI 详情通过百度地图上的自定义 `InfoWindow` 展示，包含：

- 名称
- 评分
- 地址
- 距离
- 标签与来源
- 导航按钮
- 加入行程按钮

### 6. 导航统一为百度地图

在 [RouteNavigator.kt](/home/bohuju/self_project/travel_finder/app/src/main/java/com/travelfinder/presentation/navigation/RouteNavigator.kt) 中：

- 优先唤起百度地图客户端
- 客户端不可用时退回百度网页导航

### 7. 布局调整

在 [fragment_map.xml](/home/bohuju/self_project/travel_finder/app/src/main/res/layout/fragment_map.xml) 中：

- 删除底部 POI 详情卡
- 保留搜索区、状态卡、列表和定位按钮
- 将地图气泡作为唯一详情入口

## 配置说明

需要在 [gradle.properties](/home/bohuju/self_project/travel_finder/gradle.properties) 中配置百度地图 Android AK：

```properties
BAIDU_MAP_API_KEY=你的百度地图AK
```

也可以在构建时传入：

```bash
./gradlew assembleDebug -PBAIDU_MAP_API_KEY=你的百度地图AK
```

## 验证情况

已完成验证：

- `./gradlew compileDebugKotlin`

当前结果：

- 编译通过

已知说明：

- 百度定位 SDK 中 `setOpenGps(true)` 有弃用警告，但当前不影响功能
- 若未配置 `BAIDU_MAP_API_KEY`，地图 SDK 无法正常出图

## 相关文件

- [app/build.gradle.kts](/home/bohuju/self_project/travel_finder/app/build.gradle.kts)
- [app/src/main/AndroidManifest.xml](/home/bohuju/self_project/travel_finder/app/src/main/AndroidManifest.xml)
- [app/src/main/java/com/travelfinder/TravelFinderApp.kt](/home/bohuju/self_project/travel_finder/app/src/main/java/com/travelfinder/TravelFinderApp.kt)
- [app/src/main/java/com/travelfinder/presentation/ui/map/MapFragment.kt](/home/bohuju/self_project/travel_finder/app/src/main/java/com/travelfinder/presentation/ui/map/MapFragment.kt)
- [app/src/main/java/com/travelfinder/presentation/navigation/RouteNavigator.kt](/home/bohuju/self_project/travel_finder/app/src/main/java/com/travelfinder/presentation/navigation/RouteNavigator.kt)
- [app/src/main/res/layout/fragment_map.xml](/home/bohuju/self_project/travel_finder/app/src/main/res/layout/fragment_map.xml)
- [app/src/main/res/layout/view_map_info_window.xml](/home/bohuju/self_project/travel_finder/app/src/main/res/layout/view_map_info_window.xml)
- [app/src/main/res/drawable/bg_map_info_pointer.xml](/home/bohuju/self_project/travel_finder/app/src/main/res/drawable/bg_map_info_pointer.xml)
- [app/src/main/res/values/strings.xml](/home/bohuju/self_project/travel_finder/app/src/main/res/values/strings.xml)
- [gradle.properties](/home/bohuju/self_project/travel_finder/gradle.properties)
