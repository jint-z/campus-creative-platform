# 第一周账号与权限接口

## 初始化开发数据库和管理员

使用本地 MySQL 管理员账号，在仓库根目录启动 MySQL 客户端。新库依次执行：

```sql
SOURCE database/init.sql;
CREATE USER 'campus_dev'@'127.0.0.1' IDENTIFIED BY 'REPLACE_WITH_YOUR_PASSWORD';
GRANT SELECT ON campus_creative_dev.* TO 'campus_dev'@'127.0.0.1';
```

已有开发库从这里继续。若本机应用数据库账号或 Host 与示例不同，请同步调整授权语句。

```sql
SOURCE database/week-one-accounts.sql;
GRANT INSERT ON campus_creative_dev.accounts TO 'campus_dev'@'127.0.0.1';
```

在后端的 `application-local.properties` 中启用 `dev`，填写本机数据库连接和 `app.dev.admin-password`。启动后端时，后端用该密码创建 `admin`。如账号已存在，启动保留原密码。已有管理员后可从本机配置移除初始化密码。每位组员自行设置本地密码。

## 账号 API

默认地址为 `http://127.0.0.1:8080`。请求体为 JSON，响应统一使用 `{code, message, data}`。

| 请求 | 成功响应 | 凭据 |
| --- | --- | --- |
| `POST /api/auth/student/register` | `201 OK`，`data: null` | JSON 账号密码 |
| `POST /api/auth/student/login` | `200 OK`，返回学生账号和令牌 | JSON 账号密码 |
| `POST /api/auth/admin/login` | `200 OK`，返回管理员账号和令牌 | JSON 账号密码 |
| `POST /api/auth/logout` | `200 OK`，`data: null` | Bearer 令牌 |

注册和登录请求示例：

```json
{"username":"student_a","password":"<本机测试密码>"}
```

用户名由 3–32 位 ASCII 字母、数字或下划线组成，入库和查找时统一转为小写。密码至少 8 个字符且最多 72 个 UTF-8 字节。服务端固定为注册账号赋予 `STUDENT` 角色。注册成功后调用学生登录接口。管理员登录的用户名为 `admin`。

登录成功示例：

```json
{
  "code":"OK",
  "message":"登录成功",
  "data":{
    "accessToken":"<服务端返回的令牌>",
    "expiresIn":28800,
    "account":{"id":2,"username":"student_a","role":"STUDENT"}
  }
}
```

| HTTP | code | 处理方式 |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 提示检查请求格式、用户名和密码 |
| 409 | `USERNAME_TAKEN` | 提示换一个用户名 |
| 401 | `INVALID_CREDENTIALS` | 提示重新输入用户名和密码 |
| 401 | `UNAUTHENTICATED` | 清除本地令牌，回到登录页 |
| 403 | `FORBIDDEN` | 提示当前账号无权操作 |
| 503 | `ACCOUNT_STORE_UNAVAILABLE` | 提示检查本地 MySQL、表和账号授权 |

## 业务接口接入

- 小程序发布、上传、列表与详情使用 `/api/student/**`，后台列表、详情与下架使用 `/api/admin/**`。前端为业务请求携带 `Authorization: Bearer <accessToken>`。后端控制器用 `@AuthenticationPrincipal Account account` 获取账号，发布时从 `account.id()` 取得作者 ID。
- 两端读取图片时对 `GET /api/media/**` 携带相同请求头。小程序用 `uni.downloadFile` 下载后展示临时文件。Web 用 `fetch` 获取 Blob，生成对象 URL，组件卸载时释放 URL。作品状态、图片引用及各业务接口的字段由相应模块维护。
- 切换学生账号时调用退出接口，清除客户端的令牌、账号和上一个账号的页面状态，再用新账号登录。令牌 8 小时到期，服务端重启后需重新登录。
- 开发模式的后台默认来源为 `http://127.0.0.1:5173`。换端口时配置 `app.dev.admin-origin`。小程序按 README 使用本机模拟器请求配置。
