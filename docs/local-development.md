# 本地开发与联通检查

## 准备数据库

使用 MySQL Community Server 8.4 LTS。若本机已有教学用的 MySQL，可以通过[官方 ZIP 手动配置方式](https://dev.mysql.com/doc/refman/8.4/en/windows-install-archive.html)使用独立数据目录和端口 `3307`，将监听地址设置为 `127.0.0.1`。如果项目使用的 8.4 实例监听其他端口，下面的登录命令与后端 JDBC URL 都要同步。

在仓库根目录打开终端，以该实例的数据库管理员账号登录：

```text
mysql --host=127.0.0.1 --port=3307 --user=root --password
```

在 MySQL 提示符下执行以下命令。将 `REPLACE_WITH_YOUR_PASSWORD` 换成本机专用密码，再把同一密码填到 `backend/application-local.properties`。

```sql
SOURCE database/init.sql;
CREATE USER 'campus_dev'@'127.0.0.1' IDENTIFIED BY 'REPLACE_WITH_YOUR_PASSWORD';
GRANT SELECT ON campus_creative_dev.* TO 'campus_dev'@'127.0.0.1';
```

用 `campus_dev` 登录该实例并执行 `SELECT 1;` 检查账号。账号创建只需执行一次。配置文件与密码保存在各成员本机，仓库中的 `.example` 文件使用占位值。

## 接口

探针只在 Spring `dev` profile 启用。默认后端监听 `127.0.0.1:8080`，后台允许来源为 `http://127.0.0.1:5173`。

| 请求 | 预期 |
| --- | --- |
| `GET /api/dev/health` | HTTP 200，`code: OK`，`data.status: UP` |
| `GET /api/dev/db` | HTTP 200，`code: OK`，`data.result: 1` |
| 数据库不可用时请求 `/api/dev/db` | HTTP 503，`code: DB_UNAVAILABLE`，`data: null` |
| 探针内部异常 | HTTP 500，`code: INTERNAL_ERROR`，`data: null` |

```json
{"code":"OK","message":"数据库查询成功","data":{"result":1}}
```

两端的 API 地址默认相同。需要改端口时，在相应前端目录把 `.env.example` 复制为 `.env.local` 并设置 `VITE_API_BASE_URL`，然后重启前端。后端可用 `server.port` 覆盖端口。若修改管理后台的浏览器地址，在后端本地配置里同步设置 `app.dev.admin-origin`，值为完整的来源（协议、主机、端口）。

## 排查顺序

1. **后端启动失败。** 在 `backend/` 目录执行，确认 `mvn -v` 显示 Java 21，且 `8080` 可用。
2. **两个探针都打不开。** 先访问 `http://127.0.0.1:8080/api/dev/health`。连接失败时检查后端进程和端口，404 时检查 `application-local.properties` 中的 `spring.profiles.active=dev`。
3. **仅数据库失败。** 确认 MySQL 8.4 正在监听 JDBC URL 中的端口，地址、开发库、账号及密码均与本机配置一致。用同一账号手动执行 `SELECT 1`。
4. **浏览器跨域。** 默认使用 `http://127.0.0.1:5173` 打开后台。浏览器里的 `localhost` 与 `127.0.0.1` 是不同来源。
5. **小程序请求失败。** 确认导入的是 `dist/dev/mp-weixin`，已使用测试号打开，并按 README 开启本地请求调试设置。检查开发工具 Network 标签、后端地址和端口。`127.0.0.1` 的配置用于电脑上的开发工具模拟器。

## 组员复现

从干净克隆开始按 README 启动。在两端确认两项通过并查看真实请求。仅停止本项目的开发数据库，重试后服务应仍通过、数据库应失败。停止后端时两项应都显示失败。恢复后再次重试，确认两项回到通过。反馈时附上 `main` 提交、所负责部分、实际步骤和结果。
