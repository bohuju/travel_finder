# Domain Docs

此目录用于存放跨端共享的领域模型说明。

目标是让 Android 与 Backend 对关键对象有一致理解，而不是各自维护一份“看起来差不多”的模型。

建议优先沉淀的对象：

- `Poi`
- `Trip`
- `User`
- `Location`
- `ErrorResponse`
- `SyncStatus`

后续再补充 Backend 内部处理对象：

- `RawPost`
- `PostAsset`
- `ExtractedText`
- `AddressCandidate`
- `SyncTask`
- `SyncLog`

每个领域对象文档建议至少包含：

- 字段定义
- 字段语义
- 可空性
- 状态变化规则
- 是否进入 API 契约

当前已落地：

- [poi.md](/home/bohuju/self_project/travel_finder/docs/domain/poi.md)
- [trip.md](/home/bohuju/self_project/travel_finder/docs/domain/trip.md)
- [error-response.md](/home/bohuju/self_project/travel_finder/docs/domain/error-response.md)
- [location.md](/home/bohuju/self_project/travel_finder/docs/domain/location.md)
- [sync-status.md](/home/bohuju/self_project/travel_finder/docs/domain/sync-status.md)
