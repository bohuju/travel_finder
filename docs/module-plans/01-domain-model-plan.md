# 模块规划：Domain Model

## 1. 目标

承载核心业务语义与规则，确保“行程规划”能力不受数据源与 UI 变化影响。

## 2. 边界与职责（SRP）

- `Location`：地理位置值对象
- `Locatable`：可定位对象统一抽象
- `Post`：内容源帖子模型，负责 `toPOI()` 映射
- `POI`：兴趣点业务模型
- `Review`：评价模型
- `TripPlan`：行程聚合根，维护 POI 集合与评分/时长计算规则

## 3. OOP 设计要点

- 抽象稳定点：`Locatable`
- 行为内聚：`TripPlan` 持有 `addPOI/removePOI/calculateOverallRating`
- 里氏替换：`Post/POI/TripPlan` 均可作为 `Locatable` 使用
- 封装不变量：防止重复 POI、非法顺序、空名称等

## 4. 接口与协作

- 输入：UseCase 传入模型或基础参数
- 输出：Repository/UseCase 返回领域模型
- 协作：`Post.toPOI()` 是 data->domain 映射关键桥梁

## 5. 落地任务

1. 将 `TripPlan` 规则补全为可测试纯函数/成员函数
2. 增加模型校验（构造期或工厂）
3. 增加映射一致性单测（`Post.toPOI()`）

## 6. 验收标准

- 领域层无 Android import
- `TripPlan` 核心规则测试覆盖率 >= 80%
- 新增数据源无需改动领域模型
