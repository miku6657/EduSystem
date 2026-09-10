# 教学过程管理系统（EduSystem）

一个前后端分离的教学过程管理系统，仓库包含三个部分：

| 目录 | 内容 | 技术栈 |
| --- | --- | --- |
| `edumanage/springboot` | 后端服务 | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + MySQL + JWT + Spring Security + springdoc |
| `admin` | 后台管理端（教务处/管理员） | Vue 3 + Vite + TypeScript + Element Plus + 本地 Mock |
| `student-teacher` | 师生端（学生/教师）H5 | Vue 3 + Vite + TypeScript + Vant 4 + 本地 Mock |

## 各端启动方式

### 后端

```bash
cd edumanage/springboot
./mvnw spring-boot:run          # 默认 8080 端口
```

数据库需手工导入（脚本不会自动执行）：

```bash
mysql -uroot -p < schema.sql    # 建库 eduSYSTEM + 24 张表
mysql -uroot -p eduSYSTEM < data.sql   # 测试数据
```

> 注意：`data.sql` 里的账号密码目前是明文，而登录接口用 BCrypt 校验，直接导入后无法登录，需先替换为 BCrypt 哈希。

### 后台管理端

```bash
cd admin
npm install
npm run dev                     # http://localhost:5173（mock 默认开启）
```

演示账号 `admin / 123456`。联调后端时把 `.env.development` 的 `VITE_USE_MOCK` 改为 `false`（该文件需自行创建，仓库中缺失）。

### 师生端

```bash
cd student-teacher
npm install
npm run dev                     # http://localhost:5174（mock 默认开启）
```

演示账号：学生 `2023005001 / 123456`、教师 `T1001 / 123456`。联调方式同上，`.env.development` 已随仓库提供。

## 接口约定

统一前缀 `/api`，请求头 `Authorization: Bearer <token>`。

⚠️ 目前存在**两套并存的响应契约**，联调前需先统一：

| 维度 | 前端（`admin`）约定 | 后端实际 |
| --- | --- | --- |
| 响应体 | `{ code: 0, data, msg }` | `{ code: 200, message, data }` |
| 分页体 | `{ list, total, page, pageSize }` | MyBatis-Plus `{ records, total, current, size }` |

- 师生端（`student-teacher`）的请求层**同时兼容两种契约**，无需后端改动即可联调；
- 后台管理端（`admin`）只认 `code === 0`，需要后端调整或前端适配后才能联调；
- 接口清单与缺口见 `admin/API_MANUAL.md` 与 `student-teacher/README.md`。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| `edumanage/springboot/项目规范.md` | 后端分层、命名、提交规范 |
| `edumanage/springboot/数据库设计.md` | 24 张表的用途清单 |
| `edumanage/springboot/问题排查记录.md` | 历史问题与解决过程 |
| `edumanage/springboot/CAS服务器搭建方案.md` | CAS 单点登录（Apereo CAS 7.3.x）接入方案 |
| `admin/API_MANUAL.md` | 管理端接口对接手册 |
| `student-teacher/README.md` | 师生端说明、页面清单、**待后端补的缺口清单** |
