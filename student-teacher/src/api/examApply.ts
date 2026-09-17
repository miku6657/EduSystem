import {
  http,
  normalizeList,
} from '@/utils/request'

export interface ExamApply {
  id?: string

  courseId: string

  teacherId: string

  applyType: string

  reason?: string

  status?: 'WAIT' | 'PASS' | 'FAIL'

  createTime?: string

  updateTime?: string

  /**
   * 前端展示字段
   */
  courseName?: string

  courseCode?: string
}

export interface ExamCourse {
  id: string

  courseCode: string

  name: string

  credit?: number

  type?: string

  teacherId?: string

  termId?: string
}

/**
 * 查询所有课程。
 *
 * 不再区分“我的课程”。
 *
 * 真实后端：
 * GET /api/courses
 */
export async function listExamCourses():
  Promise<ExamCourse[]> {

  const data =
    await http.get<unknown>(
      '/courses',
      {
        pageNo: 1,
        pageSize: 1000,
      },
    )

  return normalizeList<
    ExamCourse
  >(data).map(
    (item) => ({
      ...item,

      id:
        String(
          item.id,
        ),

      teacherId:
        item.teacherId === null
        || item.teacherId === undefined
          ? undefined
          : String(
              item.teacherId,
            ),

      termId:
        item.termId === null
        || item.termId === undefined
          ? undefined
          : String(
              item.termId,
            ),
    }),
  )
}

/**
 * 提交考核方式申报。
 *
 * 真实后端：
 * POST /api/exam-applies
 */
export function submitExamApply(
  payload: {
    courseId: string

    teacherId: string

    applyType: string

    reason: string
  },
) {

  return http.post<null>(
    '/exam-applies',
    payload,
  )
}

/**
 * 查询当前教师自己的申报记录。
 *
 * 后端没有 /my，
 * 所以使用已有：
 *
 * GET /api/exam-applies/export
 *
 * 再根据teacherId过滤。
 */
export async function listMyExamApplies(
  teacherId: string,
): Promise<ExamApply[]> {

  const [
    applyData,
    courses,
  ] =
    await Promise.all([
      http.get<unknown>(
        '/exam-applies/export',
      ),

      listExamCourses(),
    ])

  const applies =
    normalizeList<
      ExamApply
    >(applyData)

  const courseMap =
    new Map(
      courses.map(
        (course) => [
          String(course.id),
          course,
        ],
      ),
    )

  return applies
    .filter(
      (item) =>
        String(
          item.teacherId,
        )
        ===
        String(
          teacherId,
        ),
    )
    .map(
      (item) => {

        const course =
          courseMap.get(
            String(
              item.courseId,
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

          courseId:
            String(
              item.courseId,
            ),

          teacherId:
            String(
              item.teacherId,
            ),

          courseName:
            course?.name,

          courseCode:
            course?.courseCode,
        }
      },
    )
}

/**
 * 申报详情。
 *
 * 真实后端：
 * GET /api/exam-applies/{id}
 */
export function getExamApply(
  id: string,
) {

  return http.get<ExamApply>(
    `/exam-applies/${id}`,
  )
}
