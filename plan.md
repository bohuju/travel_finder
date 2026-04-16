# Travel Finder 项目规划

## 1. 目标

做一个“旅行兴趣点聚合 + 行程规划”系统，分为两个部分：

- 后端服务：运行在 Ubuntu 服务器上，负责抓取/识别/整理用户的小红书收藏帖，统一转成可定位的兴趣点数据，存入数据库并按日更新。
- Android 前端：登录后从后端同步数据，把兴趣点显示在地图和列表中，并支持查看详情、收藏、加行程、导航。

核心目标不是“单纯抓数据”，而是把碎片化帖子变成可直接用于出行决策的结构化 POI 数据。

---

## 2. 总体架构

建议采用典型的分层 + OOP 设计：

- 表现层：Android App
- 业务层：后端 API 服务
- 领域层：POI、帖子、地址、行程等核心对象
- 基础设施层：爬虫、OCR、数据库、地图服务、任务调度

推荐的主流程：

1. 用户登录 App。
2. App 向后端请求该用户已同步的兴趣点。
3. 后端拉取或更新小红书收藏帖子。
4. 后端对帖子做文本抽取、OCR、地址解析、地理编码。
5. 后端把 POI 写入数据库，并返回给 App。
6. App 在地图上展示 POI，在列表中展示可浏览的兴趣点。

---

## 3. OOP 设计原则

### 3.1 单一职责

每个类只负责一件事：

- `PostCrawler` 只负责抓帖子。
- `OcrExtractor` 只负责从图片提取文字。
- `AddressParser` 只负责从文本中提取地址信息。
- `GeocodeService` 只负责地址和坐标转换。
- `PoiRepository` 只负责 POI 的存取。
- `MapViewModel` 只负责界面状态。

### 3.2 开闭原则

当后续增加新来源时，只新增实现类，不改核心流程：

- 新增平台爬虫：实现 `ContentSource` 接口。
- 新增 OCR 引擎：实现 `TextRecognizer` 接口。
- 新增地图服务：实现 `GeoProvider` 接口。

### 3.3 里氏替换

任何 `ContentSource` 实现都应能替换到抓取流程中，返回统一的 `RawPost` 结构。

### 3.4 接口隔离

不要设计过胖接口，推荐拆分为：

- `ContentSource`
- `TextRecognizer`
- `GeoCoder`
- `RouteNavigator`
- `SyncJob`

### 3.5 依赖倒置

业务层依赖抽象接口，不直接依赖某个具体平台 SDK：

- 后端服务逻辑依赖 `TextRecognizer`，不直接依赖某个 OCR 厂商。
- Android 导航入口依赖 `RouteNavigator`，不把导航逻辑写死在页面里。

---

## 4. 后端规划

### 4.1 后端职责

后端是系统的“数据加工中心”，负责：

- 用户登录与身份校验。
- 管理用户的小红书收藏来源。
- 抓取帖子正文、图片和评论中的地址线索。
- 对图片执行 OCR。
- 对文本执行地址抽取和标准化。
- 调用地理编码服务，把地址转成经纬度。
- 存储 POI、帖子、同步状态和任务日志。
- 每日定时更新收藏内容。

### 4.2 后端核心对象

建议定义以下领域对象：

- `User`：用户信息和授权信息。
- `SourceAccount`：用户绑定的小红书来源账号或导入任务。
- `RawPost`：未加工的原始帖子数据。
- `PostAsset`：帖子图片、视频封面、截图等资源。
- `ExtractedText`：OCR 或正文提取后的文字。
- `AddressCandidate`：候选地址。
- `Poi`：最终兴趣点。
- `SyncTask`：同步任务。
- `SyncLog`：同步结果和错误日志。

### 4.3 后端模块划分

建议按模块拆分为：

- `auth`：登录、令牌、权限控制。
- `source`：小红书来源管理。
- `crawler`：帖子抓取。
- `ocr`：图片识别。
- `parser`：地址提取、去噪、标准化。
- `geo`：地理编码、逆地理编码。
- `poi`：兴趣点管理。
- `sync`：增量同步、定时任务、去重。
- `map`：地图展示所需数据封装。
- `navigation`：给前端返回导航参数。

### 4.4 后端类设计

推荐使用策略模式 + 工厂模式：

```text
ContentSource
├── XiaohongshuSource
├── BackupImportSource

TextRecognizer
├── BaiduOcrRecognizer
├── MockRecognizer

GeoCoder
├── BaiduGeoCoder
├── AmapGeoCoder

AddressExtractor
├── RegexAddressExtractor
├── LlmAddressExtractor

PoiSyncService
├── PullUserPostsUseCase
├── ParseAndResolvePoiUseCase
├── PersistPoiUseCase
```

### 4.5 后端数据流

#### 场景 A：帖子转 POI

1. `ContentSource` 拉取收藏帖子。
2. `AddressExtractor` 从正文中提取地址候选。
3. 图片路径进入 `TextRecognizer` 做 OCR。
4. 合并正文和 OCR 结果，做地址标准化。
5. `GeoCoder` 把地址转成经纬度。
6. 生成 `Poi` 并去重。
7. 保存到数据库。

#### 场景 B：每日增量更新

1. `SyncScheduler` 每日触发。
2. 找出最近更新的收藏帖。
3. 重跑抽取流程。
4. 对比旧 POI，更新地址、坐标、标签、热度。
5. 记录同步日志。

### 4.6 后端 API 建议

建议提供以下接口：

- `POST /api/auth/login`
- `GET /api/users/me`
- `GET /api/pois`
- `GET /api/pois/{id}`
- `GET /api/pois/map`
- `GET /api/pois/list`
- `POST /api/sync/run`
- `GET /api/sync/status`
- `GET /api/trips`
- `POST /api/trips`
- `GET /api/navigation/plan`

### 4.7 数据库建议

后端数据库建议至少包含：

- `users`
- `source_accounts`
- `raw_posts`
- `post_assets`
- `extracted_texts`
- `address_candidates`
- `pois`
- `poi_tags`
- `sync_tasks`
- `sync_logs`

---

## 5. 前端规划

### 5.1 前端职责

Android App 负责：

- 用户登录。
- 请求后端 POI 数据。
- 将 POI 展示到地图和列表。
- 显示用户当前位置。
- 支持 POI 详情、收藏、加入行程。
- 调起百度地图或高德地图导航。

### 5.2 前端页面结构

建议页面如下：

- `LoginFragment`：登录和账号绑定。
- `HomeFragment`：推荐兴趣点、最近同步结果。
- `MapFragment`：地图点位展示、当前位置、附近 POI。
- `PoiDetailFragment` 或底部详情卡：POI 详情、图片、标签、导航按钮。
- `PlanningFragment`：兴趣点列表、行程编排、顺序调整。
- `SettingsFragment`：同步设置、地图源选择、导航源选择。

### 5.3 前端类设计

建议保持你现在的 Clean Architecture 结构：

- `ui`：页面
- `viewmodel`：状态控制
- `state`：UI 状态
- `repository`：数据获取
- `service`：网络请求和导航封装

推荐引入的核心类：

- `PoiRepository`
- `TripRepository`
- `UserRepository`
- `PoiSyncViewModel`
- `MapViewModel`
- `TripViewModel`
- `NavigationLauncher`
- `LocationProvider`

### 5.4 前端数据流

1. 登录后拿到 token。
2. `MapViewModel` 请求后端 POI 列表。
3. `PoiAdapter` 展示列表。
4. 地图上渲染 marker。
5. 点击 POI 后显示详情。
6. 点击“添加到行程”后写入本地状态或请求后端。
7. 点击“去这里”时调起第三方导航。

### 5.5 前端地图能力

地图层建议支持：

- 当前定位点。
- POI marker。
- 当前范围内聚合点。
- 点击 marker 显示标题、摘要、地址。
- 搜索框联想。
- 列表和地图联动。

---

## 6. 重点功能设计

### 6.1 小红书帖子转地址

优先级建议：

1. 正文/标题直接抽地址。
2. 图片 OCR 提取地址。
3. 地址标准化。
4. 地理编码。
5. 人工确认兜底。

这样可以降低 OCR 误识别带来的错误点位。

### 6.2 地图展示

每个 POI 至少包含：

- 名称
- 地址
- 经纬度
- 图片
- 标签
- 来源帖子
- 热度或评分

### 6.3 导航唤起

用户点击“去这里”时：

- 优先提供百度地图导航。
- 也提供高德地图导航作为备用。
- 若地图 App 不存在，则退回到网页导航或提示安装。

### 6.4 当前位置

用户位置用于：

- 计算附近 POI。
- 作为路线起点。
- 显示与 POI 的距离。

---

## 7. 创意增强功能

这些功能会明显提高可用性：

- `智能路线排序`：根据地理位置、开放时间、用户停留时长自动排序一天行程。
- `一键生成半日/一日路线`：根据收藏 POI 生成推荐路线。
- `去重与合并`：同一 POI 多篇帖子出现时合并展示。
- `标签筛选`：美食、亲子、摄影、夜景、室内、雨天可玩。
- `天气联动`：雨天优先推荐室内点位。
- `离线缓存`：地铁里也能查看已同步的兴趣点。
- `行程分享卡片`：导出成图片或链接分享给别人。
- `收藏优先级`：用户给每个点打星标，生成个人偏好模型。
- `同步提醒`：每天有新帖子时提醒更新。
- `地图聚合缩放`：缩小时做聚合，放大时展开 marker。

---

## 8. 推荐实现顺序

### 阶段 1：最小可用版本

- 用户登录
- 后端同步帖子
- 地址提取
- POI 入库
- Android 展示地图和列表
- 定位与导航唤起

### 阶段 2：体验增强

- 搜索联想
- POI 详情页
- 行程创建
- 路线排序
- 去重合并

### 阶段 3：智能化

- 自动生成旅行路线
- 天气推荐
- 标签偏好推荐
- 同步任务监控

---

## 9. 目录建议

### 9.1 后端目录

```text
backend/
├── src/main/kotlin/com/travelfinder/
│   ├── auth/
│   ├── crawler/
│   ├── ocr/
│   ├── parser/
│   ├── geo/
│   ├── poi/
│   ├── sync/
│   ├── trip/
│   └── common/
└── src/main/resources/
```

### 9.2 Android 目录

```text
app/src/main/java/com/travelfinder/
├── data/
├── domain/
├── presentation/
├── di/
└── util/
```

---

## 10. 风险与约束

- 小红书数据获取要优先考虑合规方式，能用官方能力就不用强爬。
- OCR 识别结果需要人工确认兜底，否则地址误差会很大。
- 地理编码的结果要做地址标准化和去重。
- 地图与导航最好做多供应商适配，避免单一厂商不可用。
- 每日同步任务要有失败重试和日志，不然很难排查。

---

## 11. 总结

这个项目的本质是：

- 后端把“帖子内容”加工成“可定位、可规划、可同步”的 POI 数据。
- 前端把 POI 变成“地图可视化 + 行程可操作”的用户界面。
- OOP 用来保证系统能扩展来源、扩展 OCR、扩展地图服务，而不是把逻辑写死在页面里。

如果后续继续扩展，最值得做的是“智能路线排序”和“收藏兴趣画像”，这两个功能会显著提升使用便利性。
