import {
  getPage,
  http,
  normalizeList,
  pageParams,
} from '@/utils/request'

import type {
  PageQuery,
  PageResult,
} from '@/types/api'

/**
 * 教室
 *
 * base_classroom
 */
export interface Classroom {
  /**
   * BIGINT / Snowflake ID
   * 前端统一使用 string
   */
  id?: string

  roomNo: string

  campusId?: string

  type?: string

  area?: number

  capacity?: number

  status?: string
}

/**
 * 师生端页面使用的教室申请结构。
 */
export interface ClassroomApply {
  id?: string

  roomId: string

  roomName?: string

  /**
   * CAS登录账号。
   *
   * 由后端自动写入，
   * 前端提交时不传。
   */
  applicant?: string

  className?: string

  /**
   * YYYY-MM-DD
   */
  applyDate: string

  timeSlot: string

  purpose?: string

  reason?: string

  /**
   * 待审核
   * 已通过
   * 已驳回
   * 已取消
   */
  status?: string

  /**
   * 申请提交时间
   */
  createTime?: string
}

/**
 * 后端 ClassroomApplyVO 原始结构。
 *
 * 注意：
 *
 * 后端字段叫：
 *
 * date
 * applyTime
 *
 * 师生端页面使用：
 *
 * applyDate
 * createTime
 */
interface ClassroomApplyRaw {
  id:
    | string
    | number

  roomId:
    | string
    | number

  roomName?: string

  applicant?: string

  className?: string

  date: string

  timeSlot: string

  purpose?: string

  reason?: string

  status?: string

  applyTime?: string
}

/**
 * 提交申请参数。
 *
 * applicant不允许前端传。
 */
export interface CreateClassroomApplyParams {
  roomId: string

  applyDate: string

  timeSlot: string

  className?: string

  purpose?: string

  reason: string
}

/**
 * 分页查询教室。
 *
 * 后端：
 *
 * GET /api/classrooms
 */
export async function pageClassrooms(
  query:
    PageQuery
    & {
      roomNo?: string
      campusId?: string
      type?: string
      status?: string
    },
): Promise<
  PageResult<Classroom>
> {

  const page =
    await getPage<Classroom>(
      '/classrooms',
      pageParams(
        query,
      ),
    )

  /**
   * ID统一转字符串。
   */
  return {
    ...page,

    list:
      page.list.map(
        (item) => ({
          ...item,

          id:
            item.id === null
            || item.id === undefined
              ? undefined
              : String(
                  item.id,
                ),

          campusId:
            item.campusId === null
            || item.campusId === undefined
              ? undefined
              : String(
                  item.campusId,
                ),
        }),
      ),
  }
}

/**
 * 查询空闲教室。
 *
 * 后端：
 *
 * GET /api/classrooms/free
 */
export async function listFreeClassrooms(
  params?: {
    campusId?: string
    type?: string
  },
): Promise<Classroom[]> {

  const data =
    await http.get<unknown>(
      '/classrooms/free',
      params,
    )

  const list =
    normalizeList<
      Classroom
    >(data)

  return list.map(
    (item) => ({
      ...item,

      id:
        item.id === null
        || item.id === undefined
          ? undefined
          : String(
              item.id,
            ),

      campusId:
        item.campusId === null
        || item.campusId === undefined
          ? undefined
          : String(
              item.campusId,
            ),
    }),
  )
}

/**
 * 提交教室申请。
 *
 * 后端：
 *
 * POST /api/classroom-applies
 *
 * applicant由Spring Security
 * 当前登录用户自动写入。
 */
export function submitClassroomApply(
  payload:
    CreateClassroomApplyParams,
) {

  return http.post<null>(
    '/classroom-applies',
    payload,
  )
}

/**
 * 我的教室申请。
 *
 * 后端：
 *
 * GET /api/classroom-applies/my
 */
export async function listMyClassroomApplies():
  Promise<ClassroomApply[]> {

  const data =
    await http.get<unknown>(
      '/classroom-applies/my',
    )

  const list =
    normalizeList<
      ClassroomApplyRaw
    >(data)

  return list.map(
    (item) => ({
      id:
        String(
          item.id,
        ),

      roomId:
        String(
          item.roomId,
        ),

      roomName:
        item.roomName,

      applicant:
        item.applicant,

      className:
        item.className,

      /**
       * 后端：
       * date
       *
       * 页面：
       * applyDate
       */
      applyDate:
        item.date,

      timeSlot:
        item.timeSlot,

      purpose:
        item.purpose,

      reason:
        item.reason,

      status:
        item.status,

      /**
       * 后端：
       * applyTime
       *
       * 页面：
       * createTime
       */
      createTime:
        item.applyTime,
    }),
  )
}

/**
 * 撤回自己的待审核申请。
 *
 * 后端：
 *
 * PUT
 * /api/classroom-applies/{id}/cancel
 */
export function cancelClassroomApply(
  id: string,
) {

  return http.put<null>(
    `/classroom-applies/${id}/cancel`,
  )
}