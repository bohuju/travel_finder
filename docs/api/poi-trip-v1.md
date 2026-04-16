# POI / Trip API Contract v1

## 1. 基本信息

- 资源名称：POI / Trip
- 接口分组：Core Browse & Planning
- 当前版本：`v1`
- 状态：`implemented`
- Backend owner：待定
- Android owner：待定

---

## 2. 目标

这一组接口用于支撑当前最小联调闭环：

- 浏览 POI 列表
- 查看单个 POI
- 浏览行程列表
- 创建新行程

当前这组接口优先服务 Android 第一轮接口切换，而不是一次性覆盖完整业务。

---

## 3. 路由列表

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| `GET` | `/health` | 服务存活检查 | `implemented` |
| `GET` | `/api/pois` | 获取 POI 列表 | `implemented` |
| `GET` | `/api/pois/{id}` | 获取 POI 详情 | `implemented` |
| `GET` | `/api/trips` | 获取行程列表 | `implemented` |
| `POST` | `/api/trips` | 创建行程 | `implemented` |

---

## 4. 数据模型

### `Poi`

引用：

- [Poi Domain Model](/home/bohuju/self_project/travel_finder/docs/domain/poi.md)

字段表：

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | 否 | POI 唯一标识 |
| `name` | `String` | 否 | 展示名称 |
| `city` | `String` | 否 | 城市名称 |
| `address` | `String` | 否 | 标准化地址 |
| `latitude` | `Double` | 否 | 纬度 |
| `longitude` | `Double` | 否 | 经度 |
| `tags` | `List<String>` | 否 | 标签列表，可为空列表 |
| `source` | `String` | 否 | 来源标识 |

### `TripResponse`

引用：

- [Trip Domain Model](/home/bohuju/self_project/travel_finder/docs/domain/trip.md)

字段表：

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | 否 | 行程唯一标识 |
| `name` | `String` | 否 | 行程名称 |
| `days` | `Int` | 否 | 行程天数 |
| `note` | `String` | 是 | 行程备注 |
| `pois` | `List<Poi>` | 否 | 已展开的 POI 列表 |

### `CreateTripRequest`

字段表：

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `name` | `String` | 否 | 待创建行程名称 |
| `days` | `Int` | 否 | 行程天数 |
| `note` | `String` | 是 | 行程备注 |
| `poiIds` | `List<String>` | 否 | 关联的 POI id 列表 |

### `ErrorResponse`

字段表：

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `message` | `String` | 否 | 错误描述 |

引用：

- [ErrorResponse Domain Model](/home/bohuju/self_project/travel_finder/docs/domain/error-response.md)

### 业务错误码表

当前代码中尚未返回 `code` 字段，但从协作契约层面先定义如下错误码，供后续实现对齐：

| 错误码 | HTTP 状态 | 说明 |
| --- | --- | --- |
| `POI_NOT_FOUND` | `404` | 指定的 POI 不存在 |
| `TRIP_INVALID_NAME` | `400` | 行程名称为空或纯空白 |
| `TRIP_INVALID_DAYS` | `400` | 行程天数不合法 |
| `TRIP_UNKNOWN_POI` | `400` | 请求中的某个或多个 `poiIds` 不存在 |
| `BAD_REQUEST` | `400` | 通用参数错误 |

---

## 5. 接口详情

### `GET /health`

用途：

- 本地联调、部署探活、反向代理健康检查

成功响应：

- `200 OK`

```json
{
  "status": "ok"
}
```

字段说明：

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `status` | `String` | 否 | 当前固定返回 `ok` |

### `GET /api/pois`

用途：

- 获取当前可浏览的 POI 列表

请求参数：

- 当前无 query 参数

成功响应：

- `200 OK`

```json
[
  {
    "id": "poi-west-lake",
    "name": "西湖断桥",
    "city": "杭州",
    "address": "浙江省杭州市西湖区北山街",
    "latitude": 30.259,
    "longitude": 120.148,
    "tags": ["view", "lake", "classic"],
    "source": "seed"
  }
]
```

错误响应：

- 当前无专门业务错误体

### `GET /api/pois/{id}`

用途：

- 获取单个 POI 详情

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | 是 | POI id |

成功响应：

- `200 OK`
- body: `Poi`

错误响应：

| HTTP 状态 | 错误体 | 说明 |
| --- | --- | --- |
| `404` | `ErrorResponse` | POI 不存在，建议错误码 `POI_NOT_FOUND` |

错误示例：

```json
{
  "message": "POI not found: missing-id"
}
```

### `GET /api/trips`

用途：

- 获取行程列表

请求参数：

- 当前无 query 参数

成功响应：

- `200 OK`
- body: `List<TripResponse>`

```json
[
  {
    "id": "trip-1",
    "name": "杭州周末慢游",
    "days": 2,
    "note": "以西湖周边 citywalk 为主",
    "pois": [
      {
        "id": "poi-west-lake",
        "name": "西湖断桥",
        "city": "杭州",
        "address": "浙江省杭州市西湖区北山街",
        "latitude": 30.259,
        "longitude": 120.148,
        "tags": ["view", "lake", "classic"],
        "source": "seed"
      }
    ]
  }
]
```

### `POST /api/trips`

用途：

- 创建新行程

请求体：

- `CreateTripRequest`

请求示例：

```json
{
  "name": "上海夜景散步",
  "days": 1,
  "note": "先压一版最小 API",
  "poiIds": ["poi-the-bund"]
}
```

成功响应：

- `201 Created`
- body: `TripResponse`

错误响应：

| HTTP 状态 | 错误体 | 说明 |
| --- | --- | --- |
| `400` | `ErrorResponse` | 名称为空、天数非法或 POI 不存在 |

错误示例：

```json
{
  "message": "Unknown poi ids: poi-missing"
}
```

推荐错误码映射：

| 场景 | HTTP 状态 | 推荐错误码 |
| --- | --- | --- |
| `name` 为空或纯空白 | `400` | `TRIP_INVALID_NAME` |
| `days <= 0` | `400` | `TRIP_INVALID_DAYS` |
| 某个 `poiId` 不存在 | `400` | `TRIP_UNKNOWN_POI` |

---

## 6. 校验规则

- `CreateTripRequest.name` 不能为空或纯空白
- `CreateTripRequest.days` 必须大于 `0`
- `CreateTripRequest.poiIds` 中的每个 id 必须存在
- `TripResponse.pois` 可以为空列表
- `Poi.tags` 可以为空列表

---

## 7. Android 消费约定

- 地图页和列表页可直接消费 `Poi`
- 行程页应直接消费 `TripResponse`
- Android 当前不应依赖 `source` 的具体枚举语义
- Android 应接受 `note` 缺失或为空
- Android 不应假设未来 `GET /api/pois` 永远无筛选参数

---

## 8. 兼容性约定

### 8.1 总体原则

- 当前版本为最小联调版本
- 后续新增字段优先保持向后兼容
- 删除字段前必须先在文档中标记弃用
- 字段重命名视为破坏性变更

### 8.2 字段兼容规则

#### `Poi`

- 可以新增字段，但不得删除以下现有字段：
  - `id`
  - `name`
  - `city`
  - `address`
  - `latitude`
  - `longitude`
  - `tags`
  - `source`
- `tags` 必须保持为列表类型，不应改成单字符串
- `latitude` / `longitude` 的数值语义不能改成字符串

#### `TripResponse`

- 如未来引入 `poiIds`、`status`、`startDate` 等字段，不应破坏现有 `pois`
- `pois` 当前是 Android 首轮联调的关键字段，在完成替代方案前不得移除
- `note` 允许继续保持可空

#### `CreateTripRequest`

- `name`、`days`、`poiIds` 当前属于必填核心字段
- 后续新增字段必须默认可选，或提供明确默认行为

#### `ErrorResponse`

- 当前 `message` 为唯一已实现字段，在 `code` 尚未落地前不得移除
- 后续新增 `code`、`details` 等字段时，应保持对旧客户端兼容

### 8.3 客户端容忍策略

- Android 应忽略未识别的新字段
- Android 不应依赖 JSON 字段顺序
- Android 不应依赖 `message` 的自然语言内容做精确逻辑判断

### 8.4 破坏性变更判定

以下情况视为破坏性变更：

- 删除已发布字段
- 修改字段类型
- 修改必填/可空语义导致旧客户端解析失败
- 修改相同错误码的业务语义

发生破坏性变更时，必须：

1. 先更新契约文档
2. 标注影响范围
3. 给出迁移说明
4. 再推进实现

---

## 9. 变更记录

| 日期 | 版本 | 变更 | 说明 |
| --- | --- | --- | --- |
| `2026-04-17` | `v1` | 初版落地 | 对齐当前 Backend 已实现接口 |
