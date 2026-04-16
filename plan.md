# Travel Finder 演进与拆分规划

## 1. 规划目标

`Travel Finder` 的最终目标不是维持“一个仓库里包含全部内容”的长期形态，而是形成下面的稳定协作格局：

- `travel-finder-android`
  Android 客户端项目，负责地图展示、列表浏览、POI 详情、行程规划、导航唤起、本地缓存和 UI 状态。
- `travel-finder-backend`
  Backend 服务端项目，负责鉴权、POI 聚合、行程管理、内容抓取、OCR、地址解析、地理编码、同步任务与统一 API。
- 一套独立维护的协作文档
  负责 API 契约、领域模型、联调流程、版本策略、部署约定。

当前仓库是实现这个目标的过渡形态：先在单仓里把边界、接口和实现跑通，再拆分成两个独立代码项目。

---

## 2. 当前状态

当前已经完成：

- Android 工程迁移到 `android/`
- Backend 最小 Ktor 工程初始化
- 最小 API 落地：
  - `GET /health`
  - `GET /api/pois`
  - `GET /api/pois/{id}`
  - `GET /api/trips`
  - `POST /api/trips`
- `docs/` 目录已具备 API、领域、部署、进度、模块计划等文档入口

当前尚未完成：

- Backend PostgreSQL 持久化
- Android 接真实后端接口
- 鉴权、同步任务、OCR、地址解析、地理编码
- 双项目正式拆分

---

## 3. 当前仓库与最终形态

### 3.1 当前形态

当前仍采用单仓双目录：

```text
travel_finder/
├── android/
├── backend/
├── docs/
├── scripts/
├── DESIGN.md
├── plan.md
└── README.md
```

这种形态的意义是：

- 降低初期协作成本
- 便于快速统一接口与目录边界
- 让 Android 与 Backend 在真正拆分前完成第一轮联调

### 3.2 最终形态

最终建议演进为：

```text
travel-finder-android/
travel-finder-backend/
travel-finder-contracts-docs/   # 推荐独立维护，也可托管为文档站点
```

如果后续不希望维护第三个仓库，也可以将协作文档托管到独立站点或产品文档平台，但必须满足一个原则：

- API 契约和跨端领域定义不能只放在 Android 或 Backend 任意一侧

---

## 4. 职责边界

### 4.1 Android 负责

- 用户交互与页面导航
- 百度地图 SDK 展示
- POI 列表、地图、详情、行程页面
- 本地缓存与界面状态
- 定位、地图联动、导航唤起
- 调用 Backend API 并消费稳定 DTO

### 4.2 Android 不负责

- 内容抓取
- OCR
- 地址抽取与标准化
- 地理编码
- 数据持久化主存储
- 增量同步任务

### 4.3 Backend 负责

- 用户鉴权与会话
- POI 聚合与行程管理
- 内容源接入与抓取
- OCR、地址解析、地理编码
- 同步调度、失败重试、日志
- 向 Android 输出稳定 API

### 4.4 Backend 不负责

- 地图 SDK 渲染
- 客户端导航唤起
- 客户端 UI 状态
- 客户端交互逻辑

---

## 5. 协作文档设计

前后端拆分后，最容易失控的不是代码，而是契约漂移。因此文档要先于拆分设计好。

### 5.1 文档分类

推荐把协作文档固定为四类：

- `API Contract`
  接口路径、请求参数、响应 DTO、错误码、分页/筛选/排序规则、版本变更记录
- `Domain Model`
  `Poi`、`Trip`、`User`、`RawPost` 等共享对象的定义、字段语义、可空性、状态机
- `Integration Flow`
  登录流程、POI 查询流程、行程创建流程、同步状态展示流程、错误恢复流程
- `Deployment & Runtime`
  环境变量、部署方式、健康检查、日志、配置来源、联调环境说明

### 5.2 当前仓库中的落点

当前阶段先落在本仓库的 `docs/` 下：

- `docs/api/`
- `docs/domain/`
- `docs/deployment/`
- `docs/current-progress.md`
- `docs/module-plans/`

其中：

- `docs/api/` 面向前后端接口契约
- `docs/domain/` 面向共享模型定义
- `docs/deployment/` 面向服务端运行与联调环境
- `docs/current-progress.md` 面向阶段状态同步
- `docs/module-plans/` 主要服务于 Android 当前实现推进

### 5.3 拆分后的归属建议

拆分为两个代码项目后，协作文档建议独立维护，并采用以下归属规则：

- Backend owner 负责 API 契约初稿、错误码、部署文档
- Android owner 负责客户端消费约束、字段使用反馈、联调验收项
- Domain 文档由前后端共同维护，任何字段语义变更必须同步更新

### 5.4 变更流程

任何跨端变更都按下面顺序执行：

1. 先更新协作文档
2. 明确兼容性与影响范围
3. Backend 实现接口变更
4. Android 适配新契约
5. 完成联调并更新变更记录

### 5.5 版本策略

推荐从现在开始就采用下面的约束：

- 新增字段优先向后兼容
- 删除字段必须先标记弃用
- 破坏性变更必须更新 API 版本说明
- 所有示例请求响应都要放进 API 文档

---

## 6. 当前设计基线

### 6.1 Android

继续保持当前 Clean Architecture：

```text
android/app/src/main/java/com/travelfinder/
├── data/
├── domain/
├── presentation/
├── di/
└── util/
```

演进原则：

- `domain/` 保持业务抽象
- `data/` 逐步从本地/临时数据路径切到真实 API
- `presentation/` 保持地图、列表、行程页面职责清晰

### 6.2 Backend

当前已存在最小骨架，后续目标结构如下：

```text
backend/src/main/kotlin/com/travelfinder/
├── application/
├── auth/
├── user/
├── source/
├── crawler/
├── ocr/
├── parser/
├── geo/
├── poi/
├── trip/
├── sync/
└── common/
```

当前已落地模块：

- `application/`
- `poi/`
- `trip/`
- `common/`

下一步优先补：

- PostgreSQL 持久化
- 数据表与迁移机制
- 认证与用户模型

---

## 7. 关键协作对象

跨端至少要对齐以下核心对象：

- `Poi`
- `Trip`
- `User`
- `Location`
- `SyncStatus`
- `ErrorResponse`

Backend 内部还会继续扩展：

- `RawPost`
- `PostAsset`
- `ExtractedText`
- `AddressCandidate`
- `SyncTask`
- `SyncLog`

其中前六类对象应优先写入协作文档，因为它们最容易直接进入 API。

---

## 8. 阶段路线

### 阶段 1：单仓稳定化

- 完成 Android 与 Backend 的目录定型
- 固化最小 API
- 明确 API / Domain / Deployment 文档结构

### 阶段 2：Backend 基础设施化

- 接入 PostgreSQL
- 建立 schema 与迁移机制
- 让最小 API 使用真实持久化数据

### 阶段 3：Android 接口切换

- Android 从本地/临时数据切到 Backend API
- 完成 POI 列表、详情、行程的第一轮联调

### 阶段 4：协作文档稳定化

- 补齐错误码、兼容性规则、联调流程
- 给 API 与领域模型建立版本变更记录

### 阶段 5：双项目拆分

- 拆出 `travel-finder-android`
- 拆出 `travel-finder-backend`
- 迁移或独立托管协作文档

拆分执行细则见：

- [docs/split-execution-plan.md](/home/bohuju/self_project/travel_finder/docs/split-execution-plan.md)

---

## 9. 拆分验收标准

当项目准备从单仓过渡到双项目时，应满足：

- Android 与 Backend 都能独立构建和运行
- API 契约不再依赖口头同步
- 关键领域对象已有文档定义
- 联调流程可由文档执行
- 部署方式、环境变量、健康检查已有文档
- 至少完成一轮 Android <-> Backend 的真实接口联调

---

## 10. 风险与约束

- 如果先拆仓再补文档，前后端契约极易漂移
- 如果 Android 长期继续依赖本地临时数据，Backend 会难以形成真实边界
- 抓取、OCR、地理编码属于高不确定模块，不应阻塞最小联调闭环
- 数据库 schema 若晚于接口设计太久，会造成 DTO 与持久化模型反复回滚

---

## 11. 当前结论

当前最合理的推进方式不是马上拆成两个仓库，而是：

1. 先在当前仓库内稳定 Android / Backend / docs 三者边界
2. 再把 Backend 从内存实现推进到真实持久化
3. 再让 Android 完成真实接口切换
4. 最后在契约与文档稳定后拆成两个独立项目

也就是说，当前仓库是“拆分前的定型阶段”，而不是最终形态。
