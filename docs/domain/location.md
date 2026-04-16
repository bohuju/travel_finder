# Location Domain Model

## 1. 目标

本文档定义 `Location` 的第一版跨端共享语义。

当前文档基于 Android 现有实现：

- [Location.kt](/home/bohuju/self_project/travel_finder/android/app/src/main/java/com/travelfinder/domain/model/Location.kt)
- [LocationFormatter.kt](/home/bohuju/self_project/travel_finder/android/app/src/main/java/com/travelfinder/util/LocationFormatter.kt)

---

## 2. 对象定位

`Location` 是地理位置值对象，用于统一表达：

- 当前定位
- POI 所在位置
- 行程中心点
- 内容源中解析出的候选位置

它本身不表示地图 SDK 对象，也不表示逆地理编码结果的完整结构。

---

## 3. 当前字段定义

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `latitude` | `Double` | 否 | 纬度 |
| `longitude` | `Double` | 否 | 经度 |
| `address` | `String` | 否 | 地址文本，允许为空字符串 |

当前 Android 还定义了：

- `Location.UNKNOWN = Location(0.0, 0.0, "")`

---

## 4. 字段语义

### `latitude` / `longitude`

- 用于表达地理坐标
- 当前 Android 认为 `(0.0, 0.0)` 表示未知位置
- 只有坐标合法且非 `(0.0, 0.0)` 时，才可认为是有效位置

### `address`

- 用于展示或辅助定位说明
- 可为空字符串
- 当前不是位置有效性的必要条件

---

## 5. 有效性规则

根据当前 Android 逻辑，一个有效 `Location` 应满足：

- `latitude` 在 `-90.0..90.0`
- `longitude` 在 `-180.0..180.0`
- 不能同时为 `0.0 / 0.0`

这意味着：

- `Location.UNKNOWN` 是一个合法对象，但不是有效业务位置

---

## 6. 是否进入 API

当前 Backend API 尚未直接输出 `Location` 对象，而是将位置字段平铺在 `Poi` 中：

- `latitude`
- `longitude`
- `address`

后续如果引入统一位置对象，可考虑在 API 中显式使用：

```json
{
  "location": {
    "latitude": 31.2304,
    "longitude": 121.4737,
    "address": "上海市黄浦区"
  }
}
```

---

## 7. Android 使用约定

- Android 可基于 `Location` 计算距离
- Android 可优先展示 `address`，无地址时退回经纬度文本
- Android 不应把 `(0.0, 0.0)` 当作真实位置使用

---

## 8. 后续可扩展方向

后续可扩展：

- `city`
- `district`
- `geoPrecision`
- `provider`
- `sourceType`

但当前建议继续保持 `Location` 作为轻量值对象，避免过早承载过多行政区与供应商信息。
