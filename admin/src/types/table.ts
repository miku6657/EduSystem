/** 可渲染的表单项类型（搜索栏 / 弹窗表单通用） */
export type FieldType = 'input' | 'select' | 'date-picker' | 'number' | 'textarea'

export interface SelectOption {
  label: string
  value: string | number
}

/** 字段基础配置 */
export interface FieldConfig {
  /** 展示文案 */
  label: string
  /** 绑定字段（对应数据行属性 / 查询参数） */
  prop: string
  /** 控件类型，默认 input */
  type?: FieldType
  placeholder?: string
  /** select 选项 */
  options?: SelectOption[]
  /** 弹窗校验是否必填，默认 true */
  required?: boolean
  /** 日期控件格式，默认 YYYY-MM-DD */
  valueFormat?: string
  /** number 类型最小值 */
  min?: number
  /** 默认值（弹窗打开 / 重置时使用） */
  defaultValue?: unknown
}

/** 搜索栏字段 */
export type SearchField = FieldConfig

/** 新增 / 编辑弹窗字段 */
export type DialogField = FieldConfig

/** 表格列配置 */
export interface TableColumn {
  /** 特殊列类型：selection（多选）/ index（序号）/ action（操作） */
  type?: 'selection' | 'index' | 'action'
  label?: string
  prop?: string
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  fixed?: boolean | 'left' | 'right'
  showOverflowTooltip?: boolean
}
