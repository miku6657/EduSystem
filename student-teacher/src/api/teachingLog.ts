import {
  http,
  normalizeList,
} from '@/utils/request'

/**
 * 后端 teaching_log
 */
export interface TeachingLog {
  id?: string

  teacherId: string

  courseId: string

  classId: string

  /**
   * YYYY-MM-DD
   */
  teachingDate: string

  content: string

  homework?: string

  createTime?: string

  updateTime?: string

  /**
   * 前端关联展示字段
   */
  courseName?: string

  courseCode?: string

  className?: string
}

export interface TeachingCourse {
  id: string

  courseCode: string

  name: string

  credit?: number

  type?: string
}

export interface TeachingClass {
  id: string

  name: string

  grade?: string

  studentCount?: number

  counselor?: string
}

/**
 * 查询全部课程。
 *
 * 使用已有后端：
 * GET /api/courses
 */
export async function listTeachingCourses():
  Promise<TeachingCourse[]> {

  const data =
    await http.get<unknown>(
      '/courses',
      {
        pageNo: 1,
        pageSize: 1000,
      },
    )

  return normalizeList<
    TeachingCourse
  >(data).map(
    (item) => ({
      ...item,

      id:
        String(
          item.id,
        ),
    }),
  )
}

/**
 * 查询全部班级。
 *
 * 使用已有后端：
 * GET /api/classes
 */
export async function listTeachingClasses():
  Promise<TeachingClass[]> {

  const data =
    await http.get<unknown>(
      '/classes',
      {
        pageNo: 1,
        pageSize: 1000,
      },
    )

  return normalizeList<
    TeachingClass
  >(data).map(
    (item) => ({
      ...item,

      id:
        String(
          item.id,
        ),
    }),
  )
}

/**
 * 新增教学日志。
 *
 * 真实后端：
 *
 * POST /api/teaching-logs
 */
export function addTeachingLog(
  log: {
    teacherId: string

    courseId: string

    classId: string

    teachingDate: string

    content: string

    homework?: string
  },
) {

  return http.post<null>(
    '/teaching-logs',
    log,
  )
}

/**
 * 当前教师某周教学日志。
 *
 * 真实后端：
 *
 * GET
 * /api/teaching-logs/teachers/{teacherId}/weekly
 * ?date=2026-09-17
 *
 * 后端只返回ID，
 * 前端再关联课程名和班级名。
 */
export async function listMyTeachingLogs(
  teacherId: string,
  date: string,
): Promise<TeachingLog[]> {

  const [
    logData,
    courses,
    classes,
  ] =
    await Promise.all([
      http.get<unknown>(
        `/teaching-logs/teachers/${teacherId}/weekly`,
        {
          date,
        },
      ),

      listTeachingCourses(),

      listTeachingClasses(),
    ])

  const logs =
    normalizeList<
      TeachingLog
    >(logData)

  const courseMap =
    new Map(
      courses.map(
        (item) => [
          String(item.id),
          item,
        ],
      ),
    )

  const classMap =
    new Map(
      classes.map(
        (item) => [
          String(item.id),
          item,
        ],
      ),
    )

  return logs.map(
    (item) => {

      const course =
        courseMap.get(
          String(
            item.courseId,
          ),
        )

      const classInfo =
        classMap.get(
          String(
            item.classId,
          ),
        )

      return {
        ...item,

        id:
          item.id === null
          || item.id === undefined
            ? undefined
            : String(
                item.id,
              ),

        teacherId:
          String(
            item.teacherId,
          ),

        courseId:
          String(
            item.courseId,
          ),

        classId:
          String(
            item.classId,
          ),

        courseName:
          course?.name,

        courseCode:
          course?.courseCode,

        className:
          classInfo?.name,
      }
    },
  )
}

/**
 * 某班级某周教学日志。
 *
 * 后端已有，暂时本页面不用。
 */
export async function listClassTeachingLogs(
  classId: string,
  date: string,
): Promise<TeachingLog[]> {

  const data =
    await http.get<unknown>(
      `/teaching-logs/classes/${classId}/weekly`,
      {
        date,
      },
    )

  return normalizeList<
    TeachingLog
  >(data).map(
    (item) => ({
      ...item,

      id:
        item.id === null
        || item.id === undefined
          ? undefined
          : String(
              item.id,
            ),

      teacherId:
        String(
          item.teacherId,
        ),

      courseId:
        String(
          item.courseId,
        ),

      classId:
        String(
          item.classId,
        ),
    }),
  )
}
