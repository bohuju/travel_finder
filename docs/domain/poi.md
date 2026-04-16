# Poi Domain Model

## 1. 目标

本文档定义 `Poi` 的第一版跨端共享语义，用于统一 Android 与 Backend 对 POI 的理解。

当前文档基于现有 Backend 实现：

- [Poi.kt](/home/bohuju/self_project/travel_finder/backend/src/main/kotlin/com/travelfinder/poi/Poi.kt)

---

## 2. 对象定位

`Poi` 表示一个“可被地图展示、列表浏览、行程引用”的结构化兴趣点。

它是前后端共享的核心对象之一，承担以下职责：

- 地图 marker 展示
- 列表卡片展示
- POI 详情展示
- 行程中的点位引用

`Poi` 不是原始抓取内容，不直接表示帖子、图片或 OCR 结果。

---

## 3. 当前字段定义

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | 否 | POI 唯一标识 |
| `name` | `String` | 否 | POI 展示名称 |
| `city` | `String` | 否 | 城市名称，面向展示和筛选 |
| `address` | `String` | 否 | 标准化后的地址文本 |
| `latitude` | `Double` | 否 | 纬度 |
| `longitude` | `Double` | 否 | 经度 |
| `tags` | `List<String>` | 否 | 标签集合，允许为空列表 |
| `source` | `String` | 否 | 数据来源标识，例如 `seed` |

---

## 4. 字段语义

### `id`

- 必须全局唯一
- 当前形态为字符串主键
- Android 不应自行拼装或推断 `id`

### `name`

- 用于地图气泡、列表标题、详情页标题
- 必须是适合用户直接阅读的展示名

### `city`

- 当前作为简单文本城市字段
- 主要用于展示和后续筛选
- 不等价于行政区划完整模型

### `address`

- 应表示已标准化、可展示的地址文本
- 后续允许来源于正文解析、OCR、人工修正或地理服务纠偏

### `latitude` / `longitude`

- 表示地图定位坐标
- 必须成对出现
- Android 端默认认为该值可直接用于地图展示

### `tags`

- 表示业务标签，如 `citywalk`、`night`、`park`
- 当前不区分系统标签与用户标签
- 顺序当前无业务语义

### `source`

- 表示 POI 的来源类型或来源通道
- 当前仅作为简单追踪字段
- 后续可能扩展为更细的来源对象，但当前 API 先保留字符串

---

## 5. 约束与不变量

- `id` 不能为空
- `name` 不能为空
- `city` 不能为空
- `address` 不能为空
- `latitude` / `longitude` 必须有效且可用于地图展示
- `tags` 可以为空列表，但字段本身不应缺失
- `source` 不能为空

---

## 6. 是否进入 API

`Poi` 当前直接进入 API：

- `GET /api/pois`
- `GET /api/pois/{id}`
- `GET /api/trips` 中的 `TripResponse.pois`
- `POST /api/trips` 成功响应中的 `TripResponse.pois`

这意味着：

- `Poi` 字段变化属于跨端契约变化
- 任何删除、重命名、语义变化都必须先更新 API 与 Domain 文档

---

## 7. Android 使用约定

- Android 可直接把 `name`、`city`、`address` 用于展示
- Android 可直接把 `latitude`、`longitude` 用于地图 marker
- Android 不应根据 `source` 做强业务判断，除非文档明确约定
- Android 应容忍 `tags` 为空列表

---

## 8. 后续可扩展方向

后续可以在不破坏当前模型主干的前提下扩展：

- `coverImageUrl`
- `description`
- `rating`
- `openingHours`
- `sourcePostIds`
- `geoPrecision`

新增字段应优先保持向后兼容。
