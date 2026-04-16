# Deployment Docs

此目录用于存放 Backend 的部署、环境变量、运维、任务调度和发布说明。

建议后续补齐：

- 本地开发环境说明
- 测试环境与生产环境差异
- 环境变量清单
- PostgreSQL 初始化与迁移方式
- Ktor 启动方式
- 健康检查与日志说明
- Nginx / HTTPS / systemd 部署方式

这部分文档虽然偏后端，但对 Android 联调也很重要，因为它决定了：

- 测试环境地址如何获取
- 健康检查如何确认
- 接口版本如何发布
- 出问题时如何判断是客户端问题还是服务端环境问题

## 当前 backend 已支持的最小运行配置

环境变量：

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/travel_finder
DATABASE_USER=travel_finder
DATABASE_PASSWORD=secret
PORT=8080
```

当前行为：

- 如果提供 `DATABASE_URL / DATABASE_USER / DATABASE_PASSWORD`，Backend 会按给定 JDBC 配置连接数据库
- 如果未提供数据库变量，Backend 会默认回退到本地 H2 文件数据库
- 应用启动时会自动执行当前 `db/migration` 下的 schema 初始化脚本

当前验证结论：

- 已通过 H2 兼容模式完成本地持久化与测试验证
- PostgreSQL 配置路径已接入代码，但仍需要在真实 PostgreSQL 实例上完成部署级验证
