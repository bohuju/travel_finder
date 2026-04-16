# ErrorResponse Domain Model

## 1. 目标

本文档定义跨端共享错误响应对象 `ErrorResponse` 的第一版语义。

当前文档基于现有 Backend 实现：

- [ErrorResponse.kt](/home/bohuju/self_project/travel_finder/backend/src/main/kotlin/com/travelfinder/common/ErrorResponse.kt)

同时考虑未来前后端协作需要，先补充错误码约束，作为后续扩展方向。

---

## 2. 对象定位

`ErrorResponse` 用于表达“请求已到达服务端，但业务或参数校验失败，无法返回正常结果”的统一错误体。

它的职责是：

- 给 Android 提供稳定可解析的错误结构
- 让 Backend 在不同接口下保持一致错误语义
- 作为 API 契约中的统一错误响应模型

---

## 3. 当前字段定义

### 当前已实现字段

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `message` | `String` | 否 | 面向开发与联调的错误说明 |

### 建议保留的后续扩展字段

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `code` | `String` | 否 | 机器可判定的业务错误码 |
| `details` | `Map<String, String>` | 是 | 附加错误上下文 |
| `traceId` | `String` | 是 | 链路排查 id |

---

## 4. 字段语义

### `message`

- 当前是唯一已实现字段
- 应提供可读、明确、可用于联调排错的错误描述
- Android 不应依赖 `message` 做强逻辑分支

### `code`

- 当前尚未在代码中实现，但应作为后续标准化目标
- Android 应优先基于 `code` 判断错误类型，而不是解析自然语言 `message`

---

## 5. 约束与不变量

- 错误响应必须是结构化 JSON，而不是纯文本
- `message` 不能为空
- 相同业务错误应尽量映射到稳定错误码
- 同一错误码在不同接口下语义应保持一致

---

## 6. 是否进入 API

`ErrorResponse` 当前已进入 API：

- `GET /api/pois/{id}` 的 `404`
- `POST /api/trips` 的 `400`

后续所有业务错误响应都应优先复用该模型。

---

## 7. Android 使用约定

- Android 当前可展示 `message` 或记录日志
- Android 不应对 `message` 进行字符串匹配分支
- 当 `code` 落地后，Android 应切换为基于 `code` 做错误分类处理

---

## 8. 后续可扩展方向

建议下一版扩展为：

```json
{
  "code": "TRIP_UNKNOWN_POI",
  "message": "Unknown poi ids: poi-missing",
  "details": {
    "poiIds": "poi-missing"
  }
}
```

这样可以兼顾：

- 用户可读性
- Android 端可编程处理
- 服务端问题排查
