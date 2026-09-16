import { http } from '@/utils/request'

/** 学期（base_term） */
export interface Term {
  id?: number
  name: string
  startDate?: string
  endDate?: string
  /** 1 = 进行中，0 = 已结束 */
  status?: number
}

/**
 * 学期列表（用于把考试/教学任务里的 termId 映射成学期名称）
 * 后端 GET /api/terms
 */
export async function listTerms(): Promise<Term[]> {
  const data = await http.get<Term[] | null>('/terms')
  return Array.isArray(data) ? data : []
}

/**
 * 当前学期名称。
 *
 * 后端已把原 `/api/term/current` 改为 RESTful 的 `GET /api/terms`（返回学期列表），
 * 因此这里取列表里 status === 1（进行中）的那条；若后端日后恢复 current 接口，
 * 也可传入字符串直接返回。
 */
export async function getCurrentTerm(): Promise<string> {
  const data = await http.get<Term[] | Term | string | null>('/terms')
  if (!data) {
    return ''
  }
  if (typeof data === 'string') {
    return data
  }
  if (Array.isArray(data)) {
    const current = data.find((item) => Number(item?.status) === 1) ?? data[0]
    return current?.name ?? ''
  }
  return data.name ?? ''
}
