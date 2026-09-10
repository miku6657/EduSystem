import { getPage, pageParams } from '@/utils/request'
import type { PageQuery, PageResult } from '@/types/api'

/** 考试信息（exam_info） */
export interface ExamInfo {
  id?: number
  name: string
  courseId?: number
  termId?: number
  examType?: string
  /** YYYY-MM-DD */
  examDate?: string
  startTime?: string
  endTime?: string
  status?: string
  /** 展示用扩展字段（后端实体没有，Mock 会带） */
  courseName?: string
}

/**
 * 分页查询考试（教师录成绩时选择考试用）
 * 后端 GET /api/exam/page?pageNo&pageSize&name&termId&examType
 */
export function pageExams(
  query: PageQuery & { name?: string; termId?: number; examType?: string },
): Promise<PageResult<ExamInfo>> {
  return getPage<ExamInfo>('/exam/page', pageParams(query))
}
