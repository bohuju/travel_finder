# Travel Finder 模块规划总览（OOP）

本目录基于 `DESIGN.md` 拆分为多个可落地模块规划，遵循 Clean Architecture + MVVM，并显式应用 SOLID 与常见设计模式。

## 模块列表

1. `01-domain-model-plan.md`：领域模型与业务规则
2. `02-data-local-plan.md`：本地数据存储（Room）
3. `03-data-remote-crawler-plan.md`：远程抓取与策略扩展
4. `04-repository-plan.md`：仓储聚合与数据编排
5. `05-presentation-plan.md`：UI/MVVM 状态管理
6. `06-di-crosscutting-plan.md`：依赖注入与横切关注点

## 统一约束

- 依赖方向固定：`presentation -> domain <- data`
- 领域层纯 Kotlin，不依赖 Android 框架
- 优先组合而非继承，继承仅用于表达稳定抽象（如 `Locatable`）
- 所有模块对外通过接口暴露能力，便于测试替身与未来替换
