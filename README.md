# 校园创作分享平台

课程项目的本地开发基线。管理后台与微信小程序分别调用同一个 Spring Boot 后端，验证服务启动及 MySQL 查询。

| 目录 | 用途 |
| --- | --- |
| `miniapp/` | uni-app、Vue 3、TypeScript 微信小程序 |
| `admin-web/` | Vue 3、TypeScript、Vite、Element Plus 管理后台 |
| `backend/` | Spring Boot 4、MyBatis-Plus 后端 |
| `database/` | MySQL 开发库初始化脚本 |
| `docs/` | 运行、接口与协作说明 |

## 环境

- Git、JDK 21、Maven 3.9.11
- Node.js 24.18.0、npm 11.16.0
- MySQL Community Server 8.4 LTS
- 微信开发者工具、个人微信小程序测试号

Maven 与 npm 依赖版本分别记录在 `backend/pom.xml` 和两个前端锁文件。小程序脚手架基于 [DCloud 官方 vite-ts 模板](https://github.com/dcloudio/uni-preset-vue/tree/f747e3b11c51b3bff6c05e2b54f9a3173a590044)，保留微信小程序目标。

## 启动顺序

1. 克隆仓库并进入仓库根目录。

   ```text
   git clone https://github.com/jint-z/campus-creative-platform.git
   cd campus-creative-platform
   ```

2. 启动本机 MySQL 8.4，按[数据库与接口说明](docs/local-development.md)建库和账号，并按[第一周账号与权限说明](docs/week-one-auth.md)初始化账号表和管理员。
3. 从仓库根目录进入 `backend`，复制 `application-local.properties.example` 为 `application-local.properties`，填写本机数据库端口和账号密码。示例默认端口是 `3307`，若本机独立开发实例使用 `3306`，同步修改 JDBC URL。在 `backend` 目录启动后端。

   ```text
   mvn spring-boot:run
   ```

   后端默认地址为 `http://127.0.0.1:8080`。在浏览器分别打开 `/api/dev/health` 和 `/api/dev/db`，确认两条响应的 `code` 都是 `OK`。

4. 新开终端，从仓库根目录进入 `admin-web`。安装依赖并启动后，在浏览器打开 `http://127.0.0.1:5173`。

   ```text
   npm ci
   npm run dev
   ```

5. 再开一个终端，从仓库根目录进入 `miniapp`。安装依赖并启动编译。

   ```text
   npm ci
   npm run dev:mp-weixin
   ```

   登录微信开发者工具，导入 `miniapp/dist/dev/mp-weixin`，填写本人的测试号 AppID。打开「详情 → 本地设置」，仅在本项目的本地模拟器中启用「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」。打开联通验证页，检查后端服务和数据库各自的结果。测试号可按[微信官方说明](https://developers.weixin.qq.com/miniprogram/dev/devtools/sandbox.html)申请；AppID 填在开发工具中。

在 Windows PowerShell 中，若执行策略阻止 npm 的 PowerShell 入口，可用 `npm.cmd`。若 PATH 上的 Java 版本较旧，在当前终端设置 `JAVA_HOME` 指向 JDK 21，并用 `mvn -v` 确认 Maven 使用了正确版本。

## 提交前检查

| 目录 | 命令 |
| --- | --- |
| `backend/` | `mvn spotless:check verify` |
| `admin-web/` | `npm run format:check`、`npm run type-check`、`npm run build` |
| `miniapp/` | `npm run format:check`、`npm run type-check`、`npm run build:mp-weixin` |

[本地排查和接口示例](docs/local-development.md) · [分支和 PR 流程](docs/contributing.md)
