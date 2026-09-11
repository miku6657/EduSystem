<script setup lang="ts">
/**
 * 教师 · 学生考勤
 * 点名录入：GET /api/student/list-by-class/{classId}、POST /api/student-attendance/record
 * 本周报表：GET /api/student-attendance/weekly-report?classId&date（date 传该周任意一天）
 * 班级 / 课程来自缺口接口 GET /api/teacher/my-classes、GET /api/teacher/my-courses
 */
import { computed, onMounted, ref, watch } from 'vue'
import { showToast } from 'vant'
import { getWeeklyReport, recordAttendance } from '@/api/attendance'
import type { StudentAttendanceRecord, WeeklyReportRow } from '@/api/attendance'
import { listMyClasses, listMyCourses, listStudentsByClass } from '@/api/base'
import type { ClassInfo, Course, Student } from '@/api/base'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import { STUDENT_ATTENDANCE_STATUS } from '@/constants/dict'
import { todayStr } from '@/utils/format'

const userStore = useUserStore()
/** 教师工号；0 = 业务身份未解析成功 */
const teacherId = computed(() => userStore.businessId)
const today = todayStr()
const maxDate = new Date()
/** 班级 / 日期选择器共用一个弹层，用 target 区分「点名录入」与「本周报表」 */
const pickerTarget = ref<'entry' | 'report'>('entry')

const classId = ref(0)
const courseId = ref(0)
const attendanceDate = ref(today)
const reportClassId = ref(0)
const reportDate = ref(today)
/** 学生点名标记：studentId → 状态 */
const marks = ref<Record<number, string>>({})
const saving = ref(false)

const { data: classes, loading: classLoading, error: classError, reload: reloadClasses } =
  useAsyncData<ClassInfo[]>(() => (teacherId.value ? listMyClasses(teacherId.value) : Promise.resolve([])), [])
const { data: courses, loading: courseLoading, error: courseError, reload: reloadCourses } =
  useAsyncData<Course[]>(() => (teacherId.value ? listMyCourses(teacherId.value) : Promise.resolve([])), [])
const { data: students, loading: studentLoading, error: studentError, reload: reloadStudents } =
  useAsyncData<Student[]>(() => (classId.value ? listStudentsByClass(classId.value) : Promise.resolve([])), [])
const { data: report, loading: reportLoading, error: reportError, reload: reloadReport } =
  useAsyncData<WeeklyReportRow[]>(
    () => (reportClassId.value ? getWeeklyReport(reportClassId.value, reportDate.value) : Promise.resolve([])),
    [],
  )

/** 按当前学生列表重建标记；status 为空时保留已改动，否则统一设为该状态 */
function buildMarks(status?: string): Record<number, string> {
  const next: Record<number, string> = {}
  for (const student of students.value) {
    if (student.id) {
      next[student.id] = status ?? marks.value[student.id] ?? '正常'
    }
  }
  return next
}

/** 学生列表变化后重建标记（默认「正常」，保留已改动） */
watch(students, () => {
  marks.value = buildMarks()
})

/** 已标记为异常（非「正常」）的人数 */
const abnormalCount = computed(
  () => Object.values(marks.value).filter((status) => status !== '正常').length,
)

function markAllNormal() {
  marks.value = buildMarks('正常')
  showToast('已全部标记为正常')
}

async function submitRecords() {
  if (!teacherId.value) {
    showToast('未解析到教师工号')
    return
  }
  if (!classId.value || !courseId.value) {
    showToast('请先选择班级与课程')
    return
  }
  const records: StudentAttendanceRecord[] = []
  for (const student of students.value) {
    if (!student.id) {
      continue
    }
    records.push({
      studentId: student.id,
      courseId: courseId.value,
      attendanceDate: attendanceDate.value,
      status: marks.value[student.id] ?? '正常',
    })
  }
  if (records.length === 0) {
    showToast('该班级暂无学生')
    return
  }
  saving.value = true
  try {
    await recordAttendance(records)
    showToast(`已提交 ${records.length} 名学生的考勤`)
    marks.value = buildMarks('正常') // 清空异常标记
    reloadReport()
  } catch {
    // 失败原因（缺学生/课程/日期）由请求层统一 toast
  } finally {
    saving.value = false
  }
}

/* ------------------------------ 选择器 ------------------------------ */
const showClassPicker = ref(false)
const showCoursePicker = ref(false)
const showDatePicker = ref(false)

const classText = computed(() => classes.value.find((item) => item.id === classId.value)?.name ?? '')
const courseText = computed(() => courses.value.find((item) => item.id === courseId.value)?.name ?? '')
const reportClassText = computed(
  () => classes.value.find((item) => item.id === reportClassId.value)?.name ?? '',
)
const classColumns = computed(() => classes.value.map((item) => ({ text: item.name, value: item.id })))
const courseColumns = computed(() => courses.value.map((item) => ({ text: item.name, value: item.id })))
const pickerClassId = computed(() => (pickerTarget.value === 'entry' ? classId.value : reportClassId.value))
const pickerDateValues = computed(() =>
  (pickerTarget.value === 'entry' ? attendanceDate.value : reportDate.value).split('-'),
)

interface PickerPayload {
  selectedOptions?: Array<{ value?: string | number } | undefined>
}

function pickId(payload: PickerPayload): number {
  return Number(payload.selectedOptions?.[0]?.value ?? 0)
}

function openClassPicker(target: 'entry' | 'report') {
  if (!classes.value.length) {
    showToast('暂无任教班级')
    return
  }
  pickerTarget.value = target
  showClassPicker.value = true
}

function openCoursePicker() {
  if (!courses.value.length) {
    showToast('暂无任教课程')
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

/** 工号未解析时重试解析业务身份并重新取数 */
async function retryProfile() {
  await userStore.resolveProfile(true)
  reloadClasses()
  reloadCourses()
  reloadStudents()
  reloadReport()
}

onMounted(() => {
  reloadClasses()
  reloadCourses()
})
</script>

<template>
  <div>
    <van-empty v-if="!teacherId" description="未解析到教师工号">
      <van-button round type="primary" size="small" @click="retryProfile">重新解析身份</van-button>
    </van-empty>

    <van-tabs v-else>
      <!-- 点名录入 -->
      <van-tab title="点名录入" name="entry">
        <div v-if="classLoading || courseLoading" class="st-empty">
          <van-loading vertical>加载中…</van-loading>
        </div>
        <van-empty v-else-if="classError || courseError" image="error" :description="classError || courseError" />
        <van-empty v-else-if="!classes.length || !courses.length"
          :description="classes.length ? '暂无任教课程' : '暂无任教班级'" />
        <template v-else>
          <div class="st-card">
            <van-field readonly is-link label="班级" placeholder="请选择班级" :model-value="classText"
              @click="openClassPicker('entry')" />
            <van-field readonly is-link label="课程" placeholder="请选择课程" :model-value="courseText"
              @click="openCoursePicker" />
            <van-field readonly is-link label="上课日期" :model-value="attendanceDate"
              @click="openDatePicker('entry')" />
          </div>

          <div class="st-row sa__bar">
            <span class="st-muted">已标记异常 {{ abnormalCount }} 人 / 共 {{ students.length }} 人</span>
            <van-button size="mini" plain type="primary" @click="markAllNormal">全部标记为正常</van-button>
          </div>

          <div v-if="studentLoading" class="st-empty">
            <van-loading vertical>加载中…</van-loading>
          </div>
          <van-empty v-else-if="studentError" image="error" :description="studentError" />
          <van-empty v-else-if="!classId" description="请先选择班级" />
          <van-empty v-else-if="students.length === 0" description="该班级暂无学生" />
          <template v-else>
            <div v-for="student in students" :key="student.id" class="st-card">
              <div class="st-row">
                <span class="sa__name">{{ student.name }}</span>
                <span class="st-muted">{{ student.studentNo }}</span>
              </div>
              <van-radio-group v-model="marks[student.id ?? 0]" direction="horizontal" class="sa__options">
                <van-radio v-for="status in STUDENT_ATTENDANCE_STATUS" :key="status" :name="status">
                  {{ status }}
                </van-radio>
              </van-radio-group>
            </div>
            <van-button round block type="primary" :loading="saving" @click="submitRecords">
              提交考勤
            </van-button>
          </template>
        </template>
      </van-tab>

      <!-- 本周报表 -->
      <van-tab title="本周报表" name="report">
        <div v-if="classLoading" class="st-empty">
          <van-loading vertical>加载中…</van-loading>
        </div>
        <van-empty v-else-if="!classes.length" :description="classError || '暂无任教班级'" />
        <template v-else>
          <div class="st-card">
            <van-field readonly is-link label="班级" placeholder="请选择班级" :model-value="reportClassText"
              @click="openClassPicker('report')" />
            <van-field readonly is-link label="所在周" :model-value="reportDate"
              @click="openDatePicker('report')" />
          </div>

          <div v-if="reportLoading" class="st-empty">
            <van-loading vertical>加载中…</van-loading>
          </div>
          <van-empty v-else-if="reportError" image="error" :description="reportError">
            <van-button round type="primary" size="small" @click="reloadReport">重新加载</van-button>
          </van-empty>
          <van-empty v-else-if="!reportClassId" description="请先选择班级" />
          <van-empty v-else-if="report.length === 0" description="本周暂无考勤数据" />
          <template v-else>
            <div v-for="row in report" :key="row.studentId ?? row.studentNo" class="st-card">
              <div class="st-row">
                <span class="sa__name">{{ row.studentName || `学生#${row.studentId}` }}</span>
                <span class="st-muted">{{ row.studentNo || '—' }}</span>
              </div>
              <div class="sa__report">
                <span>正常 {{ row.normal ?? 0 }}</span>
                <span>迟到 {{ row.late ?? 0 }}</span>
                <span>缺勤 {{ row.absent ?? 0 }}</span>
                <span>请假 {{ row.leave ?? 0 }}</span>
                <span>出勤率 {{ rateText(row.rate) }}</span>
              </div>
            </div>
          </template>
        </template>
      </van-tab>
    </van-tabs>

    <!-- 班级 / 课程 / 日期选择器 -->
    <van-popup v-model:show="showClassPicker" position="bottom" round>
      <van-picker title="选择班级" :columns="classColumns" :model-value="[pickerClassId]"
        @confirm="onClassConfirm" @cancel="showClassPicker = false" />
    </van-popup>
    <van-popup v-model:show="showCoursePicker" position="bottom" round>
      <van-picker title="选择课程" :columns="courseColumns" :model-value="[courseId]"
        @confirm="onCourseConfirm" @cancel="showCoursePicker = false" />
    </van-popup>
    <van-popup v-model:show="showDatePicker" position="bottom" round>
      <van-date-picker title="选择日期" :model-value="pickerDateValues" :max-date="maxDate"
        @confirm="onDateConfirm" @cancel="showDatePicker = false" />
    </van-popup>
  </div>
</template>

<style scoped>
.sa__bar { margin: 0 4px 10px; }
.sa__options { margin-top: 10px; }
.sa__name { font-size: 15px; font-weight: 600; }
.sa__report { display: flex; flex-wrap: wrap; gap: 4px 12px; margin-top: 8px; font-size: 12px; color: var(--st-text-light); }
</style>
