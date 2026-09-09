# 教学过程管理系统 · 接口对接手册（API Manual）

> 用途：给**后端同学**的接口约定文档。前端所有请求以 `/api` 为统一前缀，本地开发由 `src/mock/index.ts` 提供模拟数据（`npm run dev` 时生效）；需要联调真实后端时，将 `.env.development` 中 `VITE_USE_MOCK` 改为 `false`，请求会通过 Vite 代理转发到 `http://localhost:8080`。
>
> 角色约定：`admin`（系统管理员）/ `manager`（教务处长），登录返回的角色决定后台侧边栏菜单。下文标注“管理端”表示由本仓库（后台管理端）调用，“师生端”表示由学生/教师端调用，“公共”表示两端共用。

## 0. 通用约定

- **请求前缀**：`/api`（如 `/api/auth/login`），请求头带 `Authorization: Bearer <token>`。
- **统一响应结构**：

  ```json
  { "code": 0, "data": {}, "msg": "success" }
  ```

  `code === 0` 表示成功；非 0 时前端弹出 `msg` 并中断业务。
- **分页结构**（`data` 字段）：

  ```json
  { "list": [], "total": 0, "page": 1, "pageSize": 10 }
  ```

- **删除**：`POST /api/xxx/delete`，请求体传主键，如 `{ "id": 1001 }`。
- 本文档中标注 `（已实现-Mock）` 的接口均可在本地 mock 中直接调用；`（规划中）` 表示前端已预留交互位，接口形态建议如下，待后端按约定提供。

---

## 1. 认证与公共

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/auth/login` | POST | 公共 | 登录 | 入参 `{ username, password }`；成功返回 `{ token, roles, name }`。mock 固定返回 `roles: ['admin']`、`name: '管理员'` |
| `/api/auth/userinfo` | GET | 公共 | 获取当前登录用户信息 | 返回 `{ id, username, name, roles }`，角色用于侧边栏权限 |
| `/api/auth/roles` | GET | 公共（预留） | 动态获取当前用户角色 | 建议返回 `["admin"]` 或 `["manager"]`；当前 mock 固定 `["admin"]` |
| `/api/auth/logout` | POST | 公共 | 退出登录 | 返回 `data: null` |
| `/api/term/current` | GET | 公共 | 获取当前学期 | 返回字符串，如 `"2026-2027学年第二学期"` |

## 2. 后台首页工作台（管理端）

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/dashboard/statistics` | GET | 管理端 | 后台首页 4 个统计卡片 + 待办审批表 | `data` 至少包含 `courseCount`（总课程）、`classroomCount`（总教室）、`todayAdjustCount`（今日调课申请）、`pendingGraduationCount`（待审核毕业人数）、`pendingApprovals`（最近 5 条待办审批：`{ id, type, title, applicant, applyTime, status }`） |

> 该接口同时被 H5 示例页复用展示数值，历史字段（`pendingAdjust` / `pendingClassroom` / `classroomUsageRate` / `notices` / `upcomingExams`）可保留。

## 3. 审批待办（管理端）

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/todo/list` | GET | 管理端 | 顶部“待办提醒”铃铛（调课审批 + 教室申请审批） | 返回数组：`{ id, type, title, applicant, applyTime, route }`；`type` 取值 `course-adjust` / `classroom`；`route` 为前端跳转的审核列表页（mock 固定） |

## 4. 基础数据（管理端）

### 4.1 课程管理（管理端，增删改查）

| 接口 | 方法 | 用途 | 说明 |
| --- | --- | --- | --- |
| `/api/course/list` | GET | 课程分页查询 | 支持 `name` 模糊、`term` 精确筛选 |
| `/api/course/add` | POST | 新增课程 | 入参 `{ name, term, teacher, credit, startDate }` |
| `/api/course/edit` | POST | 编辑课程 | 额外携带 `{ id }` |
| `/api/course/delete` | POST | 删除课程 | `{ id }` |

### 4.2 班级管理（管理端，增删改查）

| 接口 | 方法 | 用途 | 说明 |
| --- | --- | --- | --- |
| `/api/class/list` | GET | 班级分页查询 | 支持 `name` 模糊、`grade` 精确筛选 |
| `/api/class/add` | POST | 新增班级 | 入参 `{ name, grade, major, headTeacher, studentCount, enrollDate }` |
| `/api/class/edit` | POST | 编辑班级 | 额外携带 `{ id }` |
| `/api/class/delete` | POST | 删除班级 | `{ id }` |

## 5. 教材管理（管理端）

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/textbook/stock-in/list` | GET | 管理端 | 新书入库分页查询 | 支持 `name` 模糊筛选 |
| `/api/textbook/stock-in/delete` | POST | 管理端 | 删除入库记录 | `{ id }` |
| `/api/textbook/stock-in/add` | POST | 管理端（规划中） | 新书入库新增 | 页面“新增”按钮已就位，接口待后端提供 |
| `/api/textbook/stock-in/edit` | POST | 管理端（规划中） | 入库记录编辑 | 同上 |

## 6. 毕业审核 · 毕业生管理（管理端）

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/graduation/list` | GET | 管理端 | 毕业生列表 | 支持 `name` 模糊、`studentNo` 模糊、`status`（待审核/通过/未通过）筛选；Mock 造 10 条（含 ≥3 条待审核） |
| `/api/graduation/approve/{id}` | PUT | 管理端 | 毕业审核：通过 | 仅“待审核”可操作；成功返回 `{ code:0, msg:'审核通过' }`，状态变为“通过” |
| `/api/graduation/reject/{id}` | PUT | 管理端 | 毕业审核：驳回 | 仅“待审核”可操作；成功返回 `{ code:0, msg:'已驳回' }`，状态变为“未通过” |

说明：页面 `src/views/graduation/student-list.vue` 已改为独立审核页（含多选 + 批量通过，批量在 Mock 中由前端并发调用 approve 实现，后端可后续提供批量接口优化）。

## 7. 调课管理（师生端 + 管理端）

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/course-adjust/audit/list` | GET | **管理端** | 调课审批列表 | 支持 `courseName`/`teacher` 模糊、`status`（待审核/已通过/已驳回）筛选；侧边栏“调课审批”页 |
| `/api/course-adjust/approve/{id}` | PUT | **管理端** | 调课审批：通过 | 仅“待审核”可操作；成功后状态变为“已通过” |
| `/api/course-adjust/reject/{id}` | PUT | **管理端** | 调课审批：驳回 | 仅“待审核”可操作；成功后状态变为“已驳回” |
| `/api/course-adjust/my/list` | GET | **师生端** | 教师查看“我的调课”分页列表 | 支持 `courseName` 模糊、`status` 筛选；师生端同学使用 |
| `/api/course-adjust/my/delete` | POST | **师生端** | 删除自己的调课申请 | `{ id }`（仅未审核可删，后端需校验） |
| `/api/course-adjust/apply` | POST | **师生端**（规划中） | 提交调课申请 | 入口由师生端实现 |

## 8. 教室申请 / 教室审批（师生端发起 · 管理端审批）

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/classroom/list` | GET | 师生端 + 管理端 | 教室资源列表 | 师生端申请弹窗下拉数据；管理端日历占用总览按此教室集合生成 |
| `/api/classroom/occupancy` | GET | 管理端 | 全校教室某日期范围的占用总览（只读） | 参数 `start/end`（YYYY-MM-DD）；返回按天事件，`status`：`exam/class`（教学/考试占用·红）、`pending`（待审核申请·黄）、`approved`（已通过占用·红）；已驳回/已取消不占用 |
| `/api/classroom/apply` | POST | **师生端** | 提交教室使用申请 | body `{ roomId, date, timeSlot, purpose, reason, applicant, className }`；提交后状态为“待审核”，进入管理端审批队列（管理端代码不调用此接口） |
| `/api/classroom-apply/my/list` | GET | **师生端** | “我的申请”分页查询 | 支持 `status`（待审核/已通过/已驳回/已取消）筛选 |
| `/api/classroom-apply/cancel` | POST | **师生端** | 取消申请 | `{ id }`；仅“待审核”可取消，**后端需校验：提交后 30 分钟内不可取消** |
| `/api/classroom/approval/list` | GET | **管理端** | 教室申请审批列表 | 支持 `status`（待审核/已通过/已驳回）筛选 |
| `/api/classroom/approve/{id}` | PUT | **管理端** | 通过教室申请 | 仅“待审核”状态可操作；已通过申请占用对应教室时段 |
| `/api/classroom/reject/{id}` | PUT | **管理端** | 驳回教室申请 | 仅“待审核”状态可操作；驳回后恢复空闲，不再计入日历占用 |

说明（角色分工）：**管理端只做审批**——`src/views/classroom-apply/index.vue` 上半区为全校教室占用总览（FullCalendar 只读，点日期弹只读详情），下半区为待审批列表（通过/驳回后日历实时变色）；师生端发起申请/我的申请/取消页面由学生端同学基于同一套 `/api/classroom/*` 接口实现（本仓库 `src/views/classroom-apply/mobile.vue` 仅为临时联调演示，正式以师生端工程为准）。

## 9. 专升本报名审核（管理端）

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/college-upgrade/audit/list` | GET | **管理端** | 报名审核列表 | 支持 `studentName` 模糊、`status`（待审核/已通过/已驳回）筛选 |
| `/api/college-upgrade/approve/{id}` | PUT | **管理端** | 报名审核：通过 | 仅“待审核”可操作；成功后状态变为“已通过” |
| `/api/college-upgrade/reject/{id}` | PUT | **管理端** | 报名审核：驳回 | 仅“待审核”可操作；成功后状态变为“已驳回” |
| `/api/college-upgrade/apply/list` | GET | 管理端（历史保留） | 报名记录分页查询 | 兼容旧页使用，新审核页不再调用 |
| `/api/college-upgrade/apply/delete` | POST | 管理端（历史保留） | 删除报名记录 | `{ id }` |

## 10. 教师考勤日志（管理端）

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/attendance/log/list` | GET | 管理端 | 教师考勤日志分页查询 | 支持 `teacherName` 模糊、`checkStatus`（正常/迟到/缺勤）筛选 |
| `/api/attendance/log/delete` | POST | 管理端 | 删除考勤日志 | `{ id }` |

## 11. 考务管理 · 自动排考 + 考核方式申报（管理端）

| 接口 | 方法 | 归属 | 用途 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/exam/auto-arrange` | POST | 管理端 | 一键自动排考 | body `{ term, grade }`；返回 `{ term, grade, summary, items, generatedAt }` |
| `/api/exam/method-audit/list` | GET | **管理端** | 考核方式申报审核列表 | 支持 `courseName`/`teacher` 模糊、`status` 筛选 |
| `/api/exam/method-audit/approve/{id}` | PUT | **管理端** | 考核方式申报：通过 | 仅“待审核”可操作；成功后状态变为“已通过” |
| `/api/exam/method-audit/reject/{id}` | PUT | **管理端** | 考核方式申报：驳回 | 仅“待审核”可操作；成功后状态变为“已驳回” |

`items[].conflict` 命中两类冲突：监考员同一场次被分配到两个考场、教室同一场次被两门考试同时占用；`items[].conflictReason` 为冲突说明。页面（`src/views/exam/arrange.vue`）以红色 `el-tag` + 浅红整行高亮冲突行，并可用 `xlsx` 导出 Excel。规划中：保存排考方案 `/api/exam/arrange/save`、查询历史 `/api/exam/arrange/list`（供管理端后续迭代）。

说明：考务管理菜单下含“自动排考”（`/exam/arrange`）与“考核方式申报”（`/exam/method-audit`，审核页 `src/views/exam/method-audit.vue`）。考核方式申报内容由师生端提交（规划中，当前仅管理端审核 Mock）。

## 12. 权限对照（管理端侧边栏）

- `admin`（系统管理员）：全部 9 个一级菜单；
- `manager`（教务处长）：教学基础数据、教室审批、调课管理、毕业审核；
- 菜单可见性由 `src/router/modules/*` 中路由 `meta.roles` 配置，前端根据 `/api/auth/userinfo`（或 `/api/auth/roles`）返回的 `roles` 动态渲染。

> Mock 说明：本地数据量有限（课程/班级/毕业生等每类 5-10 条），且 dev 重启后自动重置；角色 mock 固定为 `admin`，需要验证 `manager` 视图可临时把 `/api/auth/userinfo`、`/api/auth/roles` 的返回改为 `["manager"]`。
