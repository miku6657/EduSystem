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

| 页面 | 路由 | 主要接口 | 后端状态 |
| --- | --- | --- | --- |
| 登录 | `/login` | `POST /api/auth/login` | ✅ 已实现 |
| 首页 | `/home` | `GET /api/student\|teacher/by-no/{no}`、`GET /api/term/current` | ✅ 已实现 |
| 我的成绩 | `/student/scores` | `GET /api/score/list-by-student/{studentId}` | ✅ 已实现（已带课程/考试/学分） |
| 我的考勤 | `/student/attendance` | `GET /api/student-attendance/list-by-student` | ✅ 已实现 |
| 补考重修 | `/student/retake` | `GET /api/retake/list-by-student/{id}`、`POST /api/retake/apply?type=`、`GET /api/course/page` | ✅ 已实现（含补考/重修类型） |
| 专升本报名 | `/student/upgrade` | `POST /api/upgrade-apply/apply`、`GET /api/upgrade-apply/my/list` | ✅ 已实现 |
| 毕业资格 | `/student/graduation` | `GET /api/graduate-check/by-student/{studentId}` | ✅ 已实现 |
| 教学日志 | `/teacher/teaching-log` | `POST /api/teaching-log`、`GET /api/teaching-log/list-by-teacher-week` | ✅ 已实现 |
| 我的签到 | `/teacher/check-in` | `POST /api/teacher-attendance/check-in/{teacherId}`、`GET /api/teacher-attendance/list-by-teacher`、`/stat-by-date` | ✅ 已实现（1 次请求取本人 7 天） |
| 学生考勤 | `/teacher/student-attendance` | `POST /api/student-attendance/record`、`GET /api/student-attendance/weekly-report`、`GET /api/student/list-by-class/{id}` | ✅ 已实现 |
| 成绩录入 | `/teacher/scores` | `GET /api/score/page-by-exam`、`POST /api/score/save/{examId}`、`GET /api/score/stat/{examId}`、`GET /api/exam/page` | ✅ 已实现（已带学生姓名） |
| 考核方式申报 | `/teacher/method-apply` | `POST /api/exam-apply/apply`、`GET /api/exam-apply/my/list` | ✅ 已实现 |
| 我的监考 | `/teacher/invigilation` | `GET /api/exam-monitor/list-by-teacher/{teacherId}` | ✅ 已实现（已带考试/考场信息） |

## 后端配套改动（本轮已补齐）

| # | 内容 | 说明 |
| --- | --- | --- |
| 1 | 新增表 `base_teaching_task`（任课关系） | 教师-课程-班级-学期，含实体/Mapper/Service/Controller 与 `schema.sql`、`data.sql` 种子数据 |
| 2 | `GET /api/teacher/by-no/{teacherNo}` | 工号 → 教师，师生端据此解析当前登录人的 `teacherId` |
| 3 | `GET /api/teacher/my-classes`、`/my-courses` | 基于任课表返回教师任教班级/课程 |
| 4 | `GET /api/upgrade-apply/my/list`、`GET /api/exam-apply/my/list` | 师生端"我的报名 / 我的申报" |
| 5 | `POST /api/retake/apply` 支持 `type` | 补考 / 重修均可用（缺省按重修） |
| 6 | 列表补齐展示字段 | `exam_score` 带学生姓名/学号/考试/课程/学分，`exam_monitor` 带考试名/时间/考场/教师名，`exam_retake` 带课程名/考试名，`upgrade_apply` 带学生姓名，`exam_apply` 带课程名/教师名（`@TableField(exist=false)`，Service 批量填充，无 N+1） |
| 7 | `POST /api/auth/logout` | 语义化占位接口，前端统一调用 |
| 8 | `GET /api/teacher-attendance/list-by-teacher` | 本人区间考勤，替代"逐日拉全校再筛本人" |
| 9 | `data.sql` 密码改为 BCrypt 哈希 | 种子账号由"登不进去"变为可直接登录；登录名与学号/工号对齐 |
| 10 | 成绩保存校验收紧 | 分数必须 0~100、学生ID非空、考试必须存在 |

## 仍待后端处理的缺口

| # | 缺口 | 影响页面 | 建议 |
| --- | --- | --- | --- |
| 1 | **成绩录入无归属校验（越权风险）** | 成绩录入 | `GET /api/exam/page` 返回全部考试、保存只按 examId 校验 → 任何教师都能给任意考试录改成绩。任课表已就绪，建议补"我的考试"接口 + 保存时用 `TeachingTaskService.teachesCourse` 校验（**需配合角色鉴权一起做**） |
| 2 | `student_attendance` 记录缺 `classId` | 学生考勤 | 提交时只有 `studentId/courseId/date/status`，而周报表按 `classId` 统计；合班课时两端口径可能不一致 |
| 3 | `weekly-report` 字段契约未固化 | 学生考勤 | 后端返回 `List<Map>`，字段（`normal/late/absent/leave/total/rate`）需固化，否则前端只能继续兜底 |
| 4 | 签到统计口径 | 我的签到 | `stat-by-date` 把"缺勤"计入"未签到"（已同时返回 `unchecked` 字段），与 `list-by-date` 展示口径建议统一 |
| 5 | 教学日志无修改/删除接口 | 教学日志 | 写错只能再提一条；且 `POST /api/teaching-log` 不校验班级/课程是否属于该教师 |
| 6 | **后端无角色权限模型** | 全部 | 任何登录用户可调全部接口（学生 token 能调管理端增删改）。需补 `SimpleGrantedAuthority` + 路由级鉴权，师生端才能安全上线 |
| 7 | **响应契约仍是 `code:200` + `message`** | 全部 | 师生端请求层已双契约兼容，但 `admin` 工程只认 `code:0` + `msg`，建议后端统一 |

## 与后端联调

1. 用**当前 `main` 分支的脚本**建库并导入数据：`schema.sql`（含新表 `base_teaching_task`）→ `data.sql`（密码已是 BCrypt、登录名即学号/工号）。
2. 启动后端（默认 `http://localhost:8080`）。
3. 把 `.env.development` 的 `VITE_USE_MOCK` 改为 `false`，`/api` 请求即经 Vite 代理转发到后端。
4. 用 `2023005001 / 123456`（学生）或 `T001 / 123456`（教师）登录，与后端种子账号一致。

## 验证状态

- 师生端：`vue-tsc -b` 零错误、`vite build` 通过、Mock 读链路 HTTP 冒烟 11/11、写链路 19/19
- 后端：本次改动后 `mvn -DskipTests compile` **BUILD SUCCESS**（146 个源文件）

## 第二批（待排期）

教室申请（申请 / 我的申请 / 取消）、调课申请、课表与教学任务页面 —— 教室申请与调课**后端仍无表无接口**，需先定表结构；课表可基于新的 `base_teaching_task` 落地。
