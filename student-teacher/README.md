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

演示账号（由本地 Mock 提供，密码均为 `123456`）：

| 角色 | 账号 | 说明 |
| --- | --- | --- |
| 学生 | `2023005001` | 王小明（也可用 `student01`） |
| 教师 | `T1001` | 张伟（也可用 `teacher01`） |

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
| 首页 | `/home` | `GET /api/student\|teacher/by-no/{no}`、`GET /api/term/current` | ✅ 学生已实现 / ⚠️ 教师 by-no 缺 |
| 我的成绩 | `/student/scores` | `GET /api/score/list-by-student/{studentId}` | ✅ 已实现 |
| 我的考勤 | `/student/attendance` | `GET /api/student-attendance/list-by-student` | ✅ 已实现 |
| 补考重修 | `/student/retake` | `GET /api/retake/list-by-student/{id}`、`POST /api/retake/apply`、`GET /api/course/page` | ✅ 已实现（补考类型见缺口 7） |
| 专升本报名 | `/student/upgrade` | `POST /api/upgrade-apply/apply`、`GET /api/upgrade-apply/my/list` | ⚠️ 提交已实现，我的报名缺 |
| 毕业资格 | `/student/graduation` | `GET /api/graduate-check/by-student/{studentId}` | ✅ 已实现 |
| 教学日志 | `/teacher/teaching-log` | `POST /api/teaching-log`、`GET /api/teaching-log/list-by-teacher-week` | ✅ 已实现 |
| 我的签到 | `/teacher/check-in` | `POST /api/teacher-attendance/check-in/{teacherId}`、`/list-by-date`、`/stat-by-date` | ✅ 已实现 |
| 学生考勤 | `/teacher/student-attendance` | `POST /api/student-attendance/record`、`GET /api/student-attendance/weekly-report`、`GET /api/student/list-by-class/{id}` | ✅ 已实现 |
| 成绩录入 | `/teacher/scores` | `GET /api/score/page-by-exam`、`POST /api/score/save/{examId}`、`GET /api/score/stat/{examId}`、`GET /api/exam/page` | ✅ 已实现（学生姓名见缺口 6） |
| 考核方式申报 | `/teacher/method-apply` | `POST /api/exam-apply/apply`、`GET /api/exam-apply/my/list` | ⚠️ 提交已实现，我的申报缺 |
| 我的监考 | `/teacher/invigilation` | `GET /api/exam-monitor/list-by-teacher/{teacherId}` | ✅ 已实现（考试信息见缺口 6） |

## 待后端补的缺口清单

> Mock 已按下列形态提供数据，页面可正常演示；接真实后端前需要后端补齐或确认替代方案。

| # | 缺口 | 影响页面 | 建议 |
| --- | --- | --- | --- |
| 1 | `GET /api/teacher/by-no/{teacherNo}` —— 工号 → teacherId | 首页、全部教师页 | 教师登录名是工号，但没有解析接口，导致 teacherId 拿不到，所有教师页无法取数 |
| 2 | **没有"任课/教学任务"表** | 学生"我的课表"、教师"我的教学任务" | 表结构缺失，这两个页面目前**没有排期**，需先定表 |
| 3 | `GET /api/teacher/my-classes`、`GET /api/teacher/my-courses` | 学生考勤、教学日志、考核方式申报 | 现由 Mock 提供；后端应基于任课表返回 |
| 4 | `GET /api/upgrade-apply/my/list?studentId` | 专升本报名 | 后端只有 `/report-list`（管理端报表）与 `/{id}` |
| 5 | `GET /api/exam-apply/my/list?teacherId` | 考核方式申报 | 后端只有 `/export-list` 与 `/{id}` |
| 6 | 成绩/监考列表缺展示字段 | 成绩录入、我的监考 | `exam_score` 无学生姓名、`exam_monitor` 无考试信息，建议后端返回 VO 带 `studentName`/`examName`/`roomName`，否则前端只能显示 ID |
| 7 | 补考申请入口 | 补考重修 | `ExamRetakeService.TYPE_MAKEUP` 定义了却未使用，`applyRetake` 只写"重修"，**补考申请实际不可用** |
| 8 | `POST /api/auth/logout` | 退出登录 | 后端无此接口，师生端退出仅清本地（已容错） |
| 9 | 教师"我的签到记录" | 我的签到 | 后端只有 `list-by-date`（返回全校），前端逐日筛选；若教师多可考虑加 `list-by-teacher` |
| 10 | **成绩录入无归属校验（越权风险）** | 成绩录入 | `GET /api/exam/page` 返回全部考试、`POST /api/score/save/{examId}` 只按 examId 校验 → **任何教师都能给任意考试录入/修改成绩**。建议补"我的考试"接口 + 保存时校验任课关系 |
| 11 | `student_attendance` 记录缺 `classId` | 学生考勤 | 提交时只有 `studentId/courseId/date/status`，而周报表按 `classId` 统计；合班课时两端口径可能不一致，建议记录带 `classId` 或后端校验归属 |
| 12 | `weekly-report` 字段契约未固化 | 学生考勤 | 后端返回 `List<Map>`，字段（`normal/late/absent/leave/total/rate`）目前只在前端 Mock 中约定，需后端固化，否则前端只能继续兜底 |
| 13 | 签到统计口径不一致 | 我的签到 | `stat-by-date` 把"缺勤"记录计入"未签到"，而 `list-by-date` 会展示该状态；建议统一（前端已用按钮文案"今日已记录缺勤"做区分） |
| 14 | 教学日志无修改/删除接口 | 教学日志 | 写错只能再提一条；且 `POST /api/teaching-log` 不校验班级/课程是否属于该教师 |

## 与后端联调

1. 启动后端（默认 `http://localhost:8080`，数据库 `eduSYSTEM` 需先手工导入 `edumanage/springboot/schema.sql` 与 `data.sql`）。
2. 把 `.env.development` 的 `VITE_USE_MOCK` 改为 `false`，`/api` 请求即经 Vite 代理转发到后端。
3. **注意两个前置问题**（已在审计中确认，未修）：
   - `data.sql` 里 `sys_user.password` 是明文 `123456`，而 `AuthController` 用 BCrypt 校验 → **种子账号登录会失败**，需先把密码换成 BCrypt 哈希；
   - 后端无角色权限模型（任何登录用户可调全部接口）→ 师生端上线前必须补，否则学生 token 能调管理端接口。

## 验证状态

- `vite build`：✅ 通过
- `vue-tsc -b`（含 `noUnusedLocals` / `noUnusedParameters` / `erasableSyntaxOnly` 严格项）：✅ 无错误

## 第二批（待排期）

教室申请（申请 / 我的申请 / 取消）、调课申请、课表与教学任务、首页待办 —— 均需后端先补表或补接口，详见上文缺口清单。
