# Travel Finder

`Travel Finder` 是一个“旅行兴趣点聚合 + 行程规划”系统。当前仓库处于从 Android 原型演进到“Android 客户端 + Backend 服务端”协作架构的阶段，已经完成 Android 工程迁移与 Backend 最小 Ktor 骨架落地。

本仓库当前采用单仓双目录推进开发，但最终目标不是长期维持一个大仓库，而是：

- 拆分为两个独立代码项目：
  - `travel-finder-android`
  - `travel-finder-backend`
- 保留一套独立维护的前后端协作文档，用于约束 API、领域模型、联调流程和部署约定

## 当前仓库结构

```text
travel_finder/
├── android/   # Android 客户端工程
├── backend/   # Kotlin + Ktor 服务端工程
├── docs/      # 当前阶段共享文档与未来协作文档基线
├── scripts/   # 仓库级脚本
├── DESIGN.md
├── plan.md
└── README.md
```

## 当前阶段

目前已经完成：

- Android 工程迁移到 `android/`
- Backend 最小 Ktor 服务初始化
- 最小 API 落地：
  - `GET /health`
  - `GET /api/pois`
  - `GET /api/pois/{id}`
  - `GET /api/trips`
  - `POST /api/trips`
- API / 领域 / 进度文档目录建立

目前尚未完成：

- Backend 接入 PostgreSQL
- Android 切换到真实后端 API
- 鉴权、同步任务、OCR、地址解析、地理编码
- 最终拆分成两个独立项目

## 本地开发

Android 构建：

```bash
./android/gradlew -p ./android compileDebugKotlin
./android/gradlew -p ./android assembleDebug
```

Backend 运行与测试：

```bash
./backend/gradlew -p ./backend run
./backend/gradlew -p ./backend test
./backend/gradlew -p ./backend build
```

## 协作文档

当前 `docs/` 目录不仅用于仓库说明，也作为未来前后端拆分后的协作文档基线：

- [docs/api/README.md](/home/bohuju/self_project/travel_finder/docs/api/README.md)
  API 契约、请求响应结构、错误码、版本变更记录
- [docs/domain/README.md](/home/bohuju/self_project/travel_finder/docs/domain/README.md)
  跨端共享的领域模型定义与边界
- [docs/deployment/README.md](/home/bohuju/self_project/travel_finder/docs/deployment/README.md)
  后端部署、环境变量、运维与发布约定
- [docs/current-progress.md](/home/bohuju/self_project/travel_finder/docs/current-progress.md)
  当前阶段进度与可验证状态
- [docs/module-plans/README.md](/home/bohuju/self_project/travel_finder/docs/module-plans/README.md)
  Android 现有模块计划与重构路线

推荐的长期演进方式是：

1. 当前在单仓中把接口、领域和联调文档写稳定。
2. 等 Android 与 Backend 都能独立运行后，拆分为两个独立代码项目。
3. 将协作文档继续独立维护，作为前后端共同遵守的事实来源。

## 相关文档

- 架构与拆分规划：[plan.md](/home/bohuju/self_project/travel_finder/plan.md)
- 当前系统设计：[DESIGN.md](/home/bohuju/self_project/travel_finder/DESIGN.md)
- 双项目拆分执行方案：[docs/split-execution-plan.md](/home/bohuju/self_project/travel_finder/docs/split-execution-plan.md)
- 当前项目进度：[docs/current-progress.md](/home/bohuju/self_project/travel_finder/docs/current-progress.md)
- 百度地图改动说明：[docs/baidu-map-update.md](/home/bohuju/self_project/travel_finder/docs/baidu-map-update.md)
