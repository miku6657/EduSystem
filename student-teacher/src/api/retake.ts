import {
  http,
  normalizeList,
} from '@/utils/request'

import {
  listMyScores,
} from '@/api/score'

export type RetakeStatus =
  | 'WAIT'
  | 'APPROVED'
  | 'REJECTED'
  | 'ARRANGED'
  | 'COMPLETED'

interface ExamRetakeRaw {
  id: string | number

  studentId: string | number

  courseId: string | number

  examId?: string | number | null

  type: string

  status: RetakeStatus

  createTime?: string

  updateTime?: string
}

interface CourseRaw {
  id: string | number

  courseCode?: string

  name: string

  credit?: number
}

interface ExamRaw {
  id: string | number

  name: string

  courseId?: string | number

  examDate?: string

  startTime?: string

  endTime?: string
}

/**
 * 学生页面展示的重修申请。
 */
export interface ExamRetake {
  id: string

  studentId: string

  courseId: string

  examId?: string

  type: string

  status: RetakeStatus

  courseCode?: string

  courseName?: string

  credit?: number

  examName?: string

  examDate?: string

  startTime?: string

  endTime?: string

  createTime?: string
}

/**
 * 当前真正挂科的课程。
 */
export interface FailedCourse {
  courseId: string

  courseCode?: string

  courseName: string

  credit?: number

  /**
   * 当前课程有效成绩。
   */
  score: number
}

/**
 * 学生申请重修。
 *
 * 新后端：
 *
 * POST /api/exam-retakes
 * ?studentId=...
 * &courseId=...
 */
export function applyRetake(
  studentId: string,
  courseId: string,
) {

  return http.post<null>(
    '/exam-retakes',
    undefined,
    {
      studentId,
      courseId,
    },
  )
}

/**
 * 学生查询自己的申请。
 *
 * GET
 * /api/exam-retakes/students/{studentId}
 */
export async function listMyRetakes(
  studentId: string,
): Promise<ExamRetake[]> {

  const [
    retakeData,
    courseData,
    examData,
  ] =
    await Promise.all([
      http.get<unknown>(
        `/exam-retakes/students/${studentId}`,
      ),

      http.get<unknown>(
        '/courses',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),

      http.get<unknown>(
        '/exams',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),
    ])

  const retakes =
    normalizeList<ExamRetakeRaw>(
      retakeData,
    )

  const courses =
    normalizeList<CourseRaw>(
      courseData,
    )

  const exams =
    normalizeList<ExamRaw>(
      examData,
    )

  const courseMap =
    new Map(
      courses.map(
        item => [
          String(item.id),
          item,
        ],
      ),
    )

  const examMap =
    new Map(
      exams.map(
        item => [
          String(item.id),
          item,
        ],
      ),
    )

  return retakes.map(
    item => {

      const course =
        courseMap.get(
          String(item.courseId),
        )

      const exam =
        item.examId
          ? examMap.get(
              String(item.examId),
            )
          : undefined

      return {
        id:
          String(item.id),

        studentId:
          String(item.studentId),

        courseId:
          String(item.courseId),

        examId:
          item.examId === null
          || item.examId === undefined
            ? undefined
            : String(item.examId),

        type:
          item.type,

        status:
          item.status,

        courseCode:
          course?.courseCode,

        courseName:
          course?.name,

        credit:
          course?.credit,

        examName:
          exam?.name,

        examDate:
          exam?.examDate,

        startTime:
          exam?.startTime,

        endTime:
          exam?.endTime,

        createTime:
          item.createTime,
      }
    },
  )
}

/**
 * 查询当前学生自己的挂科课程。
 *
 * 只读取当前学生的成绩。
 *
 * 规则：
 * score < 60 = 挂科
 */
export async function listFailedCourses(
  studentId: string,
): Promise<FailedCourse[]> {

  /**
   * listMyScores 本身调用的是：
   *
   * GET
   * /exam-scores/students/{studentId}
   *
   * 所以这里只会拿当前学生自己的成绩。
   */
  const scores =
    await listMyScores(
      studentId,
    )

  /**
   * courseId -> 当前课程成绩
   */
  const courseMap =
    new Map<
      string,
      FailedCourse
    >()

  for (
    const item
    of scores
  ) {

    /**
     * 没有关联课程，
     * 或还没录成绩，
     * 不参与挂科判断。
     */
    if (
      !item.courseId
      ||
      item.score === null
      ||
      item.score === undefined
    ) {
      continue
    }

    const courseId =
      String(
        item.courseId,
      )

    const score =
      Number(
        item.score,
      )

    const old =
      courseMap.get(
        courseId,
      )

    /**
     * 如果同一课程出现多条成绩，
     * 保留最高成绩。
     *
     * 例如：
     * 原成绩 55
     * 补考成绩 65
     *
     * 最终按65处理，
     * 不再属于挂科课程。
     */
    if (
      !old
      ||
      score > old.score
    ) {

      courseMap.set(
        courseId,
        {
          courseId,

          courseCode:
            item.courseCode,

          courseName:
            item.courseName
            ?? '未知课程',

          credit:
            item.credit,

          score,
        },
      )
    }
  }

  /**
   * 最后只返回 < 60 的课程。
   */
  return Array
    .from(
      courseMap.values(),
    )
    .filter(
      item =>
        item.score < 60,
    )
}

/**
 * 管理端也可以复用：
 * 查询某种类型申请。
 */
export async function listRetakesByType(
  type: string,
) {

  const data =
    await http.get<unknown>(
      `/exam-retakes/type/${encodeURIComponent(type)}`,
    )

  return normalizeList<
    ExamRetakeRaw
  >(data)
}

export interface RetakeStudent {
  id: string

  studentId: string

  courseId: string

  examId: string

  type: string

  status: RetakeStatus
}

/**
 * 查询某场重修考试真正参加的学生。
 *
 * GET
 * /api/exam-retakes/exams/{examId}
 */
export async function listRetakeStudents(
  examId: string,
): Promise<RetakeStudent[]> {

  const data =
    await http.get<unknown>(
      `/exam-retakes/exams/${examId}`,
    )

  const rows =
    normalizeList<any>(
      data,
    )

  return rows.map(
    item => ({
      id:
        String(item.id),

      studentId:
        String(item.studentId),

      courseId:
        String(item.courseId),

      examId:
        String(item.examId),

      type:
        item.type,

      status:
        item.status,
    }),
  )
}

export interface RetakeScoreSubmit {
  studentId: string

  score: number

  status?: string
}

/**
 * 教师录入重修成绩。
 *
 * 后端会再次检查：
 * studentId是否真的属于这场重修考试。
 */
export function saveRetakeScores(
  examId: string,
  scores: RetakeScoreSubmit[],
) {

  return http.post<null>(
    `/exam-retakes/exams/${examId}/scores`,
    scores,
  )
}