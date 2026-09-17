import {
  getPage,
  http,
  normalizeList,
} from '@/utils/request'

import type {
  PageResult,
} from '@/types/api'

/**
 * 成绩记录
 */
export interface ExamScore {
  id?: string

  examId?: string

  studentId: string

  score?: number | null

  status?: string

  /**
   * 以下都是前端关联后的展示字段
   */
  examName?: string

  examDate?: string

  courseId?: string

  courseName?: string

  courseCode?: string

  credit?: number
}

/**
 * 学生
 */
export interface ScoreStudent {
  id: string

  studentNo: string

  name: string

  classId?: string

  status?: string
}

/**
 * “待录入成绩课程”
 *
 * 页面显示课程，
 * 实际提交成绩时使用examId。
 */
export interface UnscoredCourse {
  examId: string

  examName: string

  examType?: string

  examDate?: string

  courseId: string

  courseCode?: string

  courseName: string
}

interface ExamInfoRaw {
  id: string | number

  name: string

  courseId?: string | number

  examType?: string

  examDate?: string
}

interface CourseRaw {
  id: string | number

  courseCode?: string

  name: string
}

/**
 * 查询所有“还没有任何成绩”的考试/课程。
 *
 * 逻辑：
 *
 * GET /exams
 * ↓
 * 每场考试查询 /exam-scores
 * ↓
 * total === 0
 * ↓
 * 才允许录入
 */
export async function listUnscoredCourses():
  Promise<UnscoredCourse[]> {

  const [
    examData,
    courseData,
  ] =
    await Promise.all([
      http.get<unknown>(
        '/exams',
        {
          pageNo: 1,
          pageSize: 1000,
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

  const exams =
    normalizeList<ExamInfoRaw>(
      examData,
    )

  const courses =
    normalizeList<CourseRaw>(
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

  /**
   * 检查每场考试有没有成绩。
   */
  const checked =
    await Promise.all(
      exams.map(
        async exam => {

          if (
            exam.id === null
            || exam.id === undefined
            || exam.courseId === null
            || exam.courseId === undefined
          ) {
            return null
          }

          const examId =
            String(exam.id)

          const page =
            await getPage<ExamScore>(
              '/exam-scores',
              {
                pageNo: 1,
                pageSize: 1,
                examId,
              },
            )

          /**
           * 已经存在任意成绩记录，
           * 就不再出现在待录入列表。
           */
          if (
            page.total > 0
          ) {
            return null
          }

          const course =
            courseMap.get(
              String(
                exam.courseId,
              ),
            )

          if (!course) {
            return null
          }

          return {
            examId,

            examName:
              exam.name,

            examType:
              exam.examType,

            examDate:
              exam.examDate,

            courseId:
              String(
                exam.courseId,
              ),

            courseCode:
              course.courseCode,

            courseName:
              course.name,
          } satisfies UnscoredCourse
        },
      ),
    )

  return checked.filter(
    (
      item,
    ): item is UnscoredCourse =>
      item !== null,
  )
}

/**
 * 查询学生。
 *
 * 当前后端没有“某课程学生”关系接口，
 * 所以使用现有学生列表。
 */
export async function listScoreStudents():
  Promise<ScoreStudent[]> {

  const data =
    await http.get<unknown>(
      '/students',
      {
        pageNo: 1,
        pageSize: 1000,
      },
    )

  return normalizeList<ScoreStudent>(
    data,
  ).map(
    student => ({
      ...student,

      id:
        String(
          student.id,
        ),

      classId:
        student.classId === null
        || student.classId === undefined
          ? undefined
          : String(
              student.classId,
            ),
    }),
  )
}

/**
 * 真正保存到exam_score。
 *
 * POST
 * /api/exam-scores/exams/{examId}
 */
export function saveScores(
  examId: string,
  scores: ExamScore[],
) {

  return http.post<null>(
    `/exam-scores/exams/${examId}`,
    scores,
  )
}

/**
 * 某场考试成绩。
 */
export function pageScoresByExam(
  examId: string,
  page = 1,
  pageSize = 100,
): Promise<
  PageResult<ExamScore>
> {

  return getPage<ExamScore>(
    '/exam-scores',
    {
      pageNo: page,
      pageSize,
      examId,
    },
  )
}

/**
 * 成绩统计。
 */
export interface ExamScoreStat {
  total?: number
  actual?: number
  absent?: number
  passed?: number
  failed?: number
}

export function getScoreStat(
  examId: string,
) {

  return http.get<
    ExamScoreStat
  >(
    `/exam-scores/exams/${examId}/statistics`,
  )
}

interface StudentExamRaw {
  id: string | number

  name: string

  courseId?: string | number

  examType?: string

  examDate?: string
}

interface StudentCourseRaw {
  id: string | number

  courseCode?: string

  name: string

  credit?: number
}

/**
 * 学生查看自己的成绩。
 */
export async function listMyScores(
  studentId: string,
): Promise<ExamScore[]> {

  const [
    scoreData,
    examData,
    courseData,
  ] =
    await Promise.all([
      /**
       * 当前学生成绩
       */
      http.get<unknown>(
        `/exam-scores/students/${studentId}`,
      ),

      /**
       * 考试基础数据
       */
      http.get<unknown>(
        '/exams',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),

      /**
       * 课程基础数据
       */
      http.get<unknown>(
        '/courses',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),
    ])

  const scores =
    normalizeList<ExamScore>(
      scoreData,
    )

  const exams =
    normalizeList<StudentExamRaw>(
      examData,
    )

  const courses =
    normalizeList<StudentCourseRaw>(
      courseData,
    )

  /**
   * examId -> exam
   */
  const examMap =
    new Map(
      exams.map(
        exam => [
          String(exam.id),
          exam,
        ],
      ),
    )

  /**
   * courseId -> course
   */
  const courseMap =
    new Map(
      courses.map(
        course => [
          String(course.id),
          course,
        ],
      ),
    )

  return scores.map(
    score => {

      const exam =
        score.examId
          ? examMap.get(
              String(
                score.examId,
              ),
            )
          : undefined

      const course =
        exam?.courseId !== undefined
        && exam?.courseId !== null
          ? courseMap.get(
              String(
                exam.courseId,
              ),
            )
          : undefined

      return {
        ...score,

        id:
          score.id === null
          || score.id === undefined
            ? undefined
            : String(
                score.id,
              ),

        examId:
          score.examId === null
          || score.examId === undefined
            ? undefined
            : String(
                score.examId,
              ),

        studentId:
          String(
            score.studentId,
          ),

        /**
         * 页面展示字段
         */
        examName:
          exam?.name,

        examDate:
          exam?.examDate,

        courseId:
          exam?.courseId === null
          || exam?.courseId === undefined
            ? undefined
            : String(
                exam.courseId,
              ),

        courseName:
          course?.name,

        courseCode:
          course?.courseCode,

        credit:
          course?.credit,
      }
    },
  )
}
