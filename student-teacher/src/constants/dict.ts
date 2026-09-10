/**
 * 业务字典：取值与后端 data.sql / Service 中硬编码的中文/英文枚举保持一致。
 * 后端若要改枚举，这里同步改一处即可。
 */

/** 审核类状态（考核方式申报 / 专升本报名 / 毕业资格审核 通用） */
export const AUDIT_STATUS_TEXT: Record<string, string> = {
  WAIT: '待审核',
  PASS: '已通过',
  FAIL: '已驳回',
}

/** Vant Tag 类型：primary / success / danger / warning */
export const AUDIT_STATUS_TYPE: Record<string, 'primary' | 'success' | 'danger' | 'warning'> = {
  WAIT: 'warning',
  PASS: 'success',
  FAIL: 'danger',
}

/** 合格 / 不合格（毕业审核里的学分、课程状态） */
export const PASS_FAIL_TEXT: Record<string, string> = {
  PASS: '合格',
  FAIL: '不合格',
}

/** 学生考勤状态（student_attendance.status） */
export const STUDENT_ATTENDANCE_STATUS = ['正常', '迟到', '缺勤', '请假'] as const

export const ATTENDANCE_STATUS_TYPE: Record<string, 'success' | 'warning' | 'danger' | 'primary'> = {
  正常: 'success',
  迟到: 'warning',
  缺勤: 'danger',
  请假: 'primary',
}

/** 教师考勤状态（teacher_attendance.status） */
export const TEACHER_ATTENDANCE_STATUS = ['正常', '迟到', '缺勤'] as const

/** 成绩状态（exam_score.status） */
export const SCORE_STATUS_TEXT: Record<string, string> = {
  NORMAL: '正常',
  ABSENT: '缺考',
  CHEAT: '违纪',
  DELAY: '缓考',
}

export const SCORE_STATUS_OPTIONS = [
  { text: '正常', value: 'NORMAL' },
  { text: '缺考', value: 'ABSENT' },
  { text: '缓考', value: 'DELAY' },
  { text: '违纪', value: 'CHEAT' },
]

/** 及格线（与后端统计口径一致：>=60 为及格） */
export const PASS_SCORE_LINE = 60

/** 补考 / 重修类型（exam_retake.type） */
export const RETAKE_TYPE_OPTIONS = [
  { text: '补考', value: '补考' },
  { text: '重修', value: '重修' },
] as const

/** 监考角色（exam_monitor.monitor_role） */
export const MONITOR_ROLE_TEXT: Record<string, string> = {
  MAIN: '主监考',
  SUB: '副监考',
}

/** 考核方式（exam_apply.apply_type） */
export const EXAM_METHOD_OPTIONS = ['闭卷', '开卷', '机考', '论文', '实操'] as const

/** 教材/教室等通用状态兜底文案 */
export function dictText(map: Record<string, string>, key?: string | null, fallback = '—'): string {
  if (!key) {
    return fallback
  }
  return map[key] ?? key
}
