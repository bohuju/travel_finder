# 模块规划：Presentation（MVVM）

## 1. 目标

以状态驱动 UI，隔离界面逻辑与业务逻辑，保证可维护与可测试。

## 2. 边界与职责

- `ui/*`：Fragment/Activity 生命周期与事件收集
- `viewmodel/*`：状态编排、调用 UseCase
- `state/*`：UI 状态密封类
- `adapter/*`：列表渲染与交互转发

## 3. OOP 设计要点

- SRP：ViewModel 不处理视图细节；Fragment 不处理业务规则
- ISP：按页面拆分状态模型（`MapUiState`, `TripListUiState`）
- 观察者模式：`StateFlow` 驱动单向数据流

## 4. 交互约束

- View -> ViewModel：只传用户意图
- ViewModel -> View：只暴露不可变状态
- 错误统一映射到可展示文案

## 5. 落地任务

1. 统一页面状态机（Idle/Loading/Success/Error）
2. 抽离一次性事件通道（如 Toast/导航）
3. 为 ViewModel 补充协程测试

## 6. 验收标准

- ViewModel 单测覆盖主要状态流转
- Fragment 无业务计算逻辑
- 横竖屏/重建后状态可恢复
