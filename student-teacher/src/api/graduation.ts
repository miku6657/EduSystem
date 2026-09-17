import {
  listMyScores,
} from '@/api/score'

export interface FailedCourse {
  examId?: string

  courseId?: string

  courseCode?: string

  courseName?: string

  examName?: string

  examDate?: string

  credit?: number

  score: number
}

export interface GraduationEligibility {
  /**
   * 是否符合毕业资格
   */
  eligible: boolean

  /**
   * 已录入成绩的课程数
   * （每门课程去重后）
   */
  total: number

  /**
   * 及格科目数
   */
  passed: number

  /**
   * 不及格科目数
   */
  failed: number

  /**
   * 低于60分的课程
   */
  failedCourses:
    FailedCourse[]
}

/**
 * 学生毕业资格判断
 *
 * 规则：
 *
 * 每门课程只取最高成绩
 * （同一课程可能有期末考试 + 重修考试）。
 *
 * 存在课程最高成绩 < 60
 * -> 不可以毕业
 *
 * 不存在课程最高成绩 < 60
 * -> 可以毕业
 */
export async function getMyGraduationEligibility(
  studentId: string,
): Promise<GraduationEligibility> {

  const scores =
    await listMyScores(
      studentId,
    )

  /**
   * courseId -> 该课程最高成绩
   */
  const courseBestMap =
    new Map<
      string,
      typeof scores[number]
    >()

  for (
    const item
    of scores
  ) {

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
      String(item.courseId)

    const old =
      courseBestMap.get(
        courseId,
      )

    if (
      !old
      ||
      Number(item.score)
      >
      Number(old.score)
    ) {
      courseBestMap.set(
        courseId,
        item,
      )
    }
  }

  /**
   * 每门课程只保留最高成绩。
   */
  const effectiveScores =
    Array.from(
      courseBestMap.values(),
    )

  const failedScores =
    effectiveScores.filter(
      item =>
        Number(item.score) < 60,
    )

  const passedScores =
    effectiveScores.filter(
      item =>
        Number(item.score) >= 60,
    )

  return {
    eligible:
      failedScores.length === 0,

    total:
      effectiveScores.length,

    passed:
      passedScores.length,

    failed:
      failedScores.length,

    failedCourses:
      failedScores.map(
        item => ({
          examId:
            item.examId,

          courseId:
            item.courseId,

          courseCode:
            item.courseCode,

          courseName:
            item.courseName,

          examName:
            item.examName,

          examDate:
            item.examDate,

          credit:
            item.credit,

          score:
            Number(
              item.score,
            ),
        }),
      ),
  }
}