# Backend

`Travel Finder` 服务端已初始化为一个最小可运行的 `Kotlin + Ktor` 工程。

当前已提供：

- `GET /health`
- `GET /api/pois`
- `GET /api/pois/{id}`
- `GET /api/trips`
- `POST /api/trips`

当前实现说明：

- 已接入数据库配置与 schema 初始化机制
- 默认支持 PostgreSQL，也支持 H2 作为本地/测试持久化回退
- 最小 API 已从纯内存实现切换到数据库仓储
- 尚未接入鉴权、同步任务与外部供应商
- 代码结构已按后续模块化方向拆出 `application / poi / trip / common`

数据库环境变量：

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/travel_finder
DATABASE_USER=travel_finder
DATABASE_PASSWORD=secret
```

如果不提供上述变量，服务会默认使用本地 H2 文件数据库。

本地运行：

```bash
./backend/gradlew -p ./backend run
```

本地测试：

```bash
./backend/gradlew -p ./backend test
```

详细规划见 [plan.md](/home/bohuju/self_project/travel_finder/plan.md)。
