import * as XLSX from 'xlsx'

/** 导出列配置 */
export interface ExcelColumn {
  /** 列标题 */
  label: string
  /** 数据字段名 */
  key: string
}

/**
 * 把数据导出为 .xlsx 文件（纯前端生成，不经过后端）。
 * 用于成绩单、班级花名册等导出场景。
 */
export function exportToExcel(options: {
  rows: Array<Record<string, unknown>>
  columns: ExcelColumn[]
  fileName: string
  sheetName?: string
}): void {
  const { rows, columns, fileName, sheetName = 'Sheet1' } = options

  const data = rows.map((row) => {
    const item: Record<string, unknown> = {}
    for (const column of columns) {
      const value = row[column.key]
      item[column.label] = value === null || value === undefined ? '' : value
    }
    return item
  })

  const sheet = XLSX.utils.json_to_sheet(data, { header: columns.map((column) => column.label) })
  const book = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(book, sheet, sheetName)
  XLSX.writeFile(book, fileName.toLowerCase().endsWith('.xlsx') ? fileName : `${fileName}.xlsx`)
}

/** 生成带时间戳的文件名，如 成绩单_20260914_1030.xlsx */
export function timestampedFileName(prefix: string): string {
  const now = new Date()
  const pad = (value: number) => (value < 10 ? `0${value}` : String(value))
  const date = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}`
  const time = `${pad(now.getHours())}${pad(now.getMinutes())}`
  return `${prefix}_${date}_${time}.xlsx`
}
