import { getPage, http, normalizeList, pageParams } from '@/utils/request'
import type { PageQuery, PageResult } from '@/types/api'

/** 班级信息（base_class） */
export interface ClassInfo {
  id?: number
  name: string
  grade?: string
  majorId?: number
  headTeacherId?: number
  studentCount?: number
  enrollDate?: string
}

/** 学生信息（base_student） */
export interface Student {
  id?: number
  studentNo: string
  name: string
  gender?: string
  classId?: number
  phone?: string
  status?: string
}

/** 课程信息（base_course） */
export interface Course {
  id?: number
  courseCode: string
  name: string
  credit?: number
  type?: string
  teachingGroupId?: number
}

/**
 * 分页查询班级
 * 后端 GET /api/classes?pageNo&pageSize&keyword&grade&majorId
 */
export function pageClasses(
  query: PageQuery & { keyword?: string; grade?: string; majorId?: number },
): Promise<PageResult<ClassInfo>> {
  return getPage<ClassInfo>('/classes', pageParams(query))
}

/**
 * 查询全部班级。
 *
 * 真实后端：
 * GET /api/classes?pageNo=1&pageSize=500
 */
export async function listClasses():
  Promise<ClassInfo[]> {

  const data =
    await http.get<unknown>(
      '/classes',
      {
        pageNo: 1,
        pageSize: 500,
      },
    )

  return normalizeList<ClassInfo>(
    data,
  )
}

/**
 * 查询某个班级的学生。
 *
 * 真实后端：
 * GET /api/students?classId=xxx
 */
export async function listStudentsByClass(
  classId: string | number,
): Promise<Student[]> {

  const data =
    await http.get<unknown>(
      '/students',
      {
        classId,
      },
    )

  return normalizeList<Student>(
    data,
  )
}

/**
 * 分页查询课程
 * 后端 GET /api/courses?pageNo&pageSize&keyword&type&teachingGroupId
 */
export function pageCourses(
  query: PageQuery & { keyword?: string; type?: string; teachingGroupId?: number },
): Promise<PageResult<Course>> {
  return getPage<Course>('/courses', pageParams(query))
}

/**
 * 教师：任教的班级（用于考勤点名选班级）
 * 后端 GET /api/teachers/{id}/classes
 */
export async function listMyClasses(teacherId: number): Promise<ClassInfo[]> {
  const data = await http.get<unknown>(`/teachers/${teacherId}/classes`)
  return normalizeList<ClassInfo>(data)
}

/**
 * 教师：任教的课程（用于教学日志、考核方式申报选课程）
 * 后端 GET /api/teachers/{id}/courses
 */
export async function listMyCourses(teacherId: number): Promise<Course[]> {
  const data = await http.get<unknown>(`/teachers/${teacherId}/courses`)
  return normalizeList<Course>(data)
}
