import { http } from '@/utils/request'

import type {
  DbId,
  StudentProfile,
  TeacherProfile,
} from '@/types/user'

/**
 * 后端 Student Entity
 */
interface StudentRaw {
  id:
    | string
    | number

  studentNo?: string

  name: string

  gender?: string

  classId?:
    | string
    | number
    | null

  phone?: string

  status?: string
}

/**
 * 后端 Teacher Entity
 */
interface TeacherRaw {
  id:
    | string
    | number

  teacherNo?: string

  name: string

  gender?: string

  type?: string

  phone?: string

  departmentId?:
    | string
    | number
    | null

  teachingGroupId?:
    | string
    | number
    | null
}

/**
 * 根据真实学生业务ID
 * 查询学生档案。
 *
 * businessId
 * =
 * base_student.id
 */
export async function getStudentById(
  id: DbId,
): Promise<StudentProfile> {

  const raw =
    await http.get<StudentRaw>(
      `/students/${id}`,
    )

  return {
    id:
      String(
        raw.id,
      ),

    studentNo:
      raw.studentNo,

    name:
      raw.name,

    gender:
      raw.gender,

    classId:
      raw.classId === null
      || raw.classId === undefined
        ? undefined
        : String(
            raw.classId,
          ),

    phone:
      raw.phone,

    status:
      raw.status,
  }
}

/**
 * 根据真实教师业务ID
 * 查询教师档案。
 *
 * businessId
 * =
 * base_teacher.id
 */
export async function getTeacherById(
  id: DbId,
): Promise<TeacherProfile> {

  const raw =
    await http.get<TeacherRaw>(
      `/teachers/${id}`,
    )

  return {
    id:
      String(
        raw.id,
      ),

    teacherNo:
      raw.teacherNo,

    name:
      raw.name,

    gender:
      raw.gender,

    departmentId:
      raw.departmentId === null
      || raw.departmentId === undefined
        ? undefined
        : String(
            raw.departmentId,
          ),

    teachingGroupId:
      raw.teachingGroupId === null
      || raw.teachingGroupId === undefined
        ? undefined
        : String(
            raw.teachingGroupId,
          ),

    type:
      raw.type,

    teacherType:
      raw.type,

    phone:
      raw.phone,
  }
}
