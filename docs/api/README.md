# API Docs

此目录用于存放 Android 客户端与 Backend 之间的 API 契约文档。

长期目标：

- 作为前后端拆分后的接口事实来源
- 记录请求、响应、错误、兼容性与版本变化

建议后续在本目录持续补充：

- 接口列表与资源分组
- DTO 字段说明
- 错误码与异常语义
- 分页、筛选、排序规则
- 兼容性与版本变更记录

当前已落地的最小后端接口如下：

## Health

### `GET /health`

用于本地联调和部署存活检查。

响应示例：

```json
{
  "status": "ok"
}
```

## POI

### `GET /api/pois`

返回当前可浏览的 POI 列表。

响应字段：

- `id`
- `name`
- `city`
- `address`
- `latitude`
- `longitude`
- `tags`
- `source`

### `GET /api/pois/{id}`

返回单个 POI 详情。

找不到时返回：

- `404 Not Found`
- body: `{ "message": "POI not found: {id}" }`

## Trip

### `GET /api/trips`

返回行程列表。当前响应会把 `poiIds` 展开成完整 `pois` 数组，便于 Android 先完成展示联调。

响应字段：

- `id`
- `name`
- `days`
- `note`
- `pois`

### `POST /api/trips`

创建新行程。

请求示例：

```json
{
  "name": "上海夜景散步",
  "days": 1,
  "note": "先压一版最小 API",
  "poiIds": ["poi-the-bund"]
}
```

校验规则：

- `name` 不能为空
- `days` 必须大于 `0`
- `poiIds` 中的每个 id 必须存在

成功时返回：

- `201 Created`
- body 为创建后的 `TripResponse`

## 文档模板与当前契约

- [contract-template.md](/home/bohuju/self_project/travel_finder/docs/api/contract-template.md)
  后续新增资源接口时复用的统一模板
- [poi-trip-v1.md](/home/bohuju/self_project/travel_finder/docs/api/poi-trip-v1.md)
  当前已实现的 `POI / Trip` 第一版正式契约

## 协作约定

- 任何跨端接口变更，先更新本文档
- 新增字段优先保持向后兼容
- 删除字段前先标记弃用
- 破坏性变更必须增加版本说明与迁移说明
