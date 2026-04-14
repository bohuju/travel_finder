# 模块规划：Data Remote Crawler

## 1. 目标

统一管理多平台抓取能力，支持按来源动态扩展。

## 2. 边界与职责

- `CrawlerStrategy`：定义抓取协议
- `XiaohongshuCrawler` / `AmapPoiCrawler`：来源特化实现
- `CrawlerFactory`：按 sourceId 提供策略实例

## 3. OOP 设计要点

- 策略模式：不同平台实现同一接口
- 工厂模式：解耦“选择策略”与“执行策略”
- OCP：新增 `DouyinCrawler` 不修改调用方逻辑
- 模板方法（可选）：抽取共通流程到 `BaseCrawler`

## 4. 关键接口建议

- `crawl(keyword: String)`
- `crawlByLocation(lat, lng, radiusMeters)`
- `getSourceName()/getSourceId()`

## 5. 落地任务

1. 抽离公共错误模型（网络、解析、限流）
2. 统一抓取结果去重规则（按 `id + source`）
3. 增加每个策略的契约测试（同输入、稳定输出结构）

## 6. 验收标准

- 新增数据源仅新增类 + DI 绑定
- 工厂返回能力与 `getSupportedSources()` 一致
- 抓取失败可追踪、可降级（返回缓存）
