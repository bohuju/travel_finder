# SyncStatus Domain Model

## 1. 目标

本文档定义 `SyncStatus` 的第一版跨端协作语义。

当前仓库中尚未落地正式代码实体，但该模型已经需要被前后端提前对齐，因为未来会同时影响：

- Backend 同步任务状态表达
- Android 首页、设置页或同步页的状态展示
- 运维与联调时的状态判断

---

## 2. 对象定位

`SyncStatus` 用于表达“当前同步任务或同步系统处于什么状态”，而不是表达具体的抓取结果详情。

它主要承担：

- 状态展示
- 重试与失败提示
- 联调与排障时的系统观察

---

## 3. 建议字段定义

第一版建议按下面的结构统一：

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `status` | `String` | 否 | 当前状态枚举 |
| `lastSuccessAt` | `String` | 是 | 最近成功同步时间，ISO-8601 |
| `lastFailureAt` | `String` | 是 | 最近失败时间，ISO-8601 |
| `message` | `String` | 是 | 当前状态描述 |
| `runningTaskId` | `String` | 是 | 正在运行的任务 id |

---

## 4. 状态枚举建议

第一版建议仅保留少量稳定状态：

| 状态 | 说明 |
| --- | --- |
| `idle` | 当前无同步任务运行 |
| `running` | 正在执行同步 |
| `success` | 最近一次同步成功 |
| `failed` | 最近一次同步失败 |
| `partial_success` | 部分成功、部分失败 |

---

## 5. 字段语义

### `status`

- 必须取自约定枚举
- Android 可基于它决定 UI 状态

### `lastSuccessAt`

- 表示最近一次完整成功完成的时间
- 不等于任务启动时间

### `lastFailureAt`

- 表示最近一次失败完成的时间

### `message`

- 面向展示与联调
- 适合提示“同步完成”“部分内容失败”“需要重试”等信息

### `runningTaskId`

- 仅在 `running` 状态下通常有值
- 用于日志或后台管理排查

---

## 6. 是否进入 API

当前尚未进入已实现 API，但后续预计会进入：

- `GET /api/sync/status`
- `POST /api/sync/run` 的响应体

因此从现在开始就应作为跨端共享对象设计。

---

## 7. Android 使用约定

- Android 应优先使用 `status` 决定展示态
- `message` 可作为辅助提示文案来源，但不应用作业务分支主依据
- 当 `status = running` 时，可展示进行中状态并适当轮询
- 当 `status = failed` 时，应允许提示重试

---

## 8. 后续可扩展方向

后续可扩展：

- `progressPercent`
- `totalCount`
- `successCount`
- `failureCount`
- `warningCount`
- `startedAt`
- `finishedAt`

如果后续同步模块复杂度变高，建议拆分：

- `SyncStatus`
- `SyncTask`
- `SyncLog`

分别承担状态展示、任务标识、日志细节。
