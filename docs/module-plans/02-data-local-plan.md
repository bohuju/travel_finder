# 模块规划：Data Local（Room）

## 1. 目标

提供离线缓存与持久化能力，保障弱网可用与冷启动性能。

## 2. 边界与职责

- `entity/*`：数据库结构定义
- `dao/*`：最小化查询/写入接口
- `db/AppDatabase`：数据库入口与版本管理

## 3. OOP 设计要点

- ISP：DAO 接口按实体拆分，避免“万能 DAO”
- DIP：Repository 依赖 DAO 抽象行为，不泄漏 SQL 细节
- SRP：Entity 仅负责存储形态，不承载业务行为

## 4. 核心对象

- `PostEntity`, `TripPlanEntity`, `POIEntity`, `TripPOIEntity`
- `PostDao`, `TripDao`

## 5. 落地任务

1. 明确主键/索引策略（`id`, `publishDate`, `cachedAt`）
2. 建立 Entity <-> Domain Mapper（单独 mapper 文件）
3. 设计缓存淘汰策略（按时间或容量）

## 6. 验收标准

- 所有 DAO 均有集成测试（内存数据库）
- Mapper 往返转换无字段丢失
- 数据库升级脚本可回归验证
