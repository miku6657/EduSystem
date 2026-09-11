/**
 * 师生端本地 Mock（vite-plugin-mock v3）
 *
 * 约定：
 * - 所有接口路径与 **后端 Controller 实际路径一致**（不是 API_MANUAL 里"规划中"的路径），
 *   这样关掉 mock 直连后端时只需要改 .env.development 的 VITE_USE_MOCK。
 * - 响应体用前端原生约定 { code: 0, data, msg }；后端返回 { code: 200, message, data }
 *   也能被 utils/request.ts 兼容。
 * - 未配置的接口一律返回失败，避免"URL 写错却静默成功"（admin 工程踩过的坑）。
 *
 * 演示账号：学生 2023005001 / 123456（王小明）；教师 T1001 / 123456（张伟）
 */
import type { MockMethod } from 'vite-plugin-mock'

type MockRow = Record<string, any>

interface MockContext {
  url: string
  body: MockRow
  query: MockRow
  headers: MockRow
}

/* ------------------------------ 通用工具 ------------------------------ */

function ok(data: unknown, msg = 'success') {
  return { code: 0, data, msg }
}

function fail(msg: string) {
  return { code: 1, data: null, msg }
}

function pad2(n: number): string {
  return n < 10 ? `0${n}` : String(n)
}

function toDateStr(date: Date): string {
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`
}

/** 相对今天偏移若干天的日期字符串，保证演示数据永远"新鲜" */
function dayOffset(days: number, base?: Date): string {
  const date = base ? new Date(base.getTime()) : new Date()
  date.setDate(date.getDate() + days)
  return toDateStr(date)
}

function parseDate(dateStr: string): Date {
  const [y, m, d] = dateStr.split('-').map((item) => Number(item))
  return new Date(y, (m ?? 1) - 1, d ?? 1)
}

/** 某天所在周（周一~周日） */
function weekRangeOf(dateStr: string): { start: string; end: string } {
  const date = parseDate(dateStr)
  const day = date.getDay()
  const offset = day === 0 ? -6 : 1 - day
  return { start: dayOffset(offset, date), end: dayOffset(offset + 6, date) }
}

function nowText(): string {
  const d = new Date()
  return `${toDateStr(d)} ${pad2(d.getHours())}:${pad2(d.getMinutes())}`
}

function between(value: string, start: string, end: string): boolean {
  return value >= start && value <= end
}

function paged<T>(list: T[], query: MockRow) {
  const page = Number(query.page ?? query.pageNo ?? 1)
  const pageSize = Number(query.pageSize ?? 10)
  const start = (page - 1) * pageSize
  return { list: list.slice(start, start + pageSize), total: list.length, page, pageSize }
}

/* ------------------------------ 演示数据 ------------------------------ */

const classes: MockRow[] = [
  { id: 1, name: '计算机2301班', grade: '2023级', majorId: 1, headTeacherId: 1, studentCount: 4 },
  { id: 2, name: '软件工程2302班', grade: '2023级', majorId: 2, headTeacherId: 2, studentCount: 2 },
]

const students: MockRow[] = [
  { id: 1, studentNo: '2023005001', name: '王小明', gender: '男', classId: 1, className: '计算机2301班', majorName: '计算机科学与技术', phone: '13900000001', status: '在读' },
  { id: 2, studentNo: '2023005002', name: '陈红', gender: '女', classId: 1, className: '计算机2301班', majorName: '计算机科学与技术', phone: '13900000002', status: '在读' },
  { id: 3, studentNo: '2023005003', name: '刘洋', gender: '男', classId: 1, className: '计算机2301班', majorName: '计算机科学与技术', phone: '13900000003', status: '在读' },
  { id: 4, studentNo: '2023005004', name: '张雪', gender: '女', classId: 1, className: '计算机2301班', majorName: '计算机科学与技术', phone: '13900000004', status: '在读' },
  { id: 5, studentNo: '2023005005', name: '孙磊', gender: '男', classId: 2, className: '软件工程2302班', majorName: '软件工程', phone: '13900000005', status: '在读' },
  { id: 6, studentNo: '2023005006', name: '周婷', gender: '女', classId: 2, className: '软件工程2302班', majorName: '软件工程', phone: '13900000006', status: '在读' },
]

const teachers: MockRow[] = [
  { id: 1, teacherNo: 'T1001', name: '张伟', gender: '男', departmentId: 1, departmentName: '计算机系', title: '副教授', teacherType: '专职', status: '在职' },
  { id: 2, teacherNo: 'T1002', name: '王丽华', gender: '女', departmentId: 1, departmentName: '计算机系', title: '讲师', teacherType: '专职', status: '在职' },
  { id: 3, teacherNo: 'T1003', name: '李思远', gender: '男', departmentId: 2, departmentName: '软件工程系', title: '教授', teacherType: '专职', status: '在职' },
  { id: 4, teacherNo: 'T1004', name: '赵明', gender: '男', departmentId: 2, departmentName: '软件工程系', title: '助教', teacherType: '兼职', status: '在职' },
]

const courses: MockRow[] = [
  { id: 1, courseCode: 'C001', name: '高等数学（上）', credit: 4, type: '必修', teachingGroupId: 1 },
  { id: 2, courseCode: 'C002', name: '数据结构', credit: 3.5, type: '必修', teachingGroupId: 1 },
  { id: 3, courseCode: 'C003', name: '数据库原理', credit: 3, type: '必修', teachingGroupId: 2 },
  { id: 4, courseCode: 'C004', name: 'Web前端开发', credit: 2.5, type: '选修', teachingGroupId: 2 },
]

const exams: MockRow[] = [
  { id: 1, name: '高等数学（上）期末考试', courseId: 1, termId: 1, examType: '期末', examDate: dayOffset(7), startTime: '09:00', endTime: '11:00', status: '已安排', courseName: '高等数学（上）' },
  { id: 2, name: '数据结构期末考试', courseId: 2, termId: 1, examType: '期末', examDate: dayOffset(8), startTime: '09:00', endTime: '11:00', status: '已安排', courseName: '数据结构' },
  { id: 3, name: '数据库原理期末考试', courseId: 3, termId: 1, examType: '期末', examDate: dayOffset(9), startTime: '14:00', endTime: '16:00', status: '已安排', courseName: '数据库原理' },
]

/** 成绩（exam_score；status：NORMAL/ABSENT/DELAY/CHEAT） */
const scores: MockRow[] = [
  { id: 1, examId: 1, studentId: 1, score: 85, status: 'NORMAL' },
  { id: 2, examId: 1, studentId: 2, score: 92, status: 'NORMAL' },
  { id: 3, examId: 1, studentId: 3, score: 45, status: 'NORMAL' },
  { id: 4, examId: 1, studentId: 4, score: 78, status: 'NORMAL' },
  { id: 5, examId: 2, studentId: 1, score: 70, status: 'NORMAL' },
  { id: 6, examId: 2, studentId: 2, score: 88, status: 'NORMAL' },
  { id: 7, examId: 2, studentId: 3, score: 55, status: 'NORMAL' },
  { id: 8, examId: 2, studentId: 4, score: null, status: 'ABSENT' },
]

/** 教师任教关系（缺口：后端没有任课表，这里仅用于 Mock 演示） */
const teacherClasses: MockRow[] = [
  { teacherId: 1, classId: 1 },
  { teacherId: 2, classId: 1 },
  { teacherId: 2, classId: 2 },
  { teacherId: 3, classId: 2 },
  { teacherId: 4, classId: 1 },
]

const teacherCourses: MockRow[] = [
  { teacherId: 1, courseId: 1 },
  { teacherId: 2, courseId: 2 },
  { teacherId: 3, courseId: 3 },
  { teacherId: 4, courseId: 4 },
]

/** 学生考勤（status：正常/迟到/缺勤/请假） */
const studentAttendance: MockRow[] = [
  { id: 1, studentId: 1, courseId: 1, attendanceDate: dayOffset(-1), status: '正常' },
  { id: 2, studentId: 2, courseId: 1, attendanceDate: dayOffset(-1), status: '正常' },
  { id: 3, studentId: 3, courseId: 1, attendanceDate: dayOffset(-1), status: '缺勤' },
  { id: 4, studentId: 4, courseId: 1, attendanceDate: dayOffset(-1), status: '迟到' },
  { id: 5, studentId: 1, courseId: 2, attendanceDate: dayOffset(-3), status: '正常' },
  { id: 6, studentId: 2, courseId: 2, attendanceDate: dayOffset(-3), status: '请假' },
  { id: 7, studentId: 3, courseId: 2, attendanceDate: dayOffset(-3), status: '正常' },
  { id: 8, studentId: 4, courseId: 2, attendanceDate: dayOffset(-3), status: '正常' },
]

/** 教师考勤（签到） */
const teacherAttendance: MockRow[] = [
  { id: 1, teacherId: 1, attendanceDate: dayOffset(-1), status: '正常', checkTime: `${dayOffset(-1)} 07:52` },
  { id: 2, teacherId: 2, attendanceDate: dayOffset(-1), status: '迟到', checkTime: `${dayOffset(-1)} 08:35` },
  { id: 3, teacherId: 3, attendanceDate: dayOffset(-1), status: '正常', checkTime: `${dayOffset(-1)} 07:45` },
]

/** 教学日志 */
const teachingLogs: MockRow[] = [
  { id: 1, teacherId: 1, courseId: 1, classId: 1, teachingDate: dayOffset(-1), content: '极限与连续：讲解极限定义、两个重要极限及例题演练。', homework: '习题 1-3 第 1、3、5 题' },
  { id: 2, teacherId: 1, courseId: 1, classId: 1, teachingDate: dayOffset(-2), content: '函数与数列：复习函数性质，引入数列极限概念。', homework: '习题 1-2 全部' },
  { id: 3, teacherId: 2, courseId: 2, classId: 1, teachingDate: dayOffset(-1), content: '线性表：顺序存储结构与链式存储结构对比。', homework: '实现单链表插入与删除' },
]

/** 补考重修 */
const retakes: MockRow[] = [
  { id: 1, studentId: 3, courseId: 2, examId: null, type: '重修' },
  { id: 2, studentId: 4, courseId: 1, examId: null, type: '补考' },
]

/** 专升本报名（applyStatus：WAIT/PASS/FAIL） */
const upgradeApplies: MockRow[] = [
  { id: 1, studentId: 2, schoolName: '华中科技大学', majorName: '计算机科学与技术', applyStatus: 'WAIT', remark: null, createTime: `${dayOffset(-5)} 10:20` },
  { id: 2, studentId: 3, schoolName: '武汉理工大学', majorName: '软件工程', applyStatus: 'PASS', remark: '符合报名条件', createTime: `${dayOffset(-9)} 15:02` },
]

/** 毕业资格审核 */
const graduateChecks: MockRow[] = [
  { id: 1, studentId: 1, checkStatus: 'PASS', creditStatus: 'PASS', courseStatus: 'PASS', remark: '学分与课程全部合格，准予毕业', checker: '教务处', updateTime: `${dayOffset(-2)} 09:12` },
  { id: 2, studentId: 2, checkStatus: 'FAIL', creditStatus: 'PASS', courseStatus: 'FAIL', remark: '有 2 门课程不合格，需参加补考', checker: '教务处', updateTime: `${dayOffset(-2)} 09:15` },
  { id: 3, studentId: 3, checkStatus: 'WAIT', creditStatus: null, courseStatus: null, remark: null, checker: null, updateTime: null },
]

/** 考核方式申报（status：WAIT/PASS/FAIL） */
const examApplies: MockRow[] = [
  { id: 1, courseId: 1, teacherId: 1, applyType: '闭卷', reason: '高等数学为专业基础课，采用闭卷笔试考核。', status: 'PASS', createTime: `${dayOffset(-12)} 11:00` },
  { id: 2, courseId: 2, teacherId: 1, applyType: '机考', reason: '数据结构侧重算法实现，采用上机实操考核。', status: 'WAIT', createTime: `${dayOffset(-3)} 14:30` },
]

/** 监考安排（monitorRole：MAIN/SUB） */
const examMonitors: MockRow[] = [
  { id: 1, examId: 1, teacherId: 1, monitorRole: 'MAIN' },
  { id: 2, examId: 1, teacherId: 2, monitorRole: 'SUB' },
  { id: 3, examId: 2, teacherId: 2, monitorRole: 'MAIN' },
  { id: 4, examId: 2, teacherId: 3, monitorRole: 'SUB' },
  { id: 5, examId: 3, teacherId: 1, monitorRole: 'SUB' },
]

/** 考场（用于监考安排展示考场名） */
const examRooms: MockRow[] = [
  { examId: 1, roomName: '第一教学楼 101' },
  { examId: 2, roomName: '第二教学楼 301' },
  { examId: 3, roomName: '实验楼 A202 机房' },
]

let sequence = 1000
function nextId(): number {
  sequence += 1
  return sequence
}

/* --------------------------- 数据装配辅助 --------------------------- */

function studentOf(id: number): MockRow | undefined {
  return students.find((item) => item.id === id)
}

function teacherOf(id: number): MockRow | undefined {
  return teachers.find((item) => item.id === id)
}

function courseOf(id: number): MockRow | undefined {
  return courses.find((item) => item.id === id)
}

function classOf(id: number): MockRow | undefined {
  return classes.find((item) => item.id === id)
}

function examOf(id: number): MockRow | undefined {
  return exams.find((item) => item.id === id)
}

/** 成绩补全课程 / 考试 / 学生信息，便于页面直接展示 */
function enrichScore(row: MockRow): MockRow {
  const exam = examOf(row.examId)
  const course = exam ? courseOf(exam.courseId) : undefined
  const student = studentOf(row.studentId)
  return {
    ...row,
    examName: exam?.name ?? `考试#${row.examId}`,
    courseName: course?.name ?? `课程#${exam?.courseId ?? '-'}`,
    credit: course?.credit ?? null,
    examDate: exam?.examDate ?? null,
    studentName: student?.name ?? null,
    studentNo: student?.studentNo ?? null,
  }
}

function enrichAttendance(row: MockRow): MockRow {
  const course = courseOf(row.courseId)
  const student = studentOf(row.studentId)
  return {
    ...row,
    courseName: course?.name ?? `课程#${row.courseId}`,
    studentName: student?.name ?? null,
    studentNo: student?.studentNo ?? null,
  }
}

function enrichTeachingLog(row: MockRow): MockRow {
  return {
    ...row,
    courseName: courseOf(row.courseId)?.name ?? `课程#${row.courseId}`,
    className: classOf(row.classId)?.name ?? `班级#${row.classId}`,
  }
}

function enrichRetake(row: MockRow): MockRow {
  const student = studentOf(row.studentId)
  return {
    ...row,
    courseName: courseOf(row.courseId)?.name ?? `课程#${row.courseId}`,
    studentName: student?.name ?? null,
    studentNo: student?.studentNo ?? null,
  }
}

function enrichUpgrade(row: MockRow): MockRow {
  const student = studentOf(row.studentId)
  return {
    ...row,
    studentName: student?.name ?? null,
    studentNo: student?.studentNo ?? null,
  }
}

function enrichGraduateCheck(row: MockRow): MockRow {
  const student = studentOf(row.studentId)
  return {
    ...row,
    studentName: student?.name ?? null,
    studentNo: student?.studentNo ?? null,
    className: student?.className ?? null,
  }
}

function enrichExamApply(row: MockRow): MockRow {
  return {
    ...row,
    courseName: courseOf(row.courseId)?.name ?? `课程#${row.courseId}`,
    teacherName: teacherOf(row.teacherId)?.name ?? null,
  }
}

function enrichMonitor(row: MockRow): MockRow {
  const exam = examOf(row.examId)
  const room = examRooms.find((item) => item.examId === row.examId)
  return {
    ...row,
    examName: exam?.name ?? `考试#${row.examId}`,
    courseName: exam?.courseName ?? null,
    examDate: exam?.examDate ?? null,
    startTime: exam?.startTime ?? null,
    endTime: exam?.endTime ?? null,
    roomName: room?.roomName ?? null,
    teacherName: teacherOf(row.teacherId)?.name ?? null,
  }
}

/** 学生登录名 → 学号（支持"学号"与 student01 这类别名） */
function resolveStudentAccount(username: string): MockRow | undefined {
  const alias: Record<string, string> = { student01: '2023005001', student02: '2023005002', student03: '2023005003', student04: '2023005004' }
  const studentNo = alias[username] ?? username
  return students.find((item) => item.studentNo === studentNo)
}

/** 教师登录名 → 工号（支持"工号"与 teacher01 这类别名） */
function resolveTeacherAccount(username: string): MockRow | undefined {
  const alias: Record<string, string> = { teacher01: 'T1001', teacher02: 'T1002', teacher03: 'T1003', teacher04: 'T1004' }
  const teacherNo = alias[username] ?? username
  return teachers.find((item) => item.teacherNo === teacherNo)
}

const DEMO_PASSWORD = '123456'
const CURRENT_TERM = '2026-2027学年第一学期'

/* ------------------------------ 路由分发 ------------------------------ */

function handleApi(ctx: MockContext) {
  const rawUrl = String(ctx.url ?? '')
  const path = rawUrl.split('?')[0]
  const query: MockRow = ctx.query ?? {}
  const body: MockRow = ctx.body ?? {}

  /** 便捷取路径参数 */
  function pathParams(pattern: RegExp): RegExpMatchArray | null {
    return path.match(pattern)
  }

  /* ---------- 认证 ---------- */
  if (path === '/api/auth/login') {
    const username = String(body.username ?? '').trim()
    const password = String(body.password ?? '')
    if (!username || !password) {
      return fail('请输入账号与密码')
    }
    if (username === 'admin' || username === 'ADMIN') {
      return fail('管理员请使用后台管理端（admin）登录')
    }
    const student = resolveStudentAccount(username)
    if (student) {
      if (password !== DEMO_PASSWORD) {
        return fail('密码错误')
      }
      return ok({
        token: `mock-token-student-${student.id}`,
        roles: ['student'],
        name: student.name,
        user: { id: student.id, username: student.studentNo, role: 'STUDENT' },
      })
    }
    const teacher = resolveTeacherAccount(username)
    if (teacher) {
      if (password !== DEMO_PASSWORD) {
        return fail('密码错误')
      }
      return ok({
        token: `mock-token-teacher-${teacher.id}`,
        roles: ['teacher'],
        name: teacher.name,
        user: { id: teacher.id, username: teacher.teacherNo, role: 'TEACHER' },
      })
    }
    return fail('账号不存在（演示账号：2023005001 或 T1001，密码 123456）')
  }

  if (path === '/api/auth/logout') {
    return ok(null)
  }

  /* ---------- 学期 / 身份解析 ---------- */
  if (path === '/api/term/current') {
    // 后端返回 Term 实体，这里两种形状都演示（前端已做归一化）
    return ok({ id: 1, name: CURRENT_TERM, startDate: '2026-09-01', endDate: '2027-01-20' })
  }

  if (path.startsWith('/api/student/by-no/')) {
    const studentNo = decodeURIComponent(path.replace('/api/student/by-no/', ''))
    const student = students.find((item) => item.studentNo === studentNo)
    return student ? ok(student) : fail('未找到该学号对应的学生')
  }

  if (path.startsWith('/api/teacher/by-no/')) {
    const teacherNo = decodeURIComponent(path.replace('/api/teacher/by-no/', ''))
    const teacher = teachers.find((item) => item.teacherNo === teacherNo)
    return teacher ? ok(teacher) : fail('未找到该工号对应的教师')
  }

  /* ---------- 基础数据（下拉选择用） ---------- */
  if (path === '/api/class/page') {
    const keyword = String(query.keyword ?? '')
    const list = classes.filter((item) => !keyword || String(item.name).includes(keyword))
    return ok(paged(list, query))
  }

  if (path === '/api/course/page') {
    const keyword = String(query.keyword ?? '')
    const list = courses.filter((item) => !keyword || String(item.name).includes(keyword))
    return ok(paged(list, query))
  }

  if (path === '/api/exam/page') {
    const name = String(query.name ?? '')
    const list = exams.filter((item) => !name || String(item.name).includes(name))
    return ok(paged(list, query))
  }

  const studentsByClass = pathParams(/^\/api\/student\/list-by-class\/(\d+)$/)
  if (studentsByClass) {
    const classId = Number(studentsByClass[1])
    return ok(students.filter((item) => item.classId === classId))
  }

  /* ---------- 教师自己的班级 / 课程（缺口接口，仅供 Mock 演示） ---------- */
  if (path === '/api/teacher/my-classes') {
    const teacherId = Number(query.teacherId ?? 0)
    const ids = teacherClasses.filter((item) => item.teacherId === teacherId).map((item) => item.classId)
    return ok(classes.filter((item) => ids.includes(item.id)))
  }

  if (path === '/api/teacher/my-courses') {
    const teacherId = Number(query.teacherId ?? 0)
    const ids = teacherCourses.filter((item) => item.teacherId === teacherId).map((item) => item.courseId)
    return ok(courses.filter((item) => ids.includes(item.id)))
  }

  /* ---------- 成绩 ---------- */
  const scoresByStudent = pathParams(/^\/api\/score\/list-by-student\/(\d+)$/)
  if (scoresByStudent) {
    const studentId = Number(scoresByStudent[1])
    return ok(scores.filter((item) => item.studentId === studentId).map(enrichScore))
  }

  if (path === '/api/score/page-by-exam') {
    const examId = Number(query.examId ?? 0)
    const status = String(query.status ?? '')
    const list = scores
      .filter((item) => item.examId === examId)
      .filter((item) => !status || item.status === status)
      .map(enrichScore)
    return ok(paged(list, query))
  }

  const scoreSave = pathParams(/^\/api\/score\/save\/(\d+)$/)
  if (scoreSave) {
    const examId = Number(scoreSave[1])
    const rows: MockRow[] = Array.isArray(body) ? body : []
    if (rows.length === 0) {
      return fail('没有需要保存的成绩')
    }
    for (const row of rows) {
      const studentId = Number(row.studentId ?? 0)
      if (!studentId) {
        return fail('存在缺少学生ID的成绩记录')
      }
      const score = row.score === '' || row.score === null || row.score === undefined ? null : Number(row.score)
      if (score !== null && (Number.isNaN(score) || score < 0 || score > 100)) {
        return fail('分数必须是 0~100 之间的数字')
      }
      const existing = scores.find((item) => item.examId === examId && item.studentId === studentId)
      const status = row.status ?? (score === null ? 'ABSENT' : 'NORMAL')
      if (existing) {
        existing.score = score
        existing.status = status
      } else {
        scores.push({ id: nextId(), examId, studentId, score, status })
      }
    }
    return ok(null, '成绩已保存')
  }

  const scoreStat = pathParams(/^\/api\/score\/stat\/(\d+)$/)
  if (scoreStat) {
    const examId = Number(scoreStat[1])
    const rows = scores.filter((item) => item.examId === examId)
    const absent = rows.filter((item) => item.status === 'ABSENT').length
    const scored = rows.filter((item) => item.status !== 'ABSENT' && item.score !== null)
    return ok({
      total: rows.length,
      actual: rows.length - absent,
      absent,
      passed: scored.filter((item) => Number(item.score) >= 60).length,
      failed: scored.filter((item) => Number(item.score) < 60).length,
    })
  }

  /* ---------- 学生考勤 ---------- */
  if (path === '/api/student-attendance/list-by-student') {
    const studentId = Number(query.studentId ?? 0)
    const startDate = String(query.startDate ?? '')
    const endDate = String(query.endDate ?? '')
    const list = studentAttendance
      .filter((item) => item.studentId === studentId)
      .filter((item) => !startDate || !endDate || between(item.attendanceDate, startDate, endDate))
      .sort((a, b) => String(b.attendanceDate).localeCompare(String(a.attendanceDate)))
      .map(enrichAttendance)
    return ok(list)
  }

  if (path === '/api/student-attendance/record') {
    const rows: MockRow[] = Array.isArray(body) ? body : []
    if (rows.length === 0) {
      return fail('没有需要提交的考勤记录')
    }
    for (const row of rows) {
      const studentId = Number(row.studentId ?? 0)
      const courseId = Number(row.courseId ?? 0)
      const attendanceDate = String(row.attendanceDate ?? '')
      if (!studentId || !courseId || !attendanceDate) {
        return fail('考勤记录缺少学生、课程或日期')
      }
      const existing = studentAttendance.find(
        (item) => item.studentId === studentId && item.courseId === courseId && item.attendanceDate === attendanceDate,
      )
      if (existing) {
        existing.status = row.status ?? existing.status
      } else {
        studentAttendance.push({ id: nextId(), studentId, courseId, attendanceDate, status: row.status ?? '正常' })
      }
    }
    return ok(null, '考勤已提交')
  }

  if (path === '/api/student-attendance/weekly-report') {
    const classId = Number(query.classId ?? 0)
    const date = String(query.date ?? dayOffset(0))
    const range = weekRangeOf(date)
    const classStudents = students.filter((item) => item.classId === classId)
    const rows = classStudents.map((student) => {
      const records = studentAttendance.filter(
        (item) => item.studentId === student.id && between(item.attendanceDate, range.start, range.end),
      )
      const normal = records.filter((item) => item.status === '正常').length
      const late = records.filter((item) => item.status === '迟到').length
      const absent = records.filter((item) => item.status === '缺勤').length
      const leave = records.filter((item) => item.status === '请假').length
      const total = records.length
      return {
        studentId: student.id,
        studentName: student.name,
        studentNo: student.studentNo,
        normal,
        late,
        absent,
        leave,
        total,
        rate: total === 0 ? null : Math.round(((normal + late) / total) * 100),
      }
    })
    return ok(rows)
  }

  /* ---------- 教师考勤 ---------- */
  const checkIn = pathParams(/^\/api\/teacher-attendance\/check-in\/(\d+)$/)
  if (checkIn) {
    const teacherId = Number(checkIn[1])
    const today = dayOffset(0)
    if (!teacherOf(teacherId)) {
      return fail('教师不存在')
    }
    const existing = teacherAttendance.find(
      (item) => item.teacherId === teacherId && item.attendanceDate === today,
    )
    if (existing) {
      return fail('今日已签到，无需重复签到')
    }
    teacherAttendance.push({
      id: nextId(),
      teacherId,
      attendanceDate: today,
      status: '正常',
      checkTime: nowText(),
    })
    return ok(null, '签到成功')
  }

  if (path === '/api/teacher-attendance/list-by-date') {
    const date = String(query.date ?? dayOffset(0))
    const list = teacherAttendance
      .filter((item) => item.attendanceDate === date)
      .map((item) => ({ ...item, teacherName: teacherOf(item.teacherId)?.name ?? null }))
    return ok(list)
  }

  if (path === '/api/teacher-attendance/stat-by-date') {
    const date = String(query.date ?? dayOffset(0))
    const checked = teacherAttendance.filter(
      (item) => item.attendanceDate === date && item.status !== '缺勤',
    ).length
    return ok({ total: teachers.length, checked, unchecked: teachers.length - checked })
  }

  /* ---------- 教学日志 ---------- */
  if (path === '/api/teaching-log' && ctx.body !== undefined) {
    const teacherId = Number(body.teacherId ?? 0)
    const courseId = Number(body.courseId ?? 0)
    const classId = Number(body.classId ?? 0)
    const teachingDate = String(body.teachingDate ?? '')
    if (!teacherId || !courseId || !classId) {
      return fail('请选择课程与班级')
    }
    if (!teachingDate) {
      return fail('请选择授课日期')
    }
    if (teachingDate > dayOffset(0)) {
      return fail('授课日期不能晚于今天')
    }
    if (!String(body.content ?? '').trim()) {
      return fail('请填写授课内容')
    }
    teachingLogs.push({
      id: nextId(),
      teacherId,
      courseId,
      classId,
      teachingDate,
      content: String(body.content).trim(),
      homework: body.homework ? String(body.homework) : '',
    })
    return ok(null, '教学日志已提交')
  }

  if (path === '/api/teaching-log/list-by-teacher-week') {
    const teacherId = Number(query.teacherId ?? 0)
    const date = String(query.date ?? dayOffset(0))
    const range = weekRangeOf(date)
    const list = teachingLogs
      .filter((item) => item.teacherId === teacherId && between(item.teachingDate, range.start, range.end))
      .sort((a, b) => String(b.teachingDate).localeCompare(String(a.teachingDate)))
      .map(enrichTeachingLog)
    return ok(list)
  }

  if (path === '/api/teaching-log/list-by-class-week') {
    const classId = Number(query.classId ?? 0)
    const date = String(query.date ?? dayOffset(0))
    const range = weekRangeOf(date)
    const list = teachingLogs
      .filter((item) => item.classId === classId && between(item.teachingDate, range.start, range.end))
      .map(enrichTeachingLog)
    return ok(list)
  }

  /* ---------- 补考重修 ---------- */
  if (path === '/api/retake/apply') {
    const studentId = Number(query.studentId ?? body.studentId ?? 0)
    const courseId = Number(query.courseId ?? body.courseId ?? 0)
    if (!studentId || !courseId) {
      return fail('请选择要申请的课程')
    }
    const duplicated = retakes.find(
      (item) => item.studentId === studentId && item.courseId === courseId && item.examId === null,
    )
    if (duplicated) {
      return fail('该课程已有待安排的申请，请勿重复提交')
    }
    retakes.push({ id: nextId(), studentId, courseId, examId: null, type: '重修' })
    return ok(null, '申请已提交，等待教务安排')
  }

  const retakeByStudent = pathParams(/^\/api\/retake\/list-by-student\/(\d+)$/)
  if (retakeByStudent) {
    const studentId = Number(retakeByStudent[1])
    return ok(retakes.filter((item) => item.studentId === studentId).map(enrichRetake))
  }

  const retakeByType = pathParams(/^\/api\/retake\/list-by-type\/(.+)$/)
  if (retakeByType) {
    const type = decodeURIComponent(retakeByType[1])
    return ok(retakes.filter((item) => item.type === type).map(enrichRetake))
  }

  /* ---------- 专升本报名 ---------- */
  if (path === '/api/upgrade-apply/apply') {
    const studentId = Number(body.studentId ?? 0)
    if (!studentId) {
      return fail('缺少学生信息')
    }
    if (!String(body.schoolName ?? '').trim() || !String(body.majorName ?? '').trim()) {
      return fail('请填写报考院校与专业')
    }
    const pending = upgradeApplies.find(
      (item) => item.studentId === studentId && item.applyStatus === 'WAIT',
    )
    if (pending) {
      return fail('已有待审核的报名，请等待审核结果')
    }
    const created = {
      id: nextId(),
      studentId,
      schoolName: String(body.schoolName).trim(),
      majorName: String(body.majorName).trim(),
      applyStatus: 'WAIT',
      remark: body.remark ? String(body.remark) : null,
      createTime: nowText(),
    }
    upgradeApplies.push(created)
    return ok(enrichUpgrade(created), '报名已提交')
  }

  if (path === '/api/upgrade-apply/my/list') {
    const studentId = Number(query.studentId ?? 0)
    const list = upgradeApplies
      .filter((item) => item.studentId === studentId)
      .sort((a, b) => Number(b.id) - Number(a.id))
      .map(enrichUpgrade)
    return ok(list)
  }

  const upgradeDetail = pathParams(/^\/api\/upgrade-apply\/(\d+)$/)
  if (upgradeDetail) {
    const id = Number(upgradeDetail[1])
    const row = upgradeApplies.find((item) => item.id === id)
    return row ? ok(enrichUpgrade(row)) : fail('报名记录不存在')
  }

  /* ---------- 毕业资格 ---------- */
  const graduateByStudent = pathParams(/^\/api\/graduate-check\/by-student\/(\d+)$/)
  if (graduateByStudent) {
    const studentId = Number(graduateByStudent[1])
    const row = graduateChecks.find((item) => item.studentId === studentId)
    if (!row) {
      return ok(null)
    }
    return ok(enrichGraduateCheck(row))
  }

  /* ---------- 考核方式申报 ---------- */
  if (path === '/api/exam-apply/apply') {
    const teacherId = Number(body.teacherId ?? 0)
    const courseId = Number(body.courseId ?? 0)
    if (!teacherId || !courseId) {
      return fail('请选择申报课程')
    }
    if (!String(body.applyType ?? '').trim()) {
      return fail('请选择考核方式')
    }
    const pending = examApplies.find(
      (item) => item.teacherId === teacherId && item.courseId === courseId && item.status === 'WAIT',
    )
    if (pending) {
      return fail('该课程已有待审核的申报')
    }
    const created = {
      id: nextId(),
      courseId,
      teacherId,
      applyType: String(body.applyType).trim(),
      reason: body.reason ? String(body.reason) : '',
      status: 'WAIT',
      createTime: nowText(),
    }
    examApplies.push(created)
    return ok(enrichExamApply(created), '申报已提交')
  }

  if (path === '/api/exam-apply/my/list') {
    const teacherId = Number(query.teacherId ?? 0)
    const list = examApplies
      .filter((item) => item.teacherId === teacherId)
      .sort((a, b) => Number(b.id) - Number(a.id))
      .map(enrichExamApply)
    return ok(list)
  }

  const examApplyDetail = pathParams(/^\/api\/exam-apply\/(\d+)$/)
  if (examApplyDetail) {
    const id = Number(examApplyDetail[1])
    const row = examApplies.find((item) => item.id === id)
    return row ? ok(enrichExamApply(row)) : fail('申报记录不存在')
  }

  /* ---------- 监考安排 ---------- */
  const monitorByTeacher = pathParams(/^\/api\/exam-monitor\/list-by-teacher\/(\d+)$/)
  if (monitorByTeacher) {
    const teacherId = Number(monitorByTeacher[1])
    const list = examMonitors
      .filter((item) => item.teacherId === teacherId)
      .map(enrichMonitor)
      .sort((a, b) => String(a.examDate ?? '').localeCompare(String(b.examDate ?? '')))
    return ok(list)
  }

  const monitorByExam = pathParams(/^\/api\/exam-monitor\/list-by-exam\/(\d+)$/)
  if (monitorByExam) {
    const examId = Number(monitorByExam[1])
    return ok(examMonitors.filter((item) => item.examId === examId).map(enrichMonitor))
  }

  /* ---------- 兜底：明确失败，避免 URL 写错却"静默成功" ---------- */
  return fail(`Mock 未配置该接口：${path}`)
}

const methods = ['get', 'post', 'put', 'delete', 'patch'] as const

/** 对每种 HTTP 方法注册同一个分发器（vite-plugin-mock v3 的 url 仅支持字符串模板） */
const mockApi = methods.map((method) => ({
  url: '/api/:rest*',
  method,
  timeout: 150,
  response: (ctx: MockContext) => handleApi(ctx),
})) as unknown as MockMethod[]

export default mockApi
