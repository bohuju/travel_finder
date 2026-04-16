# Trip Domain Model

## 1. 目标

本文档定义 `Trip` 相关对象的第一版跨端共享语义。

当前文档基于现有 Backend 实现：

- [Trip.kt](/home/bohuju/self_project/travel_finder/backend/src/main/kotlin/com/travelfinder/trip/Trip.kt)

---

## 2. 对象拆分

当前行程域包含三个相关对象：

- `Trip`
  服务端内部持久化/业务对象，使用 `poiIds`
- `TripResponse`
  面向 API 输出的对象，展开为 `pois`
- `CreateTripRequest`
  面向 API 输入的创建请求对象

这种拆分是有意的：

- Backend 内部可以保留轻量引用关系
- API 输出优先服务客户端展示

---

## 3. Trip

### 字段定义

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | 否 | 行程唯一标识 |
| `name` | `String` | 否 | 行程名称 |
| `days` | `Int` | 否 | 行程天数 |
| `note` | `String` | 是 | 备注信息 |
| `poiIds` | `List<String>` | 否 | 关联的 POI id 列表 |

### 语义说明

- `Trip` 是 Backend 当前的基础业务对象
- `poiIds` 表示行程与 POI 的关联关系
- `poiIds` 的顺序当前默认保留用户输入顺序

### 约束

- `id` 不能为空
- `name` 不能为空
- `days` 必须大于 `0`
- `poiIds` 可以为空列表
- `note` 可以为空

### 是否直接进入 API

当前 `Trip` 本身不直接作为 API 响应输出，API 优先使用 `TripResponse`。

---

## 4. TripResponse

### 字段定义

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | 否 | 行程唯一标识 |
| `name` | `String` | 否 | 行程名称 |
| `days` | `Int` | 否 | 行程天数 |
| `note` | `String` | 是 | 备注信息 |
| `pois` | `List<Poi>` | 否 | 已展开的 POI 列表 |

### 语义说明

- `TripResponse` 是当前 Android 消费的行程输出模型
- 为减少 Android 首轮联调复杂度，当前返回完整 `pois`，而不是只返回 `poiIds`

### 约束

- `pois` 可以为空列表
- `pois` 内对象必须符合 [poi.md](/home/bohuju/self_project/travel_finder/docs/domain/poi.md) 定义

### 是否进入 API

当前直接进入 API：

- `GET /api/trips`
- `POST /api/trips` 成功响应

---

## 5. CreateTripRequest

### 字段定义

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `name` | `String` | 否 | 待创建行程名称 |
| `days` | `Int` | 否 | 行程天数 |
| `note` | `String` | 是 | 备注 |
| `poiIds` | `List<String>` | 否 | 要关联的 POI id 列表 |

### 校验规则

- `name` 不能为空或纯空白
- `days` 必须大于 `0`
- `poiIds` 中每个 id 必须存在

### 是否进入 API

当前直接进入 API：

- `POST /api/trips`

---

## 6. Android 使用约定

- Android 展示层应依赖 `TripResponse`，而不是假设服务端内部一定使用 `poiIds`
- Android 提交创建时应使用 `CreateTripRequest`
- Android 应允许 `note` 为空
- Android 应允许 `pois` 为空列表

---

## 7. 后续可扩展方向

后续可能扩展：

- `startDate`
- `endDate`
- `status`
- `ownerUserId`
- `sortedPois`
- `transportHints`

如果未来需要行程排序、拖拽编辑或协作共享，建议新增专门的行程项模型，而不是继续在 `Trip` 顶层平铺全部字段。
