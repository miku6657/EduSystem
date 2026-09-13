# 教学过程管理系统 · 师生端（student-teacher）

学生 / 教师使用的移动端 H5 前端工程，与后台管理端（`../admin`）**同源不同端**：管理端做审批与基础数据维护，师生端做申请、填报与查询。

- 技术栈：Vue 3.5 + Vite 8 + TypeScript 6 + Vant 4 + Pinia + vue-router + axios
- 形态：移动优先 H5（页面主体限宽 640px 居中，桌面浏览器同样可用）
- 本地 Mock：`vite-plugin-mock`，默认开启

## 快速开始

```bash
npm install
npm run dev        # http://localhost:5174
```

演示账号（本地 Mock 与后端 `data.sql` 种子数据一致，密码均为 `123456`）：

| 角色 | 账号 | 说明 |
| --- | --- | --- |
| 学生 | `2023005001` | 王小明（登录名即学号，也可用 `student01`） |
| 教师 | `T001` | 张伟（登录名即工号，也可用 `teacher01`） |

登录名与 `base_student.student_no` / `base_teacher.teacher_no` 一致，师生端才能用登录名解析出 `studentId` / `teacherId`。
`admin` 账号会被拒绝并提示使用后台管理端登录。

常用命令：

| 命令 | 说明 |
| --- | --- |
| `npm run dev` | 启动开发服务器（mock 默认开启） |
| `npm run type-check` | `vue-tsc` 类型检查 |
| `npm run build` | 类型检查 + 生产构建 |
| `npm run preview` | 预览构建产物 |
| `npm run lint` | ESLint 检查并自动修复（`lint:check` 只检查不修复） |
| `npm run format` | Prettier 统一格式 |

## 目录结构

```
src
├── api/            按后端模块划分的接口层（auth / score / attendance / teacherAttendance /
│                   teachingLog / retake / upgrade / graduation / examApply / examMonitor /
│                   exam / base / profile / term）
├── components/     PageState（加载/失败/空三态）、StatBar（统计条）
├── composables/    useAsyncData —— 统一 loading / error / reload
├── constants/      dict.ts —— 状态字典（与后端枚举值一一对应）
├── mock/           index.ts —— 本地 Mock，路径与后端 Controller 完全一致
├── router/         index.ts（守卫含**路由级角色校验**）+ permission.ts + modules/
├── stores/         user（登录态 + 业务身份 + 当前学期）
├── styles/         全局样式（.st-page / .st-card 等）
├── types/          api / user / router meta
├── utils/          request（**双契约兼容**）/ format / role / storage / excel（导出 xlsx）
└── views/
    ├── login/          登录
    ├── layout/         Layout（顶栏 + 底部 tabbar）
    ├── home/           首页（个人卡 + 功能宫格 + 退出登录）
    ├── student/        我的成绩 / 我的考勤 / 补考重修 / 专升本报名 / 毕业资格
    ├── teacher/        教学日志 / 我的签到 / 学生考勤 / 成绩录入 / 考核方式申报 / 我的监考
    └── error/          404
```

## 接口契约：本工程比 admin 多做了两件事

### 1. 响应体双契约兼容（`src/utils/request.ts`）

后台管理端直接按 `{ code: 0, data, msg }` 判定，而后端实际返回 `{ code: 200, message, data }`，导致"关掉 mock 就全红"。师生端把两种写法都吃下：

- 成功码：`0` 与 `200` 都算成功
- 消息字段：优先 `msg`，回退 `message`
- 分页体：`{ list, total, page, pageSize }` 与 MyBatis-Plus 的 `{ records, total, current, size }` 都能归一化成 `PageResult`
- 分页入参：同时下发 `page` 与 `pageNo`，后端读哪个都能取到
- `GET /api/term/current`：后端返回 `Term` 实体、手册约定返回字符串，两者都能用

页面代码因此完全不感知后端契约细节。

### 2. 路由级角色校验（`src/router/index.ts` + `permission.ts`）

`meta.roles` 不只是用来隐藏菜单：手输 URL 访问不属于自己角色的页面会被守卫拦回首页并提示。学生看不到也进不去教师页，反之亦然。

## 页面清单与接口对照

> 接口路径以 **`main` 分支的 RESTful 风格**为准（base 模块已由 `/api/xxx/page`、`/api/xxx/list-by-*` 改为复数资源 + query 过滤）。

| 页面 | 路由 | 主要接口 | 后端状态 |
| --- | --- | --- | --- |
| 登录 | `/login` | `POST /api/auth/login`、`POST /api/auth/logout` | ✅ 已实现 |
| 首页 | `/home` | `GET /api/students?studentNo=`、`GET /api/teachers?teacherNo=`、`GET /api/terms` | ✅ 已实现 |
| **教室申请** | `/classroom-apply` | `GET /api/classroom-applies/my`、`POST /api/classroom-applies`、`PUT /api/classroom-applies/{id}/cancel`、`GET /api/classrooms` | ✅ 已实现（学生/教师共用；撤回接口本轮补） |
| **课表 / 教学任务** | `/timetable` | `GET /api/teaching-tasks?classId=`（学生）、`?teacherId=`（教师） | ✅ 已实现（学生/教师共用，按星期分组；未排课的单独归组） |
| **我的调课** | `/teacher/course-adjust` | `GET /api/course-adjusts/my`、`POST /api/course-adjusts`、`PUT /api/course-adjusts/{id}/cancel` | ✅ 已实现（教师端；审批列表/通过/驳回归管理端） |
| 我的成绩 | `/student/scores` | `GET /api/score/list-by-student/{studentId}` | ✅ 已实现（已带课程/考试/学分） |
| 我的考勤 | `/student/attendance` | `GET /api/student-attendance/list-by-student` | ✅ 已实现 |
| 补考重修 | `/student/retake` | `GET /api/retake/list-by-student/{id}`、`POST /api/retake/apply?type=`、`GET /api/courses` | ✅ 已实现（含补考/重修类型） |
| 专升本报名 | `/student/upgrade` | `POST /api/upgrade-apply/apply`、`GET /api/upgrade-apply/my/list` | ✅ 已实现 |
| 毕业资格 | `/student/graduation` | `GET /api/graduate-check/by-student/{studentId}` | ✅ 已实现 |
| 教学日志 | `/teacher/teaching-log` | `POST /api/teaching-log`、`GET /api/teaching-log/list-by-teacher-week`、`GET /api/teachers/{id}/courses` | ✅ 已实现 |
| 我的签到 | `/teacher/check-in` | `POST /api/teacher-attendance/check-in/{teacherId}`、`GET /api/teacher-attendance/list-by-teacher`、`/stat-by-date` | ✅ 已实现（1 次请求取本人 7 天） |
| 学生考勤 | `/teacher/student-attendance` | `POST /api/student-attendance/record`、`GET /api/student-attendance/weekly-report`、`GET /api/students?classId=` | ✅ 已实现 |
| 成绩录入 | `/teacher/scores` | `GET /api/score/page-by-exam`、`POST /api/score/save/{examId}`、`GET /api/score/stat/{examId}`、`GET /api/exams` | ✅ 已实现（已带学生姓名） |
| 考核方式申报 | `/teacher/method-apply` | `POST /api/exam-apply/apply`、`GET /api/exam-apply/my/list`、`GET /api/teachers/{id}/courses` | ✅ 已实现 |
| 我的监考 | `/teacher/invigilation` | `GET /api/exam-monitor/list-by-teacher/{teacherId}` | ✅ 已实现（已带考试/考场信息） |
| **班级花名册** | `/teacher/roster` | `GET /api/teachers/{id}/classes`、`GET /api/students?classId=` | ✅ 已实现（含关键字搜索与 **Excel 导出**） |
| 我的待办 | `/home` 顶部 | `list-by-teacher`（教师）/ `classroom-applies/my`、`retake`、`upgrade`（学生） | ✅ 已实现（前端用现有接口拼装，无需后端待办接口） |

## 后端配套改动

| # | 内容 | 说明 |
| --- | --- | --- |
| 1 | 新增表 `base_teaching_task`（任课关系） | 教师-课程-班级-学期；含 entity/mapper/service/controller 与 `schema.sql`、`data.sql` 种子数据 |
| 2 | `GET /api/teachers?teacherNo=` | 工号 → 教师，师生端据此解析当前登录人的 `teacherId`（按 main 的 RESTful 风格） |
| 3 | `GET /api/teachers/{id}/classes`、`/{id}/courses` | 基于任课表返回教师任教班级/课程 |
| 4 | `GET /api/upgrade-apply/my/list`、`GET /api/exam-apply/my/list` | 师生端"我的报名 / 我的申报" |
| 5 | `POST /api/retake/apply` 支持 `type` | 补考 / 重修均可用（缺省按重修） |
| 6 | 列表补齐展示字段 | `exam_score` 带学生姓名/学号/考试/课程/学分，`exam_monitor` 带考试名/时间/考场/教师名，`exam_retake` 带课程名/考试名，`upgrade_apply` 带学生姓名，`exam_apply` 带课程名/教师名（`@TableField(exist=false)` + Service 批量填充，无 N+1） |
| 7 | `GET /api/teacher-attendance/list-by-teacher` | 本人区间考勤，替代"逐日拉全校再筛本人" |
| 8 | `data.sql` 密码改为 BCrypt 哈希 | 种子账号由"登不进去"变为可直接登录；登录名与学号/工号对齐 |
| 9 | 成绩保存校验收紧 | 分数必须 0~100、学生ID非空、考试必须存在 |
| 10 | **补齐 `classroom_apply` 建表脚本** | 队友的教室申请代码齐全，但 `schema.sql` 里漏了这张表（一跑就报"表不存在"）；已补建表 + 种子数据 + `数据库设计.md` 登记 |
| 11 | 教室申请接口修正 | ① 申请人改为**以当前登录人为准**（原来信任请求体，可冒名提交，且 `applicant` 为 NOT NULL 会插入失败）；② `PUT /{id}/approve` 补 `@PreAuthorize("hasRole('ADMIN')")`（原来任何登录用户都能审批）；③ approve/reject 增加"仅待审核可审批"校验（原来可重复审批）；④ `GET /api/classroom-applies?status=` 原来忽略 status 参数，已生效；⑤ 新增 `PUT /{id}/cancel` 撤回（仅本人、仅待审核） |
| 12 | **新增表 `course_adjust`（调课申请）** | 教师提交「原时间 → 调整后时间」的调课申请，管理端审批；含 entity/mapper/service/controller、`schema.sql`、`data.sql` 种子数据与 `数据库设计.md` 登记 |
| 13 | 调课申请接口 `/api/course-adjusts` | `POST` 提交（teacherId 由服务端按登录人解析，非教师拒绝）、`GET /my` 我的调课、`GET ?status=` 审批列表（`@PreAuthorize` ADMIN）、`PUT /{id}/approve|reject`（ADMIN + 仅待审核）、`PUT /{id}/cancel`（仅本人、仅待审核） |
| 14 | **`base_teaching_task` 增加上课时间字段** | `weekday` / `start_section` / `end_section` / `classroom_id` / `weeks`，并给 `TeachingTaskController` 增加 `?classId=` 查询；返回体带课程/班级/教师/教室名称，课表页可直接渲染 |

## 仍待后端处理的缺口

| # | 缺口 | 影响页面 | 建议 |
| --- | --- | --- | --- |
| 1 | **写接口鉴权只做了一半** | 全部 | 机制已具备（`ROLE_*` authorities + `@EnableMethodSecurity`），教室申请/调课已加 `@PreAuthorize`，但成绩保存、教学日志、考勤录入等写接口仍对任何登录用户开放 |
| 2 | **成绩录入无归属校验** | 成绩录入 | `GET /api/exams` 返回全部考试、保存只按 examId 校验 → 教师可给非本人任教的考试录改成绩。任课表已就绪，可用 `TeachingTaskService.teachesCourse` 校验 |
| 3 | **调课缺少冲突校验** | 我的调课 | 提交时未校验"调整后时间该教师/班级/教室是否已有安排"（需要一张全校课表视图才能判），目前只做原时间≠新时间与必填校验 |
| 4 | `student_attendance` 记录缺 `classId` | 学生考勤 | 周报表按 `classId` 统计，而录入时只有 `studentId/courseId/date/status`；合班课时口径可能不一致 |
| 5 | `weekly-report` 字段契约未固化 | 学生考勤 | 后端返回 `List<Map>`，字段（`normal/late/absent/leave/total/rate`）需固化 |
| 6 | 签到统计口径 | 我的签到 | `stat-by-date` 把"缺勤"计入"未签到"（已同时返回 `unchecked`），与 `list-by-date` 展示口径建议统一 |
| 7 | 教学日志无修改/删除接口 | 教学日志 | 写错只能再提一条；且不校验班级/课程是否属于该教师 |
| 8 | 响应契约仍是 `code:200` + `message` | 全部 | 师生端请求层已双契约兼容；`admin` 也已自行适配，后端统一会更干净 |
| 9 | 管理端页面未跟上 | 管理端 | 教室申请审批、调课审批的**后端接口已就绪**（含 `@PreAuthorize` ADMIN），但 `admin` 工程对应页面仍走 mock，需要管理端同学切换到真实接口 |

## 与后端联调

1. **同步数据库（一条命令）**：`cd edumanage/springboot` → 执行 `sync-db.cmd`（Windows）或 `./sync-db.sh`。
   该脚本会跑 `schema.sql`（27 张表，可重复执行）+ `data.sql`（演示数据，`INSERT IGNORE`，并把 seed 账号密码统一更新为 BCrypt）。
   > ⚠️ 手工导入时必须带 `--default-character-set=utf8mb4`：Windows 上 mysql 客户端默认 `character_set_client=gbk`，
   > 不加会把 UTF-8 的 SQL 按 GBK 解释，中文乱码、甚至因 `Invalid default value` 直接中断建表（脚本已内置该参数）。
2. 启动后端（默认 `http://localhost:8080`）。
3. 把 `.env.development` 的 `VITE_USE_MOCK` 改为 `false`，`/api` 请求即经 Vite 代理转发到后端。
4. 用 `2023005001 / 123456`（学生）或 `T001 / 123456`（教师）登录，与后端种子账号一致。
5. ⚠️ main 已把 base 模块改为复数 RESTful 路径，前端 `src/api/*` 已同步；若后端再调整路径，改 `src/api` + `src/mock` 两处即可。

## 验证状态

- 师生端：`vue-tsc -b` 零错误、`vite build` 通过；Mock 端到端冒烟 **19/19**（含新 RESTful 路径、教室申请提交/冲突拦截/撤回、学生与教师"我的申请"相互隔离）
- 后端：`mvn -DskipTests compile` **BUILD SUCCESS**
- **真实端到端联调（2026-09-13 实测）**：用临时 MySQL 实例 + 真实后端（连临时库）跑通
  - 学生 `2023005001/123456`、教师 `T001/123456`、管理员 `admin/123456` 三者登录成功（BCrypt 生效）
  - 错误密码被拒、无 token 访问受保护接口返回 401、学生越权审批返回 403
  - 学生课表 / 成绩 / 教室申请（提交后 1→2 条，真实写库）/ 教师教学任务 / 监考（多考场用顿号合并）/ 我的调课（提交后 1→2 条）全部正常
  - 过程中的三个真 bug 已修：`schema.sql` 因中文默认值 + 客户端 GBK 导致建表中断、`sync-db.cmd` 被 cmd 按 GBK 解析导致整行被当命令、`@PreAuthorize` 拒绝被全局异常兜底成 500（现为 403）
- **工程化（2026-09-13 补充）**：
  - `npm run lint`（ESLint 9 扁平配置）实际检查 **65 个文件（含 22 个 .vue）0 问题**；
  - `npm run format`（Prettier）全量统一格式后 `--check` 全部合规；
  - 成绩导出 Excel 使用 `xlsx`，构建时**自动分包**（`excel-*.js` 约 275KB，只在用到导出的页面加载，主包仍 182KB）。

## 下一步

**师生端业务页面与工程化收尾已全部完成**：19 个页面（学生端 5 + 教师端 8 + 共用 2 + 登录/首页/Layout/404）、
首页待办、成绩导出 Excel、班级花名册、公共三态组件、ESLint + Prettier、真实端到端联调。

后续可选（按优先级）：
1. 把历史页面逐步迁移到 `PageState` / `StatBar`（新页面已在用，旧页面目前各自实现三态）；
2. 管理端（`admin`）把教室申请审批、调课审批从 mock 切到真实接口（后端已就绪）；
3. 后端侧遗留：成绩保存的任课归属校验、教学日志/考勤录入的角色鉴权、响应契约统一（详见上文缺口清单）。
