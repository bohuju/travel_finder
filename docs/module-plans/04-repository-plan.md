# 模块规划：Repository

## 1. 目标

整合本地与远程数据源，对 Domain 暴露稳定接口。

## 2. 边界与职责

- `PostRepository` / `TripRepository`（domain 接口）
- `PostRepositoryImpl` / `TripRepositoryImpl`（data 实现）

## 3. OOP 设计要点

- DIP：UseCase 仅依赖 Repository 接口
- SRP：仓储只做数据编排，不写业务规则
- 封装数据源选择策略：remote-first / local-first / cache-only

## 4. 协作流程

1. Repository 接收 UseCase 请求
2. 决策调用 crawler 或 DAO
3. 统一映射为 Domain 模型
4. 通过 `Result`/`Flow` 对外返回

## 5. 落地任务

1. 明确缓存回填策略（remote 成功后写 local）
2. 标准化错误语义（业务可读）
3. 引入 repository 层单测（mock DAO + crawler）

## 6. 验收标准

- UseCase 不直接触达 DAO/crawler
- 仓储失败路径与降级路径均可测试
- 所有对外接口返回类型稳定（`Result`/`Flow`）
