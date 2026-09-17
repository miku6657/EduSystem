import {
  http,
  normalizeList,
} from '@/utils/request'

import type {
  DbId,
} from '@/types/user'

export interface TeacherAttendanceRecord {
  id?: DbId

  teacherId: DbId

  attendanceDate: string

  status: string

  checkTime?: string
}

export interface TeacherAttendanceStat {
  total: number

  checked: number

  absent: number
}

/**
 * 教师签到
 *
 * 真实后端：
 *
 * POST
 * /api/teacher-attendances/{teacherId}/check-in
 */
export function checkIn(
  teacherId: DbId,
) {
  return http.post<null>(
    `/teacher-attendances/${teacherId}/check-in`,
  )
}

/**
 * 查询某天全部教师签到
 *
 * 真实后端：
 *
 * GET
 * /api/teacher-attendances?date=...
 */
export async function listByDate(
  date: string,
): Promise<
  TeacherAttendanceRecord[]
> {
  const data =
    await http.get<unknown>(
      '/teacher-attendances',
      {
        date,
      },
    )

  return normalizeList<
    TeacherAttendanceRecord
  >(data)
}

/**
 * 查询某天教师签到统计
 *
 * 真实后端：
 *
 * GET
 * /api/teacher-attendances/statistics?date=...
 */
export function statByDate(
  date: string,
) {
  return http.get<
    TeacherAttendanceStat
  >(
    '/teacher-attendances/statistics',
    {
      date,
    },
  )
}

/**
 * 后端没有按照教师查询的接口。
 *
 * 所以：
 *
 * 按日期查询所有教师
 * ↓
 * 根据当前teacherId筛本人
 */
export async function getMyAttendanceByDate(
  teacherId: DbId,
  date: string,
): Promise<
  TeacherAttendanceRecord | null
> {
  const records =
    await listByDate(
      date,
    )

  return (
    records.find(
      (item) =>
        String(
          item.teacherId,
        )
        ===
        String(
          teacherId,
        ),
    )
    ?? null
  )
}

/**
 * 查询本人最近若干天。
 */
export async function listMyRecentAttendance(
  teacherId: DbId,
  dates: string[],
): Promise<
  TeacherAttendanceRecord[]
> {
  const results =
    await Promise.all(
      dates.map(
        (date) =>
          getMyAttendanceByDate(
            teacherId,
            date,
          ),
      ),
    )

  return results.filter(
    (
      item,
    ): item is TeacherAttendanceRecord =>
      item !== null,
  )
}