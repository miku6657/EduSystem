/** 日期 / 时间 / 数值格式化工具（师生端统一在此处理，避免各页面各写一套） */

function pad2(n: number): string {
  return n < 10 ? `0${n}` : String(n)
}

/** Date → 'YYYY-MM-DD' */
export function toDateStr(date: Date): string {
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`
}

/** 今天，'YYYY-MM-DD' */
export function todayStr(): string {
  return toDateStr(new Date())
}

/** 'YYYY-MM-DD' → Date（按本地时区，避免 new Date('2026-09-11') 的 UTC 偏移） */
export function parseDate(dateStr: string): Date {
  const [y, m, d] = dateStr.split('-').map((item) => Number(item))
  return new Date(y, (m ?? 1) - 1, d ?? 1)
}

/** 日期加减天数，返回 'YYYY-MM-DD' */
export function addDays(dateStr: string, days: number): string {
  const date = parseDate(dateStr)
  date.setDate(date.getDate() + days)
  return toDateStr(date)
}

/** 取某天所在的一周（周一 ~ 周日） */
export function weekRangeOf(dateStr: string): { start: string; end: string } {
  const date = parseDate(dateStr)
  const day = date.getDay() // 0 = 周日
  const offsetToMonday = day === 0 ? -6 : 1 - day
  const monday = addDays(dateStr, offsetToMonday)
  return { start: monday, end: addDays(monday, 6) }
}

/** 本周一 ~ 本周日 */
export function currentWeekRange(): { start: string; end: string } {
  return weekRangeOf(todayStr())
}

/** 时间戳 / ISO 字符串 → 'YYYY-MM-DD HH:mm' */
export function formatDateTime(raw?: string | null): string {
  if (!raw) {
    return '—'
  }
  const normalized = raw.replace('T', ' ')
  return normalized.length > 16 ? normalized.slice(0, 16) : normalized
}

/** 时间戳 / ISO 字符串 → 'YYYY-MM-DD' */
export function formatDate(raw?: string | null): string {
  if (!raw) {
    return '—'
  }
  return raw.replace('T', ' ').slice(0, 10)
}

/** 分数显示：空值显示'—'，保留 1 位小数（整数不补 .0） */
export function formatScore(score?: number | null): string {
  if (score === null || score === undefined) {
    return '—'
  }
  return Number.isInteger(score) ? String(score) : score.toFixed(1)
}
