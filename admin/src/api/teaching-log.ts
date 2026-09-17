import { http } from '@/utils/request'

export type Id = string

interface PageResult<T> {
  records: T[]
  total: number
}

export interface TeacherItem {
  id: Id
  teacherNo: string
  name: string
}

export interface CourseItem {
  id: Id
  courseCode: string
  name: string
}

export interface ClassItem {
  id: Id
  name: string
  grade?: string
}

export interface TeachingLogRaw {
  id: Id

  teacherId: Id

  courseId: Id

  classId: Id

  teachingDate: string

  content: string

  homework?: string

  createTime?: string
}

export interface TeachingLogItem {
  id: Id

  teacherId: Id
  teacherName: string

  courseId: Id
  courseName: string

  classId: Id
  className: string

  teachingDate: string

  content: string

  homework?: string

  createTime?: string
}

/**
 * 教师列表
 */
export function getTeachers() {
  return http.get<PageResult<TeacherItem>>(
    '/teachers',
    {
      pageNo: 1,
      pageSize: 1000,
    },
  )
}

/**
 * 查询教师某周教学日志
 */
export async function getTeacherWeeklyLogs(
  teacherId: string,
  date: string,
): Promise<TeachingLogItem[]> {

  const [
    logs,
    coursesPage,
    classesPage,
    teachersPage,
  ] = await Promise.all([
    http.get<TeachingLogRaw[]>(
      `/teaching-logs/teachers/${teacherId}/weekly`,
      {
        date,
      },
    ),

    http.get<PageResult<CourseItem>>(
      '/courses',
      {
        pageNo: 1,
        pageSize: 1000,
      },
    ),

    http.get<PageResult<ClassItem>>(
      '/classes',
      {
        pageNo: 1,
        pageSize: 1000,
      },
    ),

    getTeachers(),
  ])

  const courseMap = new Map(
    coursesPage.records.map(
      item => [
        String(item.id),
        item,
      ],
    ),
  )

  const classMap = new Map(
    classesPage.records.map(
      item => [
        String(item.id),
        item,
      ],
    ),
  )

  const teacherMap = new Map(
    teachersPage.records.map(
      item => [
        String(item.id),
        item,
      ],
    ),
  )

  return logs.map(
    item => ({
      id:
        String(item.id),

      teacherId:
        String(item.teacherId),

      teacherName:
        teacherMap.get(
          String(item.teacherId),
        )?.name
        ?? '未知教师',

      courseId:
        String(item.courseId),

      courseName:
        courseMap.get(
          String(item.courseId),
        )?.name
        ?? '未知课程',

      classId:
        String(item.classId),

      className:
        classMap.get(
          String(item.classId),
        )?.name
        ?? '未知班级',

      teachingDate:
        item.teachingDate,

      content:
        item.content,

      homework:
        item.homework,

      createTime:
        item.createTime,
    }),
  )
}
