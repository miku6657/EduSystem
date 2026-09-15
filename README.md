# 教学过程管理系统（EduSystem）

一个前后端分离的教学过程管理系统，仓库包含三个部分：

| 目录 | 内容 | 技术栈 |
| --- | --- | --- |
| `edumanage/springboot` | 后端服务 | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + MySQL + JWT + Spring Security + springdoc |
| `admin` | 后台管理端（教务处/管理员） | Vue 3 + Vite + TypeScript + Element Plus + 本地 Mock |
| `student-teacher` | 师生端（学生/教师）H5 | Vue 3 + Vite + TypeScript + Vant 4 + 本地 Mock（配色/卡片结构与 `admin` 对齐） |

## 各端启动方式

### 后端

```bash
cd edumanage/springboot
./mvnw spring-boot:run          # 默认 8080 端口
```

数据库初始化 / 同步（**脚本不会自动执行，需要跑一次**）：

```bash
cd edumanage/springboot
./sync-db.cmd          # Windows：双击或命令行执行
./sync-db.sh           # macOS / Linux
```

`sync-db` 会依次执行 `schema.sql`（建库 + 27 张表）与 `data.sql`（演示数据）。
两个脚本都**可重复执行**：`schema.sql` 每张表前都会先 `DROP TABLE IF EXISTS`，
`data.sql` 全部用 `INSERT IGNORE` 并把 seed 账号密码统一更新为 BCrypt ——
所以 **每次 `git pull` 之后跑一次 `sync-db` 即可**，不会出现「表已存在 / 表不存在 / 字段不存在 / 登录失败」。

> 默认账号密码为 `root/123456`（与 `application.yml` 一致），可用环境变量覆盖：
> `set MYSQL_USER=... & set MYSQL_PWD=... & set MYSQL_HOST=...`
>
> 演示账号（密码均为 `123456`）：`admin`（管理员）、`T001`（教师张伟）、`2023005001`（学生王小明）。

### 后台管理端

```bash
cd admin
npm install
npm run dev                     # http://localhost:5173
```

`vite.config.ts` 已把 `/api` 代理到 `http://localhost:8080`（`vite-plugin-mock` 已从配置中移除，`src/mock/index.ts` 与 `README.md`/`API_MANUAL.md` 里"mock 默认开启"的说法已过时），因此**必须先启动后端**再进管理端。

⚠️ 管理端的登录页当前是 **CAS 单点登录**形态（账号密码表单被 `v-if="false"` 隐藏，`admin/README.md` 与 `API_MANUAL.md` 里"mock 登录"的说明已过时），而 CAS 服务端（见 `edumanage/springboot/CAS服务器搭建方案.md`）尚未落地 —— 本地要用 `admin / 123456` 登录，需先恢复登录页的表单分支，或直接调 `POST /api/auth/login` 拿 token。

### 师生端

```bash
cd student-teacher
npm install
npm run dev                     # http://localhost:5174（mock 默认开启）
```

演示账号：学生 `2023005001 / 123456`、教师 `T001 / 123456`（与 `data.sql` 种子数据一致）。联调方式同上，`.env.development` 已随仓库提供。

师生端共 20 个页面（学生端 6：成绩 / **考试信息** / 考勤 / 补考重修 / 专升本 / 毕业资格；教师端 8：教学日志、签到、学生考勤、成绩录入、考核方式申报、监考、花名册、**调课申请**；学生教师共用 2：课表、教室申请；另加登录、首页、Layout、404），
页面结构统一为「筛选 → 统计 → 列表 / 表单 → 分页」，视觉令牌取自后台管理端，两端观感一致。详见 `student-teacher/README.md`。

## 接口约定

统一前缀 `/api`，请求头 `Authorization: Bearer <token>`。

⚠️ 目前存在**两套并存的响应契约**，联调前需先统一：

| 维度 | 前端（`admin`）约定 | 后端实际 |
| --- | --- | --- |
| 响应体 | `{ code: 0, data, msg }`（旧 Mock 契约） | `{ code: 200, message, data }` |
| 分页体 | `{ list, total, page, pageSize }` | MyBatis-Plus `{ records, total, current, size }` |

- 师生端（`student-teacher`）的请求层**同时兼容两种契约**，无需后端改动即可联调；
- 后台管理端（`admin`）的 `request.ts` 在联调提交里已改为判 `code === 200`，响应体这一项已对齐；分页体、以及教室申请/调课审批两组接口的**路径**仍有出入（详见 `student-teacher/README.md` 缺口清单第 9 条）；
- 接口清单与缺口见 `admin/API_MANUAL.md` 与 `student-teacher/README.md`。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| `edumanage/springboot/项目规范.md` | 后端分层、命名、提交规范 |
| `edumanage/springboot/数据库设计.md` | 27 张表的用途清单 |
| `edumanage/springboot/问题排查记录.md` | 历史问题与解决过程 |
| `edumanage/springboot/CAS服务器搭建方案.md` | CAS 单点登录（Apereo CAS 7.3.x）接入方案 |
| `admin/API_MANUAL.md` | 管理端接口对接手册 |
| `student-teacher/README.md` | 师生端说明、页面清单、**待后端补的缺口清单** |
