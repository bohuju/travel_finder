# 模块规划：DI 与横切能力

## 1. 目标

通过 Hilt 管理对象生命周期，统一处理日志、错误、配置等横切关注点。

## 2. 边界与职责

- `DatabaseModule`：DB 与 DAO 提供
- `NetworkModule`：网络客户端与超时/拦截器
- `RepositoryModule`：接口到实现绑定
- `CrawlerModule`：策略实现注册

## 3. OOP 设计要点

- DIP：依赖注入替代手工 new
- 单例边界清晰：DB、HTTP Client、Repository
- 可替换性：测试环境可覆盖绑定（fake/mock）

## 4. 横切规范

- 错误分层：网络层错误 -> 仓储层语义化 -> UI 层文案
- 日志与监控：仅在 data 层记录外部依赖调用细节
- 配置集中化：API Key、超时、baseUrl 不散落业务代码

## 5. 落地任务

1. 统一 Qualifier 命名（如多数据源 crawler）
2. 为测试引入替换模块
3. 校验依赖图无循环依赖

## 6. 验收标准

- Application 启动时依赖图可正常构建
- 测试可注入 fake 仓储与 fake crawler
- 新增模块仅需新增 provider/bind，不影响调用方
