import type { MockMethod } from 'vite-plugin-mock'

/**
 * 全局 Mock（由 vite-plugin-mock 在开发环境加载）：
 * 拦截所有以 /api/ 开头的请求，统一返回 { code: 0, data: any, msg: string }。
 * 新接口数据在 handleApi 的 switch 中补充即可；未匹配的接口返回请求回显便于联调。
 */

/** vite-plugin-mock 回调上下文 */
interface MockContext {
  url: string
  query?: Record<string, unknown>
  body?: Record<string, unknown>
  headers?: Record<string, string>
}

type MockRow = Record<string, any>

const ok = <T = unknown>(data: T, msg = 'success') => ({ code: 0, data, msg })
const fail = (msg: string, code = 1) => ({ code, data: null, msg })

/* ==================== 工具函数 ==================== */

function nowText() {
  const d = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function nextId(list: MockRow[]) {
  return list.reduce((max, row) => Math.max(max, Number(row.id)), 0) + 1
}

function pick(body: MockRow, keys: string[]) {
  const result: MockRow = {}
  for (const key of keys) {
    if (body[key] !== undefined) {
      result[key] = body[key]
    }
  }
  return result
}

/** 按 page/pageSize 分页并返回 PageResult 结构 */
function pageData(
  list: MockRow[],
  ctx: MockContext,
  matcher?: (row: MockRow) => boolean,
) {
  const page = Math.max(1, Number(ctx.query?.page) || 1)
  const pageSize = Math.max(1, Number(ctx.query?.pageSize) || 10)
  const filtered = matcher ? list.filter(matcher) : [...list]
  const start = (page - 1) * pageSize
  return {
    list: filtered.slice(start, start + pageSize),
    total: filtered.length,
    page,
    pageSize,
  }
}

function removeById(list: MockRow[], ctx: MockContext) {
  const id = Number((ctx.body as MockRow)?.id)
  const index = list.findIndex((row) => row.id === id)
  if (index > -1) {
    list.splice(index, 1)
    return true
  }
  return false
}

/* ==================== 课程种子数据（10 条） ==================== */

const TERM_1 = '2026-2027学年第一学期'
const TERM_2 = '2026-2027学年第二学期'

const courseList: MockRow[] = [
  { id: 1001, name: '高等数学（上）', term: TERM_1, teacher: '陈志强', credit: 4, startDate: '2026-09-01', createTime: '2026-08-01 09:10:00' },
  { id: 1002, name: '高等数学（下）', term: TERM_2, teacher: '陈志强', credit: 4, startDate: '2026-02-26', createTime: '2026-08-02 09:10:00' },
  { id: 1003, name: '大学英语（一）', term: TERM_1, teacher: '王丽华', credit: 3, startDate: '2026-09-01', createTime: '2026-08-03 09:10:00' },
  { id: 1004, name: '大学英语（二）', term: TERM_2, teacher: '王丽华', credit: 3, startDate: '2026-02-26', createTime: '2026-08-04 09:10:00' },
  { id: 1005, name: '线性代数', term: TERM_1, teacher: '张伟', credit: 3, startDate: '2026-09-01', createTime: '2026-08-05 09:10:00' },
  { id: 1006, name: '概率论与数理统计', term: TERM_2, teacher: '张伟', credit: 4, startDate: '2026-02-26', createTime: '2026-08-06 09:10:00' },
  { id: 1007, name: '程序设计基础（Python）', term: TERM_1, teacher: '李思远', credit: 4, startDate: '2026-09-01', createTime: '2026-08-07 09:10:00' },
  { id: 1008, name: '数据结构', term: TERM_2, teacher: '李思远', credit: 4, startDate: '2026-02-26', createTime: '2026-08-08 09:10:00' },
  { id: 1009, name: '大学物理（一）', term: TERM_1, teacher: '赵明', credit: 4, startDate: '2026-09-01', createTime: '2026-08-09 09:10:00' },
  { id: 1010, name: '马克思主义基本原理', term: TERM_2, teacher: '孙芳', credit: 3, startDate: '2026-02-26', createTime: '2026-08-10 09:10:00' },
]

/* ==================== 班级种子数据（10 条） ==================== */

const classList: MockRow[] = [
  { id: 2001, name: '软件工程2401班', grade: '2024级', major: '软件工程', headTeacher: '王涛', studentCount: 42, enrollDate: '2024-09-01', createTime: '2026-08-01 10:20:00' },
  { id: 2002, name: '软件工程2402班', grade: '2024级', major: '软件工程', headTeacher: '王涛', studentCount: 41, enrollDate: '2024-09-01', createTime: '2026-08-02 10:20:00' },
  { id: 2003, name: '计算机2401班', grade: '2024级', major: '计算机科学与技术', headTeacher: '李梅', studentCount: 45, enrollDate: '2024-09-01', createTime: '2026-08-03 10:20:00' },
  { id: 2004, name: '计算机2402班', grade: '2024级', major: '计算机科学与技术', headTeacher: '李梅', studentCount: 44, enrollDate: '2024-09-01', createTime: '2026-08-04 10:20:00' },
  { id: 2005, name: '网络2401班', grade: '2024级', major: '网络工程', headTeacher: '刘洋', studentCount: 38, enrollDate: '2024-09-01', createTime: '2026-08-05 10:20:00' },
  { id: 2006, name: '人工智能2401班', grade: '2024级', major: '人工智能', headTeacher: '周敏', studentCount: 40, enrollDate: '2024-09-01', createTime: '2026-08-06 10:20:00' },
  { id: 2007, name: '大数据2401班', grade: '2024级', major: '数据科学与大数据技术', headTeacher: '陈晨', studentCount: 43, enrollDate: '2024-09-01', createTime: '2026-08-07 10:20:00' },
  { id: 2008, name: '物联网2501班', grade: '2025级', major: '物联网工程', headTeacher: '郑强', studentCount: 36, enrollDate: '2025-09-01', createTime: '2026-08-08 10:20:00' },
  { id: 2009, name: '计算机2501班', grade: '2025级', major: '计算机科学与技术', headTeacher: '李梅', studentCount: 44, enrollDate: '2025-09-01', createTime: '2026-08-09 10:20:00' },
  { id: 2010, name: '软件工程2501班', grade: '2025级', major: '软件工程', headTeacher: '王涛', studentCount: 40, enrollDate: '2025-09-01', createTime: '2026-08-10 10:20:00' },
]

/* ==================== 其余模块种子数据（5-8 条） ==================== */

/** 教材管理 - 新书入库 */
const stockInList: MockRow[] = [
  { id: 3001, name: '高等数学（上）', isbn: '978-7-04-056123-4', publisher: '高等教育出版社', quantity: 120, stockDate: '2026-09-01', createTime: '2026-09-01 10:00:00' },
  { id: 3002, name: '大学英语综合教程（一）', isbn: '978-7-5135-6328-9', publisher: '外语教学与研究出版社', quantity: 240, stockDate: '2026-09-02', createTime: '2026-09-02 10:00:00' },
  { id: 3003, name: '程序设计基础（Python）', isbn: '978-7-302-51210-5', publisher: '清华大学出版社', quantity: 180, stockDate: '2026-09-03', createTime: '2026-09-03 10:00:00' },
  { id: 3004, name: '线性代数', isbn: '978-7-04-044315-8', publisher: '高等教育出版社', quantity: 150, stockDate: '2026-09-05', createTime: '2026-09-05 10:00:00' },
  { id: 3005, name: '大学物理（上）', isbn: '978-7-115-42387-6', publisher: '机械工业出版社', quantity: 90, stockDate: '2026-09-06', createTime: '2026-09-06 10:00:00' },
  { id: 3006, name: '马克思主义基本原理', isbn: '978-7-01-018922-0', publisher: '人民出版社', quantity: 260, stockDate: '2026-09-08', createTime: '2026-09-08 10:00:00' },
]

/** 毕业审核 - 毕业生管理 */
const studentList: MockRow[] = [
  { id: 4001, studentNo: '2022001', name: '张伟', major: '软件工程', graduateYear: '2026', status: '待审核', createTime: '2026-09-01 11:00:00' },
  { id: 4002, studentNo: '2022002', name: '王芳', major: '计算机科学与技术', graduateYear: '2026', status: '通过', createTime: '2026-09-01 11:00:00' },
  { id: 4003, studentNo: '2022003', name: '李强', major: '网络工程', graduateYear: '2026', status: '待审核', createTime: '2026-09-01 11:00:00' },
  { id: 4004, studentNo: '2022004', name: '刘敏', major: '人工智能', graduateYear: '2026', status: '通过', createTime: '2026-09-01 11:00:00' },
  { id: 4005, studentNo: '2022005', name: '陈杰', major: '软件工程', graduateYear: '2026', status: '未通过', createTime: '2026-09-01 11:00:00' },
  { id: 4006, studentNo: '2022006', name: '赵雪', major: '数据科学与大数据技术', graduateYear: '2026', status: '通过', createTime: '2026-09-01 11:00:00' },
  { id: 4007, studentNo: '2022007', name: '孙涛', major: '物联网工程', graduateYear: '2026', status: '待审核', createTime: '2026-09-02 11:00:00' },
  { id: 4008, studentNo: '2022008', name: '周静', major: '计算机科学与技术', graduateYear: '2026', status: '通过', createTime: '2026-09-02 11:00:00' },
  { id: 4009, studentNo: '2022009', name: '吴迪', major: '数字媒体技术', graduateYear: '2026', status: '未通过', createTime: '2026-09-02 11:00:00' },
  { id: 4010, studentNo: '2022010', name: '郑凯', major: '大数据管理与应用', graduateYear: '2026', status: '待审核', createTime: '2026-09-02 11:00:00' },
]

/** 调课管理 - 我的调课 */
const myAdjustList: MockRow[] = [
  { id: 5001, courseName: '高等数学（上）', teacher: '陈志强', reason: '教师外出参加学术会议，需调整上课时间', originalTime: '2026-09-11 08:00~09:40', newTime: '2026-09-15 15:00~16:40', status: '待审核', createTime: '2026-09-09 09:00:00' },
  { id: 5002, courseName: '大学英语（一）', teacher: '王丽华', reason: '多媒体教室设备维护，临时更换时间', originalTime: '2026-09-12 10:00~11:40', newTime: '2026-09-16 14:00~15:40', status: '已通过', createTime: '2026-09-08 09:00:00' },
  { id: 5003, courseName: '数据结构', teacher: '李思远', reason: '与监考安排冲突，申请调课', originalTime: '2026-09-14 08:00~09:40', newTime: '2026-09-18 19:00~20:40', status: '已驳回', createTime: '2026-09-07 09:00:00' },
  { id: 5004, courseName: '线性代数', teacher: '张伟', reason: '实验室临时停电，调整教学安排', originalTime: '2026-09-15 10:00~11:40', newTime: '2026-09-17 16:00~17:40', status: '待审核', createTime: '2026-09-10 09:00:00' },
  { id: 5005, courseName: '大学物理（一）', teacher: '赵明', reason: '教师病假，需安排补课', originalTime: '2026-09-16 08:00~09:40', newTime: '2026-09-22 19:00~20:40', status: '已通过', createTime: '2026-09-06 09:00:00' },
  { id: 5006, courseName: '程序设计基础（Python）', teacher: '李思远', reason: '机房预约冲突，调整上课教室与时间', originalTime: '2026-09-17 14:00~15:40', newTime: '2026-09-21 16:00~17:40', status: '待审核', createTime: '2026-09-11 09:00:00' },
  { id: 5007, courseName: '概率论与数理统计', teacher: '周敏', reason: '学校运动会占用教室，整体调课', originalTime: '2026-09-18 08:00~09:40', newTime: '2026-09-24 10:00~11:40', status: '已通过', createTime: '2026-09-05 09:00:00' },
]

/** 考核方式申报 - 待管理端审核（教师/课程申报拟采用的考核方式） */
const methodApplyList: MockRow[] = [
  { id: 80001, courseName: '高等数学（下）', className: '计算机2401班', teacher: '陈志强', methodName: '闭卷考试', reason: '期末统一闭卷考试，题型以计算与证明为主', status: '待审核', createTime: '2026-09-02 09:00:00' },
  { id: 80002, courseName: '大学英语（二）', className: '计算机2402班', teacher: '王丽华', methodName: '平时成绩+期末闭卷', reason: '听力与笔试结合，含平时口语表现评定', status: '待审核', createTime: '2026-09-03 09:00:00' },
  { id: 80003, courseName: '数据结构', className: '软件工程2401班', teacher: '李思远', methodName: '上机考试', reason: '算法与程序设计采用现场上机考核', status: '待审核', createTime: '2026-09-03 10:00:00' },
  { id: 80004, courseName: '概率论与数理统计', className: '软件工程2402班', teacher: '张伟', methodName: '闭卷考试', reason: '以闭卷笔试考核为主', status: '已通过', createTime: '2026-09-04 09:00:00' },
  { id: 80005, courseName: '大学物理（二）', className: '人工智能2401班', teacher: '赵明', methodName: '闭卷考试+实验报告', reason: '理论闭卷，实验部分按报告评定', status: '已通过', createTime: '2026-09-04 11:00:00' },
  { id: 80006, courseName: '马克思主义基本原理', className: '大数据2401班', teacher: '孙芳', methodName: '课程论文', reason: '以课程论文为主，结合课堂研讨表现', status: '已驳回', createTime: '2026-09-05 09:00:00' },
]

/** 专升本 - 报名审核 */
const applyList: MockRow[] = [
  { id: 6001, studentName: '陈晓', majorName: '计算机应用技术', targetSchool: '武汉工程大学', rank: 3, status: '待审核', createTime: '2026-09-03 14:00:00' },
  { id: 6002, studentName: '周涛', majorName: '软件技术', targetSchool: '三峡大学', rank: 8, status: '已通过', createTime: '2026-09-03 14:00:00' },
  { id: 6003, studentName: '孙丽', majorName: '电子商务', targetSchool: '江汉大学', rank: 15, status: '已驳回', createTime: '2026-09-04 14:00:00' },
  { id: 6004, studentName: '吴凯', majorName: '计算机网络技术', targetSchool: '湖北工业大学', rank: 21, status: '待审核', createTime: '2026-09-04 14:00:00' },
  { id: 6005, studentName: '郑雪', majorName: '数字媒体应用技术', targetSchool: '武汉纺织大学', rank: 12, status: '已通过', createTime: '2026-09-05 14:00:00' },
  { id: 6006, studentName: '何俊', majorName: '动漫制作技术', targetSchool: '长江大学', rank: 33, status: '待审核', createTime: '2026-09-05 14:00:00' },
]

/** 考勤管理 - 教师考勤日志 */
const attendanceLogList: MockRow[] = [
  { id: 7001, teacherName: '陈志强', courseDate: '2026-09-08', courseName: '高等数学（上）', checkStatus: '正常', attendanceRate: '96.5%', createTime: '2026-09-08 12:00:00' },
  { id: 7002, teacherName: '王丽华', courseDate: '2026-09-08', courseName: '大学英语（一）', checkStatus: '正常', attendanceRate: '98.2%', createTime: '2026-09-08 12:00:00' },
  { id: 7003, teacherName: '张伟', courseDate: '2026-09-09', courseName: '线性代数', checkStatus: '迟到', attendanceRate: '92.1%', createTime: '2026-09-09 12:00:00' },
  { id: 7004, teacherName: '李思远', courseDate: '2026-09-09', courseName: '程序设计基础（Python）', checkStatus: '正常', attendanceRate: '97.0%', createTime: '2026-09-09 12:00:00' },
  { id: 7005, teacherName: '赵明', courseDate: '2026-09-10', courseName: '大学物理（一）', checkStatus: '缺勤', attendanceRate: '0.0%', createTime: '2026-09-10 12:00:00' },
  { id: 7006, teacherName: '孙芳', courseDate: '2026-09-10', courseName: '马克思主义基本原理', checkStatus: '正常', attendanceRate: '99.1%', createTime: '2026-09-10 12:00:00' },
  { id: 7007, teacherName: '陈志强', courseDate: '2026-09-11', courseName: '高等数学（上）', checkStatus: '迟到', attendanceRate: '90.3%', createTime: '2026-09-11 12:00:00' },
  { id: 7008, teacherName: '王丽华', courseDate: '2026-09-11', courseName: '大学英语（二）', checkStatus: '正常', attendanceRate: '95.8%', createTime: '2026-09-11 12:00:00' },
]

/* ==================== 接口分发 ==================== */

/* ==================== 日期工具（教室申请 / 排考） ==================== */

/** Date 对象 → YYYY-MM-DD（本地时区） */
function fmtDateText(d: Date) {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 距当前 days 天后的日期文本（可为负数表示过去） */
function offsetDateText(days: number) {
  const d = new Date()
  d.setDate(d.getDate() + days)
  return fmtDateText(d)
}

/** 距当前 minutes 分钟前的时间文本（YYYY-MM-DD HH:mm:ss） */
function minutesAgoText(minutes: number) {
  const d = new Date(Date.now() - minutes * 60 * 1000)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/** 解析 YYYY-MM-DD / YYYY-MM-DD HH:mm:ss 为毫秒时间戳 */
function parseDateText(text: string) {
  return new Date(text.replace(/-/g, '/')).getTime()
}

/** 枚举 startText 至 endText（含两端）的每日日期 */
function eachDateBetween(startText: string, endText: string) {
  const dates: string[] = []
  const cursor = new Date(`${startText}T00:00:00`)
  const end = new Date(`${endText}T00:00:00`)
  while (cursor.getTime() <= end.getTime()) {
    dates.push(fmtDateText(cursor))
    cursor.setDate(cursor.getDate() + 1)
  }
  return dates
}

/* ==================== 教室资源种子数据（教室申请 / 自动排考共用） ==================== */

const classroomList: MockRow[] = [
  { id: 101, name: '第一教学楼 101', building: '第一教学楼', roomNo: '101', capacity: 120, type: '普通教室' },
  { id: 102, name: '第一教学楼 102', building: '第一教学楼', roomNo: '102', capacity: 80, type: '普通教室' },
  { id: 301, name: '第二教学楼 301', building: '第二教学楼', roomNo: '301', capacity: 90, type: '多媒体教室' },
  { id: 401, name: '3号教学楼 401', building: '3号教学楼', roomNo: '401', capacity: 150, type: '阶梯教室' },
  { id: 502, name: '实验楼 A202', building: '实验楼', roomNo: 'A202', capacity: 50, type: '机房' },
  { id: 601, name: '学术报告厅 A', building: '学术报告厅', roomNo: 'A', capacity: 400, type: '报告厅' },
]

/**
 * 教室申请记录（由学生/教师端发起，管理端只做审批）。
 * applyTime 相对当前时间生成，用于演示移动端“提交 30 分钟内不可取消”规则：
 * - 8001/8002：12、20 分钟前提交 → 取消按钮禁用；
 * - 8003：110 分钟前提交 → 允许取消。
 */
const classroomApplyList: MockRow[] = [
  { id: 8001, roomId: 101, roomName: '第一教学楼 101', className: '计算机2401班', applicant: '张伟', date: offsetDateText(2), timeSlot: '第3-4节 10:00~11:40', purpose: '考试', reason: '《高等数学（下）》单元测验', status: '待审核', applyTime: minutesAgoText(12) },
  { id: 8002, roomId: 301, roomName: '第二教学楼 301', className: '人工智能2402班', applicant: '刘敏', date: offsetDateText(1), timeSlot: '第5-6节 14:00~15:40', purpose: '讲座/会议', reason: 'AI 前沿技术专题讲座（约 80 人）', status: '待审核', applyTime: minutesAgoText(20) },
  { id: 8003, roomId: 502, roomName: '实验楼 A202', className: '软件工程2402班', applicant: '周涛', date: offsetDateText(5), timeSlot: '第1-2节 08:00~09:40', purpose: '日常教学', reason: '《程序设计基础》上机补课', status: '待审核', applyTime: minutesAgoText(110) },
  { id: 8004, roomId: 101, roomName: '第一教学楼 101', className: '大数据2401班', applicant: '陈晓', date: offsetDateText(3), timeSlot: '晚自习 19:00~20:40', purpose: '社团活动', reason: 'ACM 集训队周赛场地', status: '已通过', applyTime: minutesAgoText(60 * 26) },
  { id: 8005, roomId: 401, roomName: '3号教学楼 401', className: '网络2401班', applicant: '郑雪', date: offsetDateText(4), timeSlot: '第1-2节 08:00~09:40', purpose: '考试', reason: '《大学英语（二）》期中考试', status: '已驳回', applyTime: minutesAgoText(60 * 30) },
  { id: 8006, roomId: 601, roomName: '学术报告厅 A', className: '物联网2501班', applicant: '何俊', date: offsetDateText(8), timeSlot: '第7-8节 16:00~17:40', purpose: '讲座/会议', reason: '新生入学教育动员会', status: '已取消', applyTime: minutesAgoText(60 * 49) },
]

/** 教室固定占用（教学/考试）的确定性演示规则：按“日 + 教室号”取模，周末空闲 */
function fixedBusyFor(roomId: number, date: string): MockRow | null {
  const weekday = new Date(`${date}T00:00:00`).getDay()
  if (weekday === 0 || weekday === 6) return null
  const dayOfMonth = Number(date.slice(8, 10))
  const k = dayOfMonth + roomId
  if (k % 5 === 0) return { title: '考试占用', status: 'exam', reason: '教务处排考统一占用该教室' }
  if (k % 3 === 0) return { title: '日常教学占用', status: 'class', reason: '该日按教学计划排课使用该教室' }
  return null
}

/** 接口分发：按需在此补充各业务模块的模拟数据 */
function handleApi(ctx: MockContext) {
  const [path] = ctx.url.split('?')

  // 管理端审批动作：PUT /api/classroom/approve/{id} | PUT /api/classroom/reject/{id}
  const auditMatch = path.match(/^\/api\/classroom\/(approve|reject)\/(\d+)$/)
  if (auditMatch) {
    const action = auditMatch[1]
    const id = Number(auditMatch[2])
    const row = classroomApplyList.find((r) => r.id === id)
    if (!row) return fail('申请记录不存在')
    if (row.status !== '待审核') return fail('该申请已审批或已取消，请勿重复操作')
    row.status = action === 'approve' ? '已通过' : '已驳回'
    return ok(row)
  }

  // 各业务模块审核动作：PUT /api/{module}/approve/{id} | PUT /api/{module}/reject/{id}
  // 支持模块：graduation / course-adjust / college-upgrade / exam/method-audit
  const approveRoute = path.match(/^\/api\/(.+?)\/(approve|reject)\/(\d+)$/)
  if (approveRoute) {
    const base = approveRoute[1]
    const action = approveRoute[2]
    const id = Number(approveRoute[3])
    let list: MockRow[] | undefined
    let approveStatus = ''
    let rejectStatus = ''
    if (base === 'graduation') {
      list = studentList
      approveStatus = '通过'
      rejectStatus = '未通过'
    } else if (base === 'course-adjust') {
      list = myAdjustList
      approveStatus = '已通过'
      rejectStatus = '已驳回'
    } else if (base === 'college-upgrade') {
      list = applyList
      approveStatus = '已通过'
      rejectStatus = '已驳回'
    } else if (base === 'exam/method-audit') {
      list = methodApplyList
      approveStatus = '已通过'
      rejectStatus = '已驳回'
    } else {
      return fail('该接口尚未实现')
    }
    const row = list?.find((r) => r.id === id)
    if (!row) return fail('记录不存在或已被删除')
    if (row.status !== '待审核') return fail('该记录已审核，请勿重复操作')
    row.status = action === 'approve' ? approveStatus : rejectStatus
    return ok({ id: row.id, status: row.status }, action === 'approve' ? '审核通过' : '已驳回')
  }

  switch (path) {
    /* ---------------- 登录 / 学期 ---------------- */
    case '/api/auth/login': {
      const { username, password } = (ctx.body ?? {}) as {
        username?: string
        password?: string
      }
      if (!username || !password) {
        return fail('请输入用户名和密码')
      }
      return ok({
        token: 'fake-token',
        roles: ['admin'],
        name: '管理员',
      })
    }

    case '/api/auth/userinfo':
      return ok({
        id: 1,
        username: 'admin',
        name: '管理员',
        roles: ['admin'],
      })

    case '/api/auth/logout':
      return ok(null)

    case '/api/auth/roles':
      // 预留角色接口：联调后可按真实登录用户返回，如 ['admin'] 或 ['manager']
      return ok(['admin'])

    case '/api/term/current':
      return ok('2026-2027学年第二学期')

    /* ---------------- 工作台 ---------------- */
    case '/api/dashboard/statistics':
      return ok({
        courseCount: 128,
        classroomCount: 86,
        todayAdjustCount: 12,
        pendingGraduationCount: 7,
        pendingApprovals: [
          { id: 1, type: '调课审批', title: '高等数学（上）调课申请', applicant: '陈志强', applyTime: '2026-09-11 09:30', status: '待审核' },
          { id: 2, type: '调课审批', title: '数据结构调课申请', applicant: '李思远', applyTime: '2026-09-11 10:05', status: '待审核' },
          { id: 3, type: '教室申请审批', title: '3号教学楼 301 教室申请', applicant: '王丽华', applyTime: '2026-09-11 10:40', status: '待审核' },
          { id: 4, type: '教室申请审批', title: '实验楼 A202 机房使用申请', applicant: '张伟', applyTime: '2026-09-11 11:20', status: '待审核' },
          { id: 5, type: '调课审批', title: '大学物理（一）调课申请', applicant: '赵明', applyTime: '2026-09-11 14:00', status: '待审核' },
        ],
        /* ====== H5 示例页复用的历史字段 ====== */
        pendingAdjust: 6,
        pendingClassroom: 9,
        classroomUsageRate: 82,
        notices: [
          { id: 1, title: '关于 2026-2027 学年第一学期期中考试安排的通知', time: '2026-09-05 10:20' },
          { id: 2, title: '2026 年秋季教材征订计划已开放填报', time: '2026-09-03 16:45' },
          { id: 3, title: '本学期教室资源使用数据已更新', time: '2026-09-01 09:00' },
        ],
        upcomingExams: [
          { id: 1, examName: '期中考试', courseName: '高等数学（上）', examDate: '2026-10-20', status: '待编排' },
          { id: 2, examName: '阶段测验', courseName: '大学英语（一）', examDate: '2026-10-26', status: '待编排' },
          { id: 3, examName: '期中考试', courseName: '程序设计基础', examDate: '2026-11-02', status: '已完成' },
        ],
      })

    /* ---------------- 待办提醒（顶部铃铛） ---------------- */
    case '/api/todo/list':
      return ok([
        { id: 1, type: 'course-adjust', title: '高等数学（上）调课申请', applicant: '陈志强', applyTime: '2026-09-11 09:30', route: '/course-adjust/audit-list' },
        { id: 2, type: 'course-adjust', title: '数据结构调课申请', applicant: '李思远', applyTime: '2026-09-11 10:05', route: '/course-adjust/audit-list' },
        { id: 3, type: 'classroom', title: '3号教学楼 301 教室申请', applicant: '王丽华', applyTime: '2026-09-11 10:40', route: '/classroom' },
        { id: 4, type: 'classroom', title: '实验楼 A202 机房使用申请', applicant: '张伟', applyTime: '2026-09-11 11:20', route: '/classroom' },
      ])

    /* ---------------- 课程管理 ---------------- */
    case '/api/course/list': {
      const name = String(ctx.query?.name ?? '').trim()
      const term = String(ctx.query?.term ?? '')
      return ok(
        pageData(courseList, ctx, (row) => {
          const hitName = !name || String(row.name).includes(name)
          const hitTerm = !term || row.term === term
          return hitName && hitTerm
        }),
      )
    }

    case '/api/course/add': {
      const body = (ctx.body ?? {}) as MockRow
      const row: MockRow = {
        id: nextId(courseList),
        name: String(body.name ?? ''),
        term: String(body.term ?? ''),
        teacher: String(body.teacher ?? ''),
        credit: Number(body.credit) || 1,
        startDate: String(body.startDate ?? ''),
        createTime: nowText(),
      }
      courseList.unshift(row)
      return ok(row)
    }

    case '/api/course/edit': {
      const body = (ctx.body ?? {}) as MockRow
      const id = Number(body.id)
      const index = courseList.findIndex((row) => row.id === id)
      if (index === -1) {
        return fail('数据不存在或已被删除')
      }
      courseList[index] = {
        ...courseList[index],
        ...pick(body, ['name', 'term', 'teacher', 'credit', 'startDate']),
      }
      return ok(courseList[index])
    }

    case '/api/course/delete': {
      const removed = removeById(courseList, ctx)
      return removed ? ok(null) : fail('数据不存在或已被删除')
    }

    /* ---------------- 班级管理 ---------------- */
    case '/api/class/list': {
      const name = String(ctx.query?.name ?? '').trim()
      const grade = String(ctx.query?.grade ?? '')
      return ok(
        pageData(classList, ctx, (row) => {
          const hitName = !name || String(row.name).includes(name)
          const hitGrade = !grade || row.grade === grade
          return hitName && hitGrade
        }),
      )
    }

    case '/api/class/add': {
      const body = (ctx.body ?? {}) as MockRow
      const row: MockRow = {
        id: nextId(classList),
        name: String(body.name ?? ''),
        grade: String(body.grade ?? ''),
        major: String(body.major ?? ''),
        headTeacher: String(body.headTeacher ?? ''),
        studentCount: Number(body.studentCount) || 0,
        enrollDate: String(body.enrollDate ?? ''),
        createTime: nowText(),
      }
      classList.unshift(row)
      return ok(row)
    }

    case '/api/class/edit': {
      const body = (ctx.body ?? {}) as MockRow
      const id = Number(body.id)
      const index = classList.findIndex((row) => row.id === id)
      if (index === -1) {
        return fail('数据不存在或已被删除')
      }
      classList[index] = {
        ...classList[index],
        ...pick(body, ['name', 'grade', 'major', 'headTeacher', 'studentCount', 'enrollDate']),
      }
      return ok(classList[index])
    }

    case '/api/class/delete': {
      const removed = removeById(classList, ctx)
      return removed ? ok(null) : fail('数据不存在或已被删除')
    }

    /* ---------------- 新书入库 ---------------- */
    case '/api/textbook/stock-in/list': {
      const name = String(ctx.query?.name ?? '').trim()
      return ok(
        pageData(stockInList, ctx, (row) => !name || String(row.name).includes(name)),
      )
    }

    case '/api/textbook/stock-in/delete': {
      const removed = removeById(stockInList, ctx)
      return removed ? ok(null) : fail('数据不存在或已被删除')
    }

    /* ---------------- 毕业生管理 ---------------- */
    case '/api/graduation/student/list': {
      const name = String(ctx.query?.name ?? '').trim()
      const status = String(ctx.query?.status ?? '')
      return ok(
        pageData(studentList, ctx, (row) => {
          const hitName = !name || String(row.name).includes(name)
          const hitStatus = !status || row.status === status
          return hitName && hitStatus
        }),
      )
    }

    case '/api/graduation/student/delete': {
      const removed = removeById(studentList, ctx)
      return removed ? ok(null) : fail('数据不存在或已被删除')
    }

    /* ---------------- 我的调课 ---------------- */
    case '/api/course-adjust/my/list': {
      const courseName = String(ctx.query?.courseName ?? '').trim()
      const status = String(ctx.query?.status ?? '')
      return ok(
        pageData(myAdjustList, ctx, (row) => {
          const hitCourse = !courseName || String(row.courseName).includes(courseName)
          const hitStatus = !status || row.status === status
          return hitCourse && hitStatus
        }),
      )
    }

    case '/api/course-adjust/my/delete': {
      const removed = removeById(myAdjustList, ctx)
      return removed ? ok(null) : fail('数据不存在或已被删除')
    }

    /* ---------------- 专升本报名审核 ---------------- */
    case '/api/college-upgrade/apply/list': {
      const studentName = String(ctx.query?.studentName ?? '').trim()
      const status = String(ctx.query?.status ?? '')
      return ok(
        pageData(applyList, ctx, (row) => {
          const hitName = !studentName || String(row.studentName).includes(studentName)
          const hitStatus = !status || row.status === status
          return hitName && hitStatus
        }),
      )
    }

    case '/api/college-upgrade/apply/delete': {
      const removed = removeById(applyList, ctx)
      return removed ? ok(null) : fail('数据不存在或已被删除')
    }

    /* ---------------- 教师考勤日志 ---------------- */
    case '/api/attendance/log/list': {
      const teacherName = String(ctx.query?.teacherName ?? '').trim()
      const checkStatus = String(ctx.query?.checkStatus ?? '')
      return ok(
        pageData(attendanceLogList, ctx, (row) => {
          const hitTeacher = !teacherName || String(row.teacherName).includes(teacherName)
          const hitStatus = !checkStatus || row.checkStatus === checkStatus
          return hitTeacher && hitStatus
        }),
      )
    }

    case '/api/attendance/log/delete': {
      const removed = removeById(attendanceLogList, ctx)
      return removed ? ok(null) : fail('数据不存在或已被删除')
    }

    /* ---------------- 教室资源 / 教室申请 ---------------- */
    case '/api/classroom/list':
      return ok(classroomList)

    case '/api/classroom/occupancy': {
      const start = String(ctx.query?.start ?? offsetDateText(-15)).slice(0, 10)
      const end = String(ctx.query?.end ?? offsetDateText(20)).slice(0, 10)
      const events: MockRow[] = []
      // 1) 固定占用：为全校每间教室生成“考试/教学占用”（红）
      for (const room of classroomList) {
        for (const date of eachDateBetween(start, end)) {
          const fixed = fixedBusyFor(room.id, date)
          if (fixed) {
            events.push({
              id: `occ-${room.id}-${date}`,
              roomId: room.id,
              roomName: String(room.name),
              date,
              title: `${room.name} · ${fixed.title}`,
              status: fixed.status,
              timeSlot: '全天（按课表）',
              applicant: '教务处/系统',
              className: '',
              purpose: '',
              reason: fixed.reason,
            })
          }
        }
      }
      // 2) 师生端发起的申请：待审核（黄）/ 已通过（红）均视为占用，已驳回/已取消不占用
      for (const row of classroomApplyList) {
        if (row.status !== '待审核' && row.status !== '已通过') continue
        const applyDate = String(row.date)
        if (applyDate < start || applyDate > end) continue
        const isPending = row.status === '待审核'
        const room = classroomList.find((r) => r.id === Number(row.roomId))
        const roomName = String(room?.name ?? row.roomName ?? '')
        events.push({
          id: `apply-${row.id}`,
          roomId: Number(row.roomId),
          roomName,
          date: applyDate,
          title: `${roomName} · ${isPending ? '待审核' : '已通过'}（${row.applicant}）`,
          status: isPending ? 'pending' : 'approved',
          applicant: row.applicant,
          className: String(row.className ?? ''),
          timeSlot: String(row.timeSlot ?? ''),
          purpose: String(row.purpose ?? ''),
          reason: String(row.reason ?? ''),
        })
      }
      return ok(events)
    }

    case '/api/classroom/approval/list': {
      const status = String(ctx.query?.status ?? '')
      const list = classroomApplyList.filter((row) => {
        const auditable = row.status === '待审核' || row.status === '已通过' || row.status === '已驳回'
        return auditable && (!status || row.status === status)
      })
      return ok(list)
    }

    case '/api/classroom-apply/cancel': {
      const body = (ctx.body ?? {}) as MockRow
      const id = Number(body.id)
      const row = classroomApplyList.find((r) => r.id === id)
      if (!row) return fail('申请记录不存在')
      if (row.status !== '待审核') return fail('仅待审核状态的申请可以取消')
      if (Date.now() - parseDateText(String(row.applyTime)) < 30 * 60 * 1000) {
        return fail('申请提交后 30 分钟内不可取消')
      }
      row.status = '已取消'
      return ok({ id: row.id, status: row.status })
    }

    /* ---------------- 师生端发起申请（本管理端不展示该表单；Mock 模拟后端供师生端联调） ---------------- */
    case '/api/classroom/apply': {
      const body = (ctx.body ?? {}) as MockRow
      const room = classroomList.find((r) => r.id === Number(body.roomId))
      if (!room) return fail('所选教室不存在')
      const date = String(body.date ?? '')
      const reason = String(body.reason ?? '').trim()
      if (!date || !reason) return fail('请填写完整申请信息')
      const row: MockRow = {
        id: nextId(classroomApplyList),
        roomId: room.id,
        roomName: String(room.name),
        className: String(body.className ?? ''),
        applicant: String(body.applicant ?? '学生'),
        date,
        timeSlot: String(body.timeSlot ?? ''),
        purpose: String(body.purpose ?? '其他'),
        reason,
        status: '待审核',
        applyTime: nowText(),
      }
      classroomApplyList.unshift(row)
      return ok(row)
    }

    case '/api/classroom-apply/my/list': {
      const status = String(ctx.query?.status ?? '')
      const roomName = String(ctx.query?.roomName ?? '').trim()
      return ok(
        pageData(classroomApplyList, ctx, (row) => {
          const hitStatus = !status || row.status === status
          const hitRoom = !roomName || String(row.roomName).includes(roomName)
          return hitStatus && hitRoom
        }),
      )
    }

    /* ---------------- 毕业生管理（审核） ---------------- */
    case '/api/graduation/list': {
      const name = String(ctx.query?.name ?? '').trim()
      const studentNo = String(ctx.query?.studentNo ?? '').trim()
      const status = String(ctx.query?.status ?? '')
      const rows = studentList.filter((row) => {
        const hitName = !name || String(row.name).includes(name)
        const hitNo = !studentNo || String(row.studentNo).includes(studentNo)
        const hitStatus = !status || row.status === status
        return hitName && hitNo && hitStatus
      })
      return ok(rows)
    }

    /* ---------------- 调课审批（管理端） ---------------- */
    case '/api/course-adjust/audit/list': {
      const courseName = String(ctx.query?.courseName ?? '').trim()
      const teacher = String(ctx.query?.teacher ?? '').trim()
      const status = String(ctx.query?.status ?? '')
      const rows = myAdjustList.filter((row) => {
        const hitCourse = !courseName || String(row.courseName).includes(courseName)
        const hitTeacher = !teacher || String(row.teacher).includes(teacher)
        const hitStatus = !status || row.status === status
        return hitCourse && hitTeacher && hitStatus
      })
      return ok(rows)
    }

    /* ---------------- 专升本报名审核 ---------------- */
    case '/api/college-upgrade/audit/list': {
      const studentName = String(ctx.query?.studentName ?? '').trim()
      const status = String(ctx.query?.status ?? '')
      const rows = applyList.filter((row) => {
        const hitName = !studentName || String(row.studentName).includes(studentName)
        const hitStatus = !status || row.status === status
        return hitName && hitStatus
      })
      return ok(rows)
    }

    /* ---------------- 考核方式申报审核（管理端） ---------------- */
    case '/api/exam/method-audit/list': {
      const courseName = String(ctx.query?.courseName ?? '').trim()
      const teacher = String(ctx.query?.teacher ?? '').trim()
      const status = String(ctx.query?.status ?? '')
      const rows = methodApplyList.filter((row) => {
        const hitCourse = !courseName || String(row.courseName).includes(courseName)
        const hitTeacher = !teacher || String(row.teacher).includes(teacher)
        const hitStatus = !status || row.status === status
        return hitCourse && hitTeacher && hitStatus
      })
      return ok(rows)
    }

    /* ---------------- 考务管理 - 一键自动排考 ---------------- */
    case '/api/exam/auto-arrange': {
      const body = (ctx.body ?? {}) as MockRow
      const term = String(body.term ?? '2026-2027学年第二学期')
      const grade = String(body.grade ?? '2024级')
      const items: MockRow[] = [
        { id: 9101, courseName: '高等数学（下）', className: '计算机2401班', studentCount: 45, examDate: '2026-09-21', session: '第1场 08:30~10:10', examRoom: '第一教学楼 101', invigilators: ['张伟', '陈志强'], conflict: false, conflictReason: '' },
        { id: 9102, courseName: '大学英语（二）', className: '计算机2402班', studentCount: 44, examDate: '2026-09-21', session: '第1场 08:30~10:10', examRoom: '第一教学楼 102', invigilators: ['王丽华', '张伟'], conflict: true, conflictReason: '监考员「张伟」同时被分配到第1场的「高等数学（下）」（第一教学楼 101）与「大学英语（二）」（第一教学楼 102）' },
        { id: 9103, courseName: '数据结构', className: '软件工程2401班', studentCount: 42, examDate: '2026-09-21', session: '第2场 10:30~12:10', examRoom: '第二教学楼 301', invigilators: ['李思远', '刘敏'], conflict: false, conflictReason: '' },
        { id: 9104, courseName: '概率论与数理统计', className: '软件工程2402班', studentCount: 41, examDate: '2026-09-21', session: '第2场 10:30~12:10', examRoom: '3号教学楼 401', invigilators: ['赵明', '孙芳'], conflict: false, conflictReason: '' },
        { id: 9105, courseName: '大学物理（二）', className: '人工智能2401班', studentCount: 40, examDate: '2026-09-22', session: '第1场 08:30~10:10', examRoom: '第二教学楼 301', invigilators: ['陈志强', '周敏'], conflict: false, conflictReason: '' },
        { id: 9106, courseName: '高等数学（下）重修', className: '大数据2401班', studentCount: 43, examDate: '2026-09-21', session: '第2场 10:30~12:10', examRoom: '3号教学楼 401', invigilators: ['郑强', '何俊'], conflict: true, conflictReason: '教室「3号教学楼 401」在第2场被「概率论与数理统计」与「高等数学（下）重修」同时占用' },
      ]
      const conflictItems = items.filter((row) => row.conflict)
      return ok({
        term,
        grade,
        summary: {
          examName: `${term} ${grade} 期末考试`,
          examCount: items.length,
          roomCount: new Set(items.map((row) => String(row.examRoom))).size,
          normalCount: items.length - conflictItems.length,
          conflictCount: conflictItems.length,
        },
        items,
        generatedAt: nowText(),
      })
    }

    default:
      // 未配置的接口：返回请求信息回显，方便观察实际请求内容
      return ok(
        {
          path,
          query: ctx.query ?? null,
          body: ctx.body ?? null,
        },
        'mock: 该接口尚未配置模拟数据',
      )
  }
}

const methods = ['get', 'post', 'put', 'delete', 'patch'] as const

/** 对每种 HTTP 方法注册同一个兜底处理器（vite-plugin-mock@3 的 url 仅支持字符串模板） */
const mockApi = methods.map((method) => ({
  url: '/api/:rest*',
  method,
  timeout: 200,
  response: (ctx: MockContext) => handleApi(ctx),
})) as unknown as MockMethod[]

export default mockApi
