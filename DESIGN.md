# Travel Finder 设计说明

## 1. 设计目标

本文件描述 `Travel Finder` 当前阶段的系统设计基线，以及从“单仓双目录”演进到“前后端两个独立项目”的目标路径。

当前设计目标有两个：

- 先跑通 Android 与 Backend 的最小闭环
- 先设计清楚协作文档，再进行代码仓库拆分

---

## 2. 当前系统形态

当前仓库采用：

- `android/`：Android 客户端
- `backend/`：Kotlin + Ktor 服务端
- `docs/`：共享文档

这不是最终形态，而是为了在拆分前降低沟通成本、快速固定接口与边界的过渡形态。

### 2.1 当前系统关系

```text
Android App
    |
    | HTTP API
    v
Backend (Ktor)
    |
    | 当前阶段：内存仓储
    | 目标阶段：PostgreSQL + 同步任务 + 外部能力
    v
Data / Integration Layer
```

### 2.2 当前已落地能力

Android：

- 地图展示
- POI 搜索与列表展示
- 行程基础逻辑
- 百度地图与定位接入
- 地图和列表联动

Backend：

- 最小 Ktor 工程
- `GET /health`
- `GET /api/pois`
- `GET /api/pois/{id}`
- `GET /api/trips`
- `POST /api/trips`

---

## 3. 设计原则

### 3.1 边界优先

当前阶段最重要的不是功能数量，而是边界清晰：

- Android 不处理抓取、OCR、地址解析、地理编码
- Backend 不处理 UI、地图渲染、客户端交互状态

### 3.2 契约优先

任何跨端交互都必须以文档化契约为准，而不是仅凭双方代码实现“碰巧对上”。

### 3.3 渐进拆分

先单仓稳定，再双仓拆分。拆分发生在契约稳定之后，而不是契约混乱时。

### 3.4 可替换实现

Backend 当前允许先使用内存仓储、种子数据和简化模型，但 API 形状、模块边界、文档结构要按照未来真实系统来设计。

---

## 4. Android 设计

Android 延续当前 Clean Architecture：

```text
android/app/src/main/java/com/travelfinder/
├── data/
├── domain/
├── presentation/
├── di/
└── util/
```

各层职责：

- `data/`
  负责远程接口、本地缓存、Repository 实现、DTO 转换
- `domain/`
  负责 UseCase、Repository 抽象、核心业务模型
- `presentation/`
  负责 Fragment、ViewModel、Adapter、UI State
- `di/`
  负责依赖注入
- `util/`
  负责工具类和通用逻辑

Android 的设计方向是：逐步把 `data/` 从本地/临时数据路径切换到 Backend API，而不改动 `domain/` 和 `presentation/` 的核心边界。

---

## 5. Backend 设计

Backend 采用 Kotlin + Ktor，并按模块职责拆分。

### 5.1 当前模块

当前已落地：

- `application/`
- `poi/`
- `trip/`
- `common/`

### 5.2 目标模块

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

模块职责：

- `application/`
  Ktor 启动、插件安装、路由装配
- `auth/`
  登录、令牌、权限
- `user/`
  用户信息与绑定关系
- `source/`
  外部内容来源
- `crawler/`
  内容抓取
- `ocr/`
  OCR 抽象与实现
- `parser/`
  地址提取与标准化
- `geo/`
  地理编码
- `poi/`
  POI 查询与聚合
- `trip/`
  行程管理
- `sync/`
  定时同步、日志、失败重试
- `common/`
  错误模型、配置、共享工具

### 5.3 数据策略

当前：

- 以内存仓储和种子数据为主

下一步：

- 切换到 PostgreSQL
- 引入 schema 和迁移机制
- 让 API 不再依赖进程内状态

---

## 6. 跨端协作设计

这是本项目设计里最重要的部分。

### 6.1 为什么要先设计协作文档

前后端真正拆成两个独立项目后，代码不再天然靠同一个仓库维持一致，最容易出现的问题包括：

- 字段名不一致
- 可空性理解不一致
- 错误码和异常处理不一致
- Android 使用了 Backend 未承诺的隐含行为
- Backend 修改接口但 Android 无法及时感知

所以，协作文档必须先成为事实来源。

### 6.2 协作文档的组成

建议长期固定为下面四类：

- API 契约文档
- 领域模型文档
- 联调流程文档
- 部署与运行文档

### 6.3 当前仓库中的承载位置

```text
docs/
├── api/
├── domain/
├── deployment/
├── module-plans/
└── current-progress.md
```

说明：

- `docs/api/`
  存放路径、请求、响应、错误、版本记录
- `docs/domain/`
  存放共享对象定义、字段语义、边界说明
- `docs/deployment/`
  存放环境变量、部署、运行和联调环境说明
- `docs/module-plans/`
  主要服务于当前 Android 端实施计划
- `docs/current-progress.md`
  记录阶段性状态，不作为长期契约事实来源

### 6.4 协作规则

跨端变更默认遵循：

1. 文档先变更
2. Backend 实现跟进
3. Android 适配跟进
4. 联调完成后记录结果

任何破坏性变更都必须在文档中显式说明影响范围。

---

## 7. 关键共享对象

当前最应该优先对齐的对象不是全部领域对象，而是会直接进入 API 的那些：

- `Poi`
- `Trip`
- `User`
- `Location`
- `ErrorResponse`
- `SyncStatus`

Backend 内部的处理对象可以后续再细化：

- `RawPost`
- `PostAsset`
- `ExtractedText`
- `AddressCandidate`
- `SyncTask`
- `SyncLog`

---

## 8. 目标演进

### 8.1 过渡阶段

当前单仓双目录负责解决：

- 目录边界
- 模块边界
- 最小 API
- 第一版协作文档

### 8.2 稳定阶段

当 Backend 持久化和 Android 接口切换完成后，系统应达到：

- 双端可独立运行
- API 文档可指导联调
- 关键领域模型已被文档化
- 部署方式有清晰说明

### 8.3 最终阶段

最终目标：

- `travel-finder-android`
- `travel-finder-backend`
- 独立维护的协作文档

到这一步时，协作文档的地位相当于“前后端共同遵守的协议层”。

具体执行方案见：

- [docs/split-execution-plan.md](/home/bohuju/self_project/travel_finder/docs/split-execution-plan.md)

---

## 9. 当前设计结论

当前项目最正确的推进方式不是立刻拆仓，而是：

- 先稳定 Android / Backend / docs 三个边界
- 先完成最小 API 的真实联调
- 先让协作文档成为跨端事实来源
- 最后再拆成两个独立项目

因此，当前设计的核心不是“做大而全”，而是“为未来拆分打好不会返工的基础”。
