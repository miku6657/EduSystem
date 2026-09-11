import { http, normalizeList } from '@/utils/request'

/** 学生考勤记录（student_attendance；status 为中文：正常/迟到/缺勤/请假） */
export interface StudentAttendanceRecord {
  id?: number
  studentId: number
  courseId: number
  /** YYYY-MM-DD */
  attendanceDate: string
  status: string
  /** 展示用扩展字段（后端实体没有，Mock 会带） */
  courseName?: string
  studentName?: string
  studentNo?: string
}

/** 班级周考勤报表行（后端返回 List<Map>，字段以实际为准，故用松散类型） */
export interface WeeklyReportRow {
  studentId?: number
  studentName?: string
  studentNo?: string
  normal?: number
  late?: number
  absent?: number
  leave?: number
  /** 出勤率，0~100 */
  rate?: number
  [key: string]: unknown
}

/**
 * 学生：查询本人在日期区间内的考勤
 * 后端 GET /api/student-attendance/list-by-student?studentId&startDate&endDate
 */
export async function listMyAttendance(
  studentId: number,
  startDate: string,
  endDate: string,
): Promise<StudentAttendanceRecord[]> {
  const data = await http.get<unknown>('/student-attendance/list-by-student', {
    studentId,
    startDate,
    endDate,
  })
  return normalizeList<StudentAttendanceRecord>(data)
}

/**
 * 教师：批量录入学生考勤（同学生同课程同日期已有记录时覆盖更新）
 * 后端 POST /api/student-attendance/record，body 为记录数组
 */
export function recordAttendance(records: StudentAttendanceRecord[]) {
  return http.post<null>('/student-attendance/record', records)
}

/**
 * 教师：班级学生周考勤报表（date 传该周任意一天）
 * 后端 GET /api/student-attendance/weekly-report?classId&date
 */
export async function getWeeklyReport(classId: number, date: string): Promise<WeeklyReportRow[]> {
  const data = await http.get<unknown>('/student-attendance/weekly-report', { classId, date })
  return normalizeList<WeeklyReportRow>(data)
}
