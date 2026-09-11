import { http } from '@/utils/request'

/**
 * 当前学期。
 * 后端 GET /api/term/current 返回的是 Term 实体对象，而前端只需要学期名字符串，
 * 这里统一归一化成字符串，两种返回都能用。
 */
export async function getCurrentTerm(): Promise<string> {
  const data = await http.get<string | { name?: string; termName?: string } | null>('/term/current')
  if (!data) {
    return ''
  }
  if (typeof data === 'string') {
    return data
  }
  return data.name || data.termName || ''
}
