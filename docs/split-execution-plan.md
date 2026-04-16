# 前后端双项目拆分执行方案

## 1. 文档目标

本文档用于指导 `Travel Finder` 从当前单仓双目录形态，演进为：

- `travel-finder-android`
- `travel-finder-backend`
- `travel-finder-contracts-docs`

三者协作的稳定结构。

本文档聚焦四件事：

- 两个项目的目录边界
- 共享文档仓的结构
- 拆分顺序
- 每一步的验收标准

---

## 2. 当前前提

当前仓库已经具备拆分前的基础条件：

- Android 工程已迁移到 `android/`
- Backend 工程已迁移到 `backend/`
- 最小 API 已存在
- 协作文档已具备雏形：
  - `docs/api/`
  - `docs/domain/`
  - `docs/deployment/`

当前仍未完成：

- Backend PostgreSQL 持久化
- Android 接真实后端 API
- 同步任务、鉴权、OCR 等复杂能力

因此，当前适合执行的是 **有节奏的拆分准备与过渡**，而不是立即粗暴拆仓。

---

## 3. 最终目标结构

推荐最终形态如下：

```text
travel-finder-android/
travel-finder-backend/
travel-finder-contracts-docs/
```

三个仓库各自承担不同职责：

- Android 仓只放客户端代码与客户端本地说明
- Backend 仓只放服务端代码与服务端本地说明
- Contracts Docs 仓只放前后端共享契约与联调文档

---

## 4. 两个项目的目录边界

### 4.1 `travel-finder-android`

Android 仓只保留客户端工程和客户端局部文档。

推荐结构：

```text
travel-finder-android/
├── app/
├── gradle/
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── local.properties           # 本地文件，不入库
├── README.md
├── docs/
│   ├── development/
│   ├── testing/
│   └── map-sdk/
└── scripts/
```

边界规则：

- 保留 Android 编译、运行、测试所需全部文件
- 允许保留仅服务 Android 实现的局部文档
- 不保留共享 API 契约和跨端领域模型主文档
- 不保留 Backend 部署文档

Android 仓中不应再出现：

- Backend 代码
- PostgreSQL 说明
- 服务端部署说明
- 跨端契约的主版本文档

### 4.2 `travel-finder-backend`

Backend 仓只保留服务端工程和服务端局部文档。

推荐结构：

```text
travel-finder-backend/
├── src/
│   ├── main/
│   └── test/
├── gradle/
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── build.gradle.kts
├── README.md
├── docs/
│   ├── development/
│   ├── operations/
│   └── db/
└── scripts/
```

边界规则：

- 保留 Ktor 服务端全部源码、测试、构建文件
- 保留仅服务端内部使用的开发与运维文档
- 不保留 Android 模块计划
- 不保留跨端契约主文档副本

Backend 仓中不应再出现：

- Android UI 代码
- 百度地图 SDK 文档
- 前端页面交互设计说明
- 共享契约的主维护版本

### 4.3 目录边界原则

判断一个文件该放在哪个仓，可以用下面规则：

- 只影响 Android 开发：放 Android 仓
- 只影响 Backend 开发与运维：放 Backend 仓
- 同时影响 Android 与 Backend 的协作：放共享文档仓

---

## 5. 共享文档仓结构

推荐将共享文档独立为：

- `travel-finder-contracts-docs`

推荐结构：

```text
travel-finder-contracts-docs/
├── README.md
├── api/
│   ├── README.md
│   ├── contract-template.md
│   ├── poi-trip-v1.md
│   └── changelog/
├── domain/
│   ├── README.md
│   ├── poi.md
│   ├── trip.md
│   ├── location.md
│   ├── error-response.md
│   └── sync-status.md
├── integration/
│   ├── login-flow.md
│   ├── poi-flow.md
│   ├── trip-flow.md
│   └── sync-flow.md
├── deployment/
│   ├── README.md
│   ├── environments.md
│   ├── backend-runtime.md
│   └── release-process.md
└── decisions/
    ├── adr-001-repo-split.md
    └── adr-002-api-versioning.md
```

### 5.1 各目录职责

- `api/`
  存放接口契约、示例请求响应、错误码、版本变更
- `domain/`
  存放跨端共享对象定义与字段语义
- `integration/`
  存放联调流程、时序、异常处理方式
- `deployment/`
  存放联调环境、测试环境、运行约定
- `decisions/`
  存放关键架构决策记录

### 5.2 共享文档仓规则

- 共享文档仓是前后端契约的唯一事实来源
- Android 与 Backend 仓只允许引用，不允许长期维护各自分叉版本
- 所有跨端变更必须先进入该仓

---

## 6. 拆分顺序

建议按照五个阶段执行。

### 阶段 1：文档冻结边界

目标：

- 先固定拆分后的仓边界与共享文档规则

动作：

- 新增本拆分执行方案
- 明确 Android / Backend / Contracts 三者职责
- 约定“共享文档为唯一事实来源”

验收标准：

- 能明确判断任一文件应该归属哪个仓
- API / Domain / Deployment 文档目录已稳定
- 团队对拆分目标无明显歧义

### 阶段 2：Backend 基础设施补齐

目标：

- 让 Backend 从演示骨架变成真实服务基础

动作：

- 接入 PostgreSQL
- 建立 schema 和迁移机制
- 让 `POI / Trip` 最小接口使用真实持久化数据

验收标准：

- Backend 可脱离内存仓储独立运行
- 服务重启后数据不丢失
- 最小 API 在真实数据源下可用

### 阶段 3：Android 接口切换

目标：

- 让 Android 开始真正依赖 Backend API

动作：

- 按共享 API 契约建立 DTO
- 接通 `POI / Trip` 最小接口
- 替换当前本地/临时数据路径

验收标准：

- Android 至少一个完整页面链路使用真实 Backend 数据
- Android 不再依赖临时数据才能演示最小流程
- 联调中出现的问题可以回到共享文档定位

### 阶段 4：共享文档仓独立化

目标：

- 把当前仓库里的共享文档独立成单独维护源

动作：

- 将 `docs/api/`
- `docs/domain/`
- `docs/deployment/`
- 后续新增的 `docs/integration/`
  迁移到 `travel-finder-contracts-docs`

验收标准：

- Android 与 Backend 都能只通过共享文档仓完成契约对齐
- 当前仓库中的共享文档不再作为主维护版本
- 契约变更流程已经切换到“先改文档仓”

### 阶段 5：代码仓正式拆分

目标：

- 将 Android 和 Backend 物理拆分为两个独立项目

动作：

- 迁出 `android/` 到 `travel-finder-android`
- 迁出 `backend/` 到 `travel-finder-backend`
- 各自补充本仓 `README`、本地开发说明和 CI

验收标准：

- Android 仓单独 clone 后可独立构建
- Backend 仓单独 clone 后可独立构建与运行
- 两边都只依赖共享文档仓同步契约

---

## 7. 每一步的详细验收标准

### 7.1 Android 仓验收标准

- 仓内不包含 Backend 代码
- `README` 能独立指导 Android 开发者启动项目
- 本地构建命令、测试命令、调试命令完整可用
- Android 局部文档不再承担跨端契约职责

### 7.2 Backend 仓验收标准

- 仓内不包含 Android 代码
- `README` 能独立指导 Backend 开发者启动服务
- 数据库、环境变量、运行方式在仓内自洽
- Backend 内部说明与共享契约不重复维护

### 7.3 共享文档仓验收标准

- `API / Domain / Integration / Deployment` 四类文档结构清晰
- 至少一组真实接口已有正式契约文档
- 至少一组核心对象已有正式领域文档
- 兼容性、错误码、联调流程可查可追溯

### 7.4 整体拆分完成标准

- 新成员只看三个仓的 `README` 即可理解协作方式
- Android 与 Backend 修改同一接口时，不需要依赖口头同步
- 契约变更可通过文档仓记录和审查
- 任一仓升级后，都能通过共享文档判断兼容影响

---

## 8. 风险与缓解

### 风险 1：过早拆分导致频繁来回改契约

缓解：

- 在 Backend 持久化和 Android 最小联调完成后再正式拆仓

### 风险 2：文档仓独立后没人维护

缓解：

- 规定跨端改动必须以文档 PR 为入口
- 约定前后端 owner 共同 review

### 风险 3：Android 和 Backend 各自复制一份契约

缓解：

- 明确共享文档仓是唯一事实来源
- 代码仓只保留引用链接，不保留主副本

### 风险 4：拆分时丢失历史上下文

缓解：

- 在拆分前将关键 ADR、API 版本记录、领域模型说明补齐

---

## 9. 当前建议结论

结合当前项目状态，推荐策略是：

1. 现在就按“三仓目标”来约束边界与文档流程
2. 先完成 Backend 持久化
3. 再完成 Android 最小接口切换
4. 再独立共享文档仓
5. 最后正式拆出两个代码仓

也就是说，当前已经到了“按双项目治理”的阶段，但正式物理拆分应放在联调闭环形成之后。
