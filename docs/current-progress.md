# Travel Finder 当前项目进度

## 1. 当前阶段

项目目前处于 **从单一 Android 原型向“Android + Backend”双端架构演进，并完成 Backend 最小落地** 的阶段。

已经完成的重点，是把项目方向从“仅客户端原型”明确成“单仓双目录”的长期结构，完成 Android 工程迁移，并启动第一版 Ktor 后端骨架。

当前状态可以概括为：

- Android 端已有可运行原型
- 百度地图已接入并可编译
- 仓库级架构规划已重写
- Android 工程已迁移到 `android/`
- Backend 已初始化最小 Ktor 服务骨架
- Backend 已具备数据库持久化与 schema 初始化能力
- Backend 已支持 PostgreSQL 配置，当前本地验证基于 H2 兼容模式

---

## 2. 已完成内容

### 2.1 架构与文档

已完成：

- 重写 [plan.md](/home/bohuju/self_project/travel_finder/plan.md)，将其升级为仓库级架构规划文档
- 明确项目采用 `android/ + backend/ + docs/ + scripts/` 的单仓双目录结构
- 明确 Android 与 Backend 的职责边界
- 明确 Backend 未来默认技术栈为 `Kotlin + Ktor + PostgreSQL`
- 新增根目录 [README.md](/home/bohuju/self_project/travel_finder/README.md)
- 初始化服务端骨架并更新 [backend/README.md](/home/bohuju/self_project/travel_finder/backend/README.md)
- 补充并更新共享文档：
  - [docs/api/README.md](/home/bohuju/self_project/travel_finder/docs/api/README.md)
  - [docs/domain/README.md](/home/bohuju/self_project/travel_finder/docs/domain/README.md)
  - [docs/deployment/README.md](/home/bohuju/self_project/travel_finder/docs/deployment/README.md)
  - [docs/current-progress.md](/home/bohuju/self_project/travel_finder/docs/current-progress.md)
  - [docs/split-execution-plan.md](/home/bohuju/self_project/travel_finder/docs/split-execution-plan.md)

### 2.2 Backend 服务端

Backend 目前已具备以下最小能力：

- `backend/` Gradle 工程初始化完成
- Ktor 最小服务已可本地启动
- 数据库配置入口已建立
- schema 初始化机制已建立
- 最小 API 已落地：
  - `GET /health`
  - `GET /api/pois`
  - `GET /api/pois/{id}`
  - `GET /api/trips`
  - `POST /api/trips`
- `poi / trip / application / common` 基础模块结构已建立
- `database / poi / trip / application / common` 基础模块结构已建立
- `POI / Trip` 已接入数据库仓储
- 基础测试已补齐
- 根目录已可直接使用 `./backend/gradlew -p ./backend ...` 执行服务端命令

### 2.3 Android 客户端

Android 端已具备以下基础能力：

- Clean Architecture 基础分层
- POI 搜索与列表展示
- 行程选择与创建基础逻辑
- 百度地图 SDK 接入
- 百度定位 SDK 接入
- 地图 marker 渲染
- 列表与地图联动
- 百度地图 `InfoWindow` 详情气泡
- 百度地图导航唤起

相关说明文档：

- [docs/baidu-map-update.md](/home/bohuju/self_project/travel_finder/docs/baidu-map-update.md)

### 2.4 仓库迁移

已完成：

- 当前 Android 工程已经迁移到 `android/` 目录
- Android 构建命令已改为从仓库根调用 `./android/gradlew -p ./android ...`
- 调试脚本 [scripts/run_android_debug.sh](/home/bohuju/self_project/travel_finder/scripts/run_android_debug.sh) 已适配新路径
- 根目录结构已稳定为 `android/ + backend/ + docs/ + scripts/`

---

## 3. 当前可验证状态

已验证：

- `./android/gradlew -p ./android compileDebugKotlin`
- `./backend/gradlew -p ./backend clean test`
- `./backend/gradlew -p ./backend build`

当前结果：

- Android 编译通过
- Backend 测试通过
- Backend 构建通过
- Backend 数据库迁移与持久化路径通过本地验证

已知情况：

- 仍有少量历史代码警告，例如未使用参数、弃用 API 警告
- 不影响当前 Android 工程编译
- 当前已验证 H2 持久化路径
- PostgreSQL 生产环境链路已具备配置入口，但尚未在真实 PostgreSQL 实例上完成本轮验证

---

## 4. 尚未完成内容

### 4.1 Backend

Backend 已完成骨架初始化，但仍尚缺：

- 鉴权模块
- 同步任务框架
- OCR、地址解析、地理编码抽象与实现
- 真实 PostgreSQL 环境验证与部署脚本

### 4.2 Android 接口切换

目前 Android 仍以现有本地/临时数据路径为主，尚未完成：

- 切换为真正调用后端 API
- DTO / API contract 的跨端落地
- 登录态与服务端用户体系打通

### 4.3 部署文档

部署文档目录已预留，但尚未写完整内容：

- [docs/deployment/README.md](/home/bohuju/self_project/travel_finder/docs/deployment/README.md)

后续需要补：

- Ubuntu 服务器部署步骤
- Ktor 启动方式
- PostgreSQL 初始化
- Nginx / HTTPS / systemd
- 环境变量与日志说明

---

## 5. 当前进行中的工作

当前仓库中正在进行的主要工作是：

- 继续收尾仓库迁移后的文档与路径同步
- Backend 从“可持久化基础版”向“可联调真实服务”推进
- 为后续 Android 接真实后端接口做接口与数据层准备
- 为后续拆分为 `android / backend / contracts-docs` 三仓形态做执行方案准备

这意味着当前工作区已经完成结构定型，但仍处于 **Backend 基础设施补齐前的过渡阶段**。

---

## 6. 建议的下一步

按照当前规划，建议按下面顺序继续推进：

1. 整理并提交当前仓库迁移与 Backend 骨架改动
2. 在真实 PostgreSQL 环境上验证当前数据库路径
3. 补充数据库 schema、迁移与配置说明
4. 写第一版部署文档
5. 让 Android 开始切换到真实后端接口

---

## 7. 一句话总结

项目已经完成“方向定型 + Android 原型 + 百度地图能力 + 仓库结构升级 + Backend 最小骨架启动 + 数据库基础设施接入”的关键第一段工作，下一阶段的核心任务是 **把 Backend 从可持久化基础版推进到可联调、可部署的真实服务**。
