<script setup lang="ts">
/**
 * 教师 · 学生考勤
 * 使用真实后端接口：
 *
 * GET  /api/classes
 * GET  /api/courses
 * GET  /api/students?classId=...
 * POST /api/student-attendances
 * GET  /api/student-attendances/weekly-report
 *
 * 当前不维护教师-课程、教师-班级关系，
 * 教师端直接使用系统全部班级和课程。
 *
 * 结构：顶部卡片头（PageHeader + 主操作 + StatBar）+ van-tabs（点名录入 / 本周报表）+ PageState 三态。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { showToast } from 'vant'
import {
  getWeeklyReport,
  listAttendanceClasses,
  listAttendanceCourses,
  listAttendanceStudents,
  recordAttendance,
} from '@/api/attendance'

import type {
  AttendanceClass,
  AttendanceCourse,
  AttendanceStudent,
  StudentAttendanceRecord,
  WeeklyReportRow,
} from '@/api/attendance'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import PageState from '@/components/PageState.vue'
import StatBar from '@/components/StatBar.vue'
import { STUDENT_ATTENDANCE_STATUS } from '@/constants/dict'
import { todayStr } from '@/utils/format'

const today = todayStr()
const maxDate = new Date()
/** 班级 / 日期选择器共用一个弹层，用 target 区分「点名录入」与「本周报表」 */
const pickerTarget = ref<'entry' | 'report'>('entry')

const classId = ref('')
const courseId = ref('')
const attendanceDate = ref(today)
const reportClassId = ref('')
const reportDate = ref(today)
/** 学生点名标记：studentId → 状态 */
const marks = ref<Record<string, string>>({})
const saving = ref(false)

const {
  data: classes,
  loading: classLoading,
  error: classError,
  reload: reloadClasses,
} = useAsyncData<AttendanceClass[]>(
  () => listAttendanceClasses(),
  [],
)
const {
  data: courses,
  loading: courseLoading,
  error: courseError,
  reload: reloadCourses,
} = useAsyncData<AttendanceCourse[]>(
  () => listAttendanceCourses(),
  [],
)
const {
  data: students,
  loading: studentLoading,
  error: studentError,
  reload: reloadStudents,
} = useAsyncData<AttendanceStudent[]>(
  () => (classId.value ? listAttendanceStudents(classId.value) : Promise.resolve([])),
  [],
)
const {
  data: report,
  loading: reportLoading,
  error: reportError,
  reload: reloadReport,
} = useAsyncData<WeeklyReportRow[]>(
  () =>
    reportClassId.value
      ? getWeeklyReport(reportClassId.value, reportDate.value)
      : Promise.resolve([]),
  [],
)

/** 按当前学生列表重建标记；默认所有学生「正常」，status 可整体覆盖 */
function buildMarks(status?: string): Record<string, string> {
  const result: Record<string, string> = {}
  for (const student of students.value) {
    result[String(student.id)] = status ?? '正常'
  }
  return result
}

/** 学生列表变化后重建标记（默认全部「正常」） */
watch(students, () => {
  marks.value = buildMarks()
})

/** 已标记为异常（非「正常」）的人数 */
const abnormalCount = computed(
  () => Object.values(marks.value).filter((status) => status !== '正常').length,
)

/** 统计条数据（对齐公共组件 StatBar）：本次点名概览 */
const summaryItems = computed(() => [
  { label: '已标记异常', value: abnormalCount.value },
  { label: '班级人数', value: students.value.length },
])

/** 班级 / 课程加载失败时的重试（只重取选项，不改动已选条件） */
function reloadOptions() {
  reloadClasses()
  reloadCourses()
}

function markAllNormal() {
  marks.value = buildMarks('正常')
  showToast('已全部标记为正常')
}

async function submitRecords() {
  if (!classId.value) {
    showToast('请选择班级')
    return
  }
  if (!courseId.value) {
    showToast('请选择课程')
    return
  }
  if (students.value.length === 0) {
    showToast('该班级暂无学生')
    return
  }
  const records: StudentAttendanceRecord[] = students.value.map((student) => ({
    studentId: String(student.id),
    courseId: courseId.value,
    attendanceDate: attendanceDate.value,
    status: marks.value[String(student.id)] ?? '正常',
  }))
  saving.value = true
  try {
    await recordAttendance(records)
    showToast(`已提交 ${records.length} 名学生的考勤`)
    // 当前班级自动切换到周报查询
    reportClassId.value = classId.value
    reportDate.value = attendanceDate.value
    await reloadReport()
  } catch {
    // request.ts统一提示
  } finally {
    saving.value = false
  }
}

/* ------------------------------ 选择器 ------------------------------ */
const showClassPicker = ref(false)
const showCoursePicker = ref(false)
const showDatePicker = ref(false)

const classText = computed(
  () => classes.value.find((item) => String(item.id) === classId.value)?.name ?? '',
)
const courseText = computed(
  () => courses.value.find((item) => String(item.id) === courseId.value)?.name ?? '',
)
const reportClassText = computed(
  () => classes.value.find((item) => String(item.id) === reportClassId.value)?.name ?? '',
)
const classColumns = computed(() =>
  classes.value.map((item) => ({ text: item.name, value: String(item.id) })),
)
const courseColumns = computed(() =>
  courses.value.map((item) => ({
    text: `${item.name}（${item.courseCode}）`,
    value: String(item.id),
  })),
)
const pickerClassId = computed(() =>
  pickerTarget.value === 'entry' ? classId.value : reportClassId.value,
)
const pickerDateValues = computed(() =>
  (pickerTarget.value === 'entry' ? attendanceDate.value : reportDate.value).split('-'),
)

interface PickerPayload {
  selectedOptions?: Array<{ value?: string | number } | undefined>
}

function pickId(payload: PickerPayload): string {
  const value = payload.selectedOptions?.[0]?.value
  return value === undefined || value === null ? '' : String(value)
}

function openClassPicker(target: 'entry' | 'report') {
  if (!classes.value.length) {
    showToast('系统暂无班级数据')
    return
  }
  pickerTarget.value = target
  showClassPicker.value = true
}

function openCoursePicker() {
  if (!courses.value.length) {
    showToast('系统暂无课程数据')
    return
  }
  showCoursePicker.value = true
}

function openDatePicker(target: 'entry' | 'report') {
  pickerTarget.value = target
  showDatePicker.value = true
}

function onClassConfirm(payload: PickerPayload) {
  const id = pickId(payload)
  showClassPicker.value = false
  if (!id) {
    return
  }
  if (pickerTarget.value === 'entry') {
    if (id !== classId.value) {
      classId.value = id
      reloadStudents()
    }
  } else if (id !== reportClassId.value) {
    reportClassId.value = id
    reloadReport()
  }
}

function onCourseConfirm(payload: PickerPayload) {
  courseId.value = pickId(payload)
  showCoursePicker.value = false
}

function onDateConfirm(payload: { selectedValues?: string[] }) {
  const values = payload.selectedValues ?? []
  showDatePicker.value = false
  if (values.length !== 3) {
    return
  }
  const date = values.join('-')
  if (pickerTarget.value === 'entry') {
    attendanceDate.value = date
  } else {
    reportDate.value = date
    reloadReport()
  }
}

/** 出勤率：该周无考勤记录时后端返回 null，此时显示 — */
function rateText(rate?: number | null): string {
  return rate === null || rate === undefined ? '—' : `${rate}%`
}

onMounted(() => {
  reloadClasses()
  reloadCourses()
})
</script>

<template>
  <div>
    <!-- 顶部：标题 + 主操作 + 概览（对齐 admin 的卡片头结构） -->
    <div class="st-card">
        <PageHeader title="学生考勤">
          <template #actions>
            <van-button size="small" type="primary" :loading="saving" @click="submitRecords">
              提交考勤
            </van-button>
          </template>
        </PageHeader>

        <StatBar :items="summaryItems" />
      </div>

      <van-tabs>
        <!-- 点名录入 -->
        <van-tab title="点名录入" name="entry">
          <PageState
            :loading="classLoading || courseLoading"
            :error="classError || courseError"
            :empty="!classes.length || !courses.length"
            :empty-text="classes.length ? '系统暂无课程数据' : '系统暂无班级数据'"
            @retry="reloadOptions"
          >
            <div class="st-card">
              <van-field
                readonly
                is-link
                label="班级"
                placeholder="请选择班级"
                :model-value="classText"
                @click="openClassPicker('entry')"
              />
              <van-field
                readonly
                is-link
                label="课程"
                placeholder="请选择课程"
                :model-value="courseText"
                @click="openCoursePicker"
              />
              <van-field
                readonly
                is-link
                label="上课日期"
                :model-value="attendanceDate"
                @click="openDatePicker('entry')"
              />
            </div>

            <div class="st-row sa__bar">
              <van-button size="mini" plain type="primary" @click="markAllNormal"
                >全部标记为正常</van-button
              >
            </div>

            <PageState
              :loading="studentLoading"
              :error="studentError"
              :empty="!classId || students.length === 0"
              :empty-text="classId ? '该班级暂无学生' : '请先选择班级'"
              @retry="reloadStudents"
            >
              <div v-for="student in students" :key="student.id" class="st-card">
                <div class="st-row">
                  <span class="sa__name">{{ student.name }}</span>
                  <span class="st-muted">{{ student.studentNo }}</span>
                </div>
                <van-radio-group
                  v-model="marks[student.id]"
                  direction="horizontal"
                  class="sa__options"
                >
                  <van-radio
                    v-for="status in STUDENT_ATTENDANCE_STATUS"
                    :key="status"
                    :name="status"
                  >
                    {{ status }}
                  </van-radio>
                </van-radio-group>
              </div>
            </PageState>
          </PageState>
        </van-tab>

        <!-- 本周报表 -->
        <van-tab title="本周报表" name="report">
          <PageState
            :loading="classLoading"
            :empty="!classes.length"
            :empty-text="classError || '系统暂无班级数据'"
            @retry="reloadClasses"
          >
            <div class="st-card">
              <van-field
                readonly
                is-link
                label="班级"
                placeholder="请选择班级"
                :model-value="reportClassText"
                @click="openClassPicker('report')"
              />
              <van-field
                readonly
                is-link
                label="所在周"
                :model-value="reportDate"
                @click="openDatePicker('report')"
              />
            </div>

            <PageState
              :loading="reportLoading"
              :error="reportError"
              :empty="!reportClassId || report.length === 0"
              :empty-text="reportClassId ? '本周暂无考勤数据' : '请先选择班级'"
              @retry="reloadReport"
            >
              <div v-for="row in report" :key="row.studentNo ?? row.studentName" class="st-card">
                <div class="st-row">
                  <span class="sa__name">{{ row.studentName || '未知学生' }}</span>
                  <span class="st-muted">{{ row.studentNo || '—' }}</span>
                </div>
                <div class="sa__report">
                  <span>总记录 {{ row.total }}</span>
                  <span>正常 {{ row.normal }}</span>
                  <span>异常 {{ row.abnormal }}</span>
                  <span>正常率 {{ rateText(row.rate) }}</span>
                </div>
              </div>
            </PageState>
          </PageState>
        </van-tab>
      </van-tabs>

    <!-- 班级 / 课程 / 日期选择器 -->
    <van-popup v-model:show="showClassPicker" position="bottom" round>
      <van-picker
        title="选择班级"
        :columns="classColumns"
        :model-value="[pickerClassId]"
        @confirm="onClassConfirm"
        @cancel="showClassPicker = false"
      />
    </van-popup>
    <van-popup v-model:show="showCoursePicker" position="bottom" round>
      <van-picker
        title="选择课程"
        :columns="courseColumns"
        :model-value="[courseId]"
        @confirm="onCourseConfirm"
        @cancel="showCoursePicker = false"
      />
    </van-popup>
    <van-popup v-model:show="showDatePicker" position="bottom" round>
      <van-date-picker
        title="选择日期"
        :model-value="pickerDateValues"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<style scoped>
.sa__bar {
  justify-content: flex-end;
  margin: 0 4px 10px;
}
.sa__options {
  margin-top: 10px;
}
.sa__name {
  font-size: 15px;
  font-weight: 600;
}
.sa__report {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--st-text-light);
}
</style>
