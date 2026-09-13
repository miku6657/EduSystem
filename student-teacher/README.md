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

## 目录结构

```
src
├── api/            按后端模块划分的接口层（auth / score / attendance / teacherAttendance /
│                   teachingLog / retake / upgrade / graduation / examApply / examMonitor /
│                   exam / base / profile / term）
├── components/     （暂无；Vant 组件按需自动导入）
├── composables/    useAsyncData —— 统一 loading / error / reload
├── constants/      dict.ts —— 状态字典（与后端枚举值一一对应）
├── mock/           index.ts —— 本地 Mock，路径与后端 Controller 完全一致
├── router/         index.ts（守卫含**路由级角色校验**）+ permission.ts + modules/
├── stores/         user（登录态 + 业务身份 + 当前学期）
├── styles/         全局样式（.st-page / .st-card 等）
├── types/          api / user / router meta
├── utils/          request（**双契约兼容**）/ format / role / storage
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

1. 用 `main` 分支的脚本建库：`schema.sql`（含 `base_teaching_task`、`classroom_apply`）→ `data.sql`（密码为 BCrypt、登录名即学号/工号、含教室申请种子数据）。
2. 启动后端（默认 `http://localhost:8080`）。
3. 把 `.env.development` 的 `VITE_USE_MOCK` 改为 `false`，`/api` 请求即经 Vite 代理转发到后端。
4. 用 `2023005001 / 123456`（学生）或 `T001 / 123456`（教师）登录，与后端种子账号一致。
5. ⚠️ main 已把 base 模块改为复数 RESTful 路径，前端 `src/api/*` 已同步；若后端再调整路径，改 `src/api` + `src/mock` 两处即可。

## 验证状态

- 师生端：`vue-tsc -b` 零错误、`vite build` 通过；Mock 端到端冒烟 **19/19**（含新 RESTful 路径、教室申请提交/冲突拦截/撤回、学生与教师"我的申请"相互隔离）
- 后端：`mvn -DskipTests compile` **BUILD SUCCESS**

## 下一步

业务页面已全部落地（学生 5 + 教师 7 + 共用 2）。剩下的是纯加分项与工程化收尾：

1. **教师端成绩导出 Excel**（需给师生端加 `xlsx` 依赖）与 **班级花名册**；
2. **首页待办**：用现有接口（今日未签到 / 本周日志是否提交 / 近期待监考 / 待审核报名等）在前端拼装，无需新接口；
3. **工程化**：抽公共组件（加载/错误/空三态、统计条），配 ESLint/Prettier；
4. **联调收尾**：关掉 mock（`VITE_USE_MOCK=false`）与真实后端跑一轮端到端，把字段差异当场修掉。
