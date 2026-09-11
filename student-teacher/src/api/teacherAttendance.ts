import { http, normalizeList } from '@/utils/request'

/** 教师考勤记录（teacher_attendance；status 为中文：正常/迟到/缺勤） */
export interface TeacherAttendanceRecord {
  id?: number
  teacherId: number
  /** YYYY-MM-DD */
  attendanceDate: string
  status: string
  /** 签到时间 */
  checkTime?: string
  /** 展示用扩展字段（后端实体没有，Mock 会带） */
  teacherName?: string
}

/** 某天教师出勤统计（后端返回 Map） */
export interface TeacherAttendanceStat {
  /** 教师总数 */
  total?: number
  /** 已签到人数 */
  checked?: number
  /** 未签到人数 */
  unchecked?: number
  [key: string]: unknown
}

/**
 * 教师：签到（一天一次）
 * 后端 POST /api/teacher-attendance/check-in/{teacherId}
 */
export function checkIn(teacherId: number) {
  return http.post<null>(`/teacher-attendance/check-in/${teacherId}`)
}

/**
 * 查询某天全部教师考勤记录
 * 后端 GET /api/teacher-attendance/list-by-date?date
 */
export async function listByDate(date: string): Promise<TeacherAttendanceRecord[]> {
  const data = await http.get<unknown>('/teacher-attendance/list-by-date', { date })
  return normalizeList<TeacherAttendanceRecord>(data)
}

/**
 * 某天教师出勤统计
 * 后端 GET /api/teacher-attendance/stat-by-date?date
 */
export function statByDate(date: string) {
  return http.get<TeacherAttendanceStat>('/teacher-attendance/stat-by-date', { date })
}
