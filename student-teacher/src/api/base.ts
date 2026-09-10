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
 * 后端 GET /api/class/page?pageNo&pageSize&keyword&grade&majorId
 */
export function pageClasses(
  query: PageQuery & { keyword?: string; grade?: string; majorId?: number },
): Promise<PageResult<ClassInfo>> {
  return getPage<ClassInfo>('/class/page', pageParams(query))
}

/**
 * 查询某班级的学生
 * 后端 GET /api/student/list-by-class/{classId}
 */
export async function listStudentsByClass(classId: number): Promise<Student[]> {
  const data = await http.get<unknown>(`/student/list-by-class/${classId}`)
  return normalizeList<Student>(data)
}

/**
 * 分页查询课程
 * 后端 GET /api/course/page?pageNo&pageSize&keyword&type&teachingGroupId
 */
export function pageCourses(
  query: PageQuery & { keyword?: string; type?: string; teachingGroupId?: number },
): Promise<PageResult<Course>> {
  return getPage<Course>('/course/page', pageParams(query))
}

/**
 * 教师：我的班级（用于考勤录入选班级）
 * ⚠️ 缺口：后端没有"教师任教班级"接口（也没有任课关系表），
 * 当前由 Mock 提供 GET /api/teacher/my-classes?teacherId，待后端补。
 */
export async function listMyClasses(teacherId: number): Promise<ClassInfo[]> {
  const data = await http.get<unknown>('/teacher/my-classes', { teacherId })
  return normalizeList<ClassInfo>(data)
}

/**
 * 教师：我的任教课程（用于教学日志、考核方式申报选课程）
 * ⚠️ 缺口：同上，后端无接口，当前由 Mock 提供 GET /api/teacher/my-courses?teacherId，待后端补。
 */
export async function listMyCourses(teacherId: number): Promise<Course[]> {
  const data = await http.get<unknown>('/teacher/my-courses', { teacherId })
  return normalizeList<Course>(data)
}
