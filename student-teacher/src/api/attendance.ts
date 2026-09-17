import {
  http,
  normalizeList,
} from '@/utils/request'

export interface StudentAttendanceRecord {
  id?: string

  studentId: string

  courseId: string

  attendanceDate: string

  /**
   * 正常 / 迟到 / 缺勤 / 请假
   */
  status: string

  /**
   * 前端展示字段
   */
  courseName?: string
  courseCode?: string
}

export interface AttendanceClass {
  id: string
  name: string
  grade?: string
}

export interface AttendanceCourse {
  id: string
  courseCode: string
  name: string
}

export interface AttendanceStudent {
  id: string
  studentNo: string
  name: string
  classId?: string
}

interface WeeklyReportRaw {
  studentNo?: string
  name?: string
  total?: number
  normal?: number
  absent?: number
}

export interface WeeklyReportRow {
  studentNo?: string
  studentName?: string

  total: number
  normal: number

  /**
   * 后端的 absent 实际是：
   * total - normal
   *
   * 所以前端叫 abnormal 更准确。
   */
  abnormal: number

  rate?: number | null
}

/**
 * 教师：所有班级
 */
export async function listAttendanceClasses():
  Promise<AttendanceClass[]> {

  const data =
    await http.get<unknown>(
      '/classes',
      {
        pageNo: 1,
        pageSize: 1000,
      },
    )

  return normalizeList<AttendanceClass>(
    data,
  ).map(
    item => ({
      ...item,
      id: String(item.id),
    }),
  )
}

/**
 * 教师：所有课程
 */
export async function listAttendanceCourses():
  Promise<AttendanceCourse[]> {

  const data =
    await http.get<unknown>(
      '/courses',
      {
        pageNo: 1,
        pageSize: 1000,
      },
    )

  return normalizeList<AttendanceCourse>(
    data,
  ).map(
    item => ({
      ...item,
      id: String(item.id),
    }),
  )
}

/**
 * 教师：某班级学生
 */
export async function listAttendanceStudents(
  classId: string,
): Promise<AttendanceStudent[]> {

  const data =
    await http.get<unknown>(
      '/students',
      {
        classId,
      },
    )

  return normalizeList<AttendanceStudent>(
    data,
  ).map(
    item => ({
      ...item,

      id:
        String(item.id),

      classId:
        item.classId === undefined
        || item.classId === null
          ? undefined
          : String(item.classId),
    }),
  )
}

/**
 * 教师：提交学生考勤。
 *
 * 学生端绝对不调用这个函数。
 */
export function recordAttendance(
  records:
    StudentAttendanceRecord[],
) {

  return http.post<null>(
    '/student-attendances',
    records,
  )
}

/**
 * 学生：只查看自己的考勤。
 */
export async function listMyAttendance(
  studentId: string,
  startDate: string,
  endDate: string,
): Promise<StudentAttendanceRecord[]> {

  const [
    attendanceData,
    courseData,
  ] =
    await Promise.all([
      http.get<unknown>(
        `/student-attendances/students/${studentId}`,
        {
          startDate,
          endDate,
        },
      ),

      http.get<unknown>(
        '/courses',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),
    ])

  const records =
    normalizeList<StudentAttendanceRecord>(
      attendanceData,
    )

  const courses =
    normalizeList<AttendanceCourse>(
      courseData,
    )

  const courseMap =
    new Map(
      courses.map(
        course => [
          String(course.id),
          course,
        ],
      ),
    )

  return records.map(
    record => {

      const course =
        courseMap.get(
          String(record.courseId),
        )

      return {
        ...record,

        id:
          record.id === null
          || record.id === undefined
            ? undefined
            : String(record.id),

        studentId:
          String(record.studentId),

        courseId:
          String(record.courseId),

        courseName:
          course?.name,

        courseCode:
          course?.courseCode,
      }
    },
  )
}

/**
 * 教师：班级周报。
 */
export async function getWeeklyReport(
  classId: string,
  date: string,
): Promise<WeeklyReportRow[]> {

  const data =
    await http.get<unknown>(
      '/student-attendances/weekly-report',
      {
        classId,
        date,
      },
    )

  const rows =
    normalizeList<WeeklyReportRaw>(
      data,
    )

  return rows.map(
    row => {

      const total =
        Number(row.total ?? 0)

      const normal =
        Number(row.normal ?? 0)

      const abnormal =
        Number(row.absent ?? 0)

      return {
        studentNo:
          row.studentNo,

        studentName:
          row.name,

        total,

        normal,

        abnormal,

        rate:
          total > 0
            ? Math.round(
                normal
                / total
                * 100,
              )
            : null,
      }
    },
  )
}
