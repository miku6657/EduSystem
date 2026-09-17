<script setup lang="ts">
/**
 * 教师 · 教学日志
 * 数据源：GET /api/teaching-log/list-by-teacher-week?teacherId&date（date 传该周任意一天）
 *         POST /api/teaching-log
 * 结构与 student/scores.vue 一致：顶部卡片头（PageHeader + StatBar）+ useAsyncData +
 * 显式 import + Vant 组件 + PageState 三态。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import {
  addTeachingLog,
  listMyTeachingLogs,
  listTeachingClasses,
  listTeachingCourses,
} from '@/api/teachingLog'

import type {
  TeachingClass,
  TeachingCourse,
  TeachingLog,
} from '@/api/teachingLog'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import PageState from '@/components/PageState.vue'
import StatBar from '@/components/StatBar.vue'
import { addDays, todayStr, weekRangeOf } from '@/utils/format'

const userStore = useUserStore()
/** 教师工号；0 = 业务身份未解析成功（提示并禁止请求/提交） */
const teacherId = computed(() => userStore.businessId)
const today = todayStr()
/** 授课日期不得晚于今天，故日期选择器最大值取今天 */
const maxDate = new Date()
/** 当前查看的周：用该周任意一天做锚点 */
const weekDate = ref(today)
const weekRange = computed(() => weekRangeOf(weekDate.value))
const isThisWeek = computed(() => weekRange.value.start === weekRangeOf(today).start)

const {
  data: logs,
  loading,
  error,
  reload,
} = useAsyncData<TeachingLog[]>(
  () =>
    teacherId.value ? listMyTeachingLogs(teacherId.value, weekDate.value) : Promise.resolve([]),
  [],
)
const {
  data: courses,
  reload: reloadCourses,
} =
  useAsyncData<
    TeachingCourse[]
  >(
    () =>
      listTeachingCourses(),
    [],
  )
const {
  data: classes,
  reload: reloadClasses,
} =
  useAsyncData<
    TeachingClass[]
  >(
    () =>
      listTeachingClasses(),
    [],
  )

/** 统计条数据（对齐公共组件 StatBar）：本页日志数与覆盖课程数 */
const summaryItems = computed(() => [
  { label: '已提交日志', value: logs.value.length },
  { label: '涉及课程', value: new Set(logs.value.map((log) => log.courseId)).size },
])

/* ------------------------------ 新增表单 ------------------------------ */
const showForm = ref(false)
const showCourse = ref(false)
const showClass = ref(false)
const showDate = ref(false)
const submitting = ref(false)
const form =
  reactive({
    courseId: '',

    classId: '',

    teachingDate:
      today,

    content: '',

    homework: '',
  })

const courseText =
  computed(
    () =>
      courses.value.find(
        (item) =>
          String(item.id)
          ===
          form.courseId,
      )?.name
      ?? '',
  )
const classText =
  computed(
    () =>
      classes.value.find(
        (item) =>
          String(item.id)
          ===
          form.classId,
      )?.name
      ?? '',
  )
const courseColumns =
  computed(() =>
    courses.value.map(
      (item) => ({
        text:
          `${item.name}（${item.courseCode}）`,

        value:
          String(item.id),
      }),
    ),
  )
const classColumns =
  computed(() =>
    classes.value.map(
      (item) => ({
        text:
          item.grade
            ? `${item.name}（${item.grade}级）`
            : item.name,

        value:
          String(item.id),
      }),
    ),
  )
/** van-date-picker 的值形如 ['2026','09','11'] */
const dateValues = computed(() => form.teachingDate.split('-'))
/** 长文折叠：记录已展开的日志 id */
const expanded = reactive<Record<number, boolean>>({})

interface PickerPayload {
  selectedOptions?: Array<{ value?: string | number } | undefined>
}

function pickId(
  payload: PickerPayload,
): string {

  const value =
    payload
      .selectedOptions
      ?.[0]
      ?.value

  return value === null
  || value === undefined
    ? ''
    : String(value)
}

function openCoursePicker() {
  if (!courses.value.length) {
    showToast('系统暂无课程数据')
    return
  }
  showCourse.value = true
}

function openClassPicker() {
  if (!classes.value.length) {
    showToast('系统暂无班级数据')
    return
  }
  showClass.value = true
}

function onCourseConfirm(payload: PickerPayload) {
  form.courseId = pickId(payload)
  showCourse.value = false
}

function onClassConfirm(payload: PickerPayload) {
  form.classId = pickId(payload)
  showClass.value = false
}

function onDateConfirm(payload: { selectedValues?: string[] }) {
  const values = payload.selectedValues ?? []
  showDate.value = false
  if (values.length === 3) {
    form.teachingDate = values.join('-')
  }
}

function shiftWeek(days: number) {
  weekDate.value = addDays(weekDate.value, days)
  reload()
}

function goThisWeek() {
  weekDate.value = today
  reload()
}

/** 打开表单：默认第一门课程 / 第一个班级，日期默认今天 */
function openForm() {
  if (courses.value.length === 0) {
    showToast('系统暂无课程数据')
    return
  }

  if (classes.value.length === 0) {
    showToast('系统暂无班级数据')
    return
  }

  form.courseId = String(courses.value[0]?.id ?? '')
  form.classId = String(classes.value[0]?.id ?? '')
  form.teachingDate = today
  form.content = ''
  form.homework = ''
  showForm.value = true
}

function toggleContent(id?: number) {
  if (id !== undefined) {
    expanded[id] = !expanded[id]
  }
}

async function onSubmit() {
  if (!teacherId.value) {
    showToast('当前账号未绑定教师档案')
    return
  }
  if (!form.courseId) {
    showToast('请选择课程')
    return
  }
  if (!form.classId) {
    showToast('请选择班级')
    return
  }
  if (!form.content.trim()) {
    showToast('请填写授课内容')
    return
  }
  submitting.value = true
  try {
    await addTeachingLog({
      teacherId: String(teacherId.value),
      courseId: form.courseId,
      classId: form.classId,
      teachingDate: form.teachingDate,
      content: form.content.trim(),
      homework: form.homework.trim() || undefined,
    })
    showToast('教学日志已提交')
    showForm.value = false
    /**
     * 切换到刚提交日志所在周。
     */
    weekDate.value = form.teachingDate
    await reload()
  } catch {
    // request.ts统一提示
  } finally {
    submitting.value = false
  }
}

/** 工号未解析时重试解析业务身份 */
async function retryProfile() {
  await userStore.resolveProfile(true)
  reload()
  reloadCourses()
  reloadClasses()
}

onMounted(() => {
  reload()
  reloadCourses()
  reloadClasses()
})
</script>

<template>
  <div>
    <!-- businessId 为 0：明确提示，不发无意义的请求 -->
    <van-empty v-if="!teacherId" description="未解析到教师工号">
      <van-button round type="primary" size="small" @click="retryProfile">重新解析身份</van-button>
    </van-empty>

    <template v-else>
      <!-- 顶部：标题 + 主操作 + 周范围 + 概览（对齐 admin 的卡片头结构） -->
      <div class="st-card">
        <PageHeader title="教学日志">
          <template #actions>
            <van-button size="small" type="primary" icon="plus" @click="openForm">
              新增日志
            </van-button>
          </template>
        </PageHeader>

        <div class="log__week">
          <div class="log__week-range">{{ weekRange.start }} ~ {{ weekRange.end }}</div>
          <div class="st-muted">{{ isThisWeek ? '本周' : '历史周' }}</div>
        </div>

        <StatBar :items="summaryItems" />

        <div class="st-row log__nav">
          <van-button size="small" plain type="primary" @click="shiftWeek(-7)">上一周</van-button>
          <van-button size="small" plain type="primary" @click="goThisWeek">本周</van-button>
          <van-button size="small" plain type="primary" @click="shiftWeek(7)">下一周</van-button>
        </div>
      </div>

      <PageState
        :loading="loading"
        :error="error"
        :empty="logs.length === 0"
        empty-text="本周还没有教学日志"
        @retry="reload"
      >
        <div
          v-for="log in logs"
          :key="log.id ?? `${log.teachingDate}-${log.courseId}`"
          class="st-card"
        >
          <div class="st-row">
            <div class="log__title">{{ log.courseName || `课程#${log.courseId}` }}</div>
            <span class="st-muted">{{ log.teachingDate }}</span>
          </div>
          <div class="st-muted log__meta">{{ log.className || `班级#${log.classId}` }}</div>
          <div class="log__content" :class="{ 'log__content--fold': !expanded[log.id ?? -1] }">
            {{ log.content }}
          </div>
          <div v-if="log.homework" class="log__homework">作业：{{ log.homework }}</div>
          <div
            v-if="(log.content ?? '').length > 40"
            class="log__toggle"
            @click="toggleContent(log.id)"
          >
            {{ expanded[log.id ?? -1] ? '收起' : '展开' }}
          </div>
        </div>
      </PageState>
    </template>

    <!-- 新增教学日志 -->
    <van-popup v-model:show="showForm" position="bottom" round :style="{ height: '80%' }">
      <div class="log__form-title">新增教学日志</div>
      <van-form class="log__form" @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            readonly
            is-link
            name="course"
            label="课程"
            placeholder="请选择课程"
            :model-value="courseText"
            :rules="[{ required: true, message: '请选择课程' }]"
            @click="openCoursePicker"
          />
          <van-field
            readonly
            is-link
            name="class"
            label="班级"
            placeholder="请选择班级"
            :model-value="classText"
            :rules="[{ required: true, message: '请选择班级' }]"
            @click="openClassPicker"
          />
          <van-field
            readonly
            is-link
            name="teachingDate"
            label="授课日期"
            :model-value="form.teachingDate"
            @click="showDate = true"
          />
          <van-field
            v-model="form.content"
            name="content"
            label="授课内容"
            type="textarea"
            rows="3"
            autosize
            maxlength="500"
            placeholder="请填写本次授课内容"
            :rules="[{ required: true, message: '请填写授课内容' }]"
          />
          <van-field
            v-model="form.homework"
            name="homework"
            label="作业布置"
            type="textarea"
            rows="2"
            autosize
            maxlength="200"
            placeholder="选填"
          />
        </van-cell-group>
        <div class="log__submit">
          <van-button round block type="primary" native-type="submit" :loading="submitting"
            >提交</van-button
          >
        </div>
        <div v-if="!courses.length || !classes.length" class="log__tip st-muted">
          系统暂无课程或班级基础数据。
        </div>
      </van-form>
    </van-popup>

    <!-- 课程 / 班级 / 日期选择器 -->
    <van-popup v-model:show="showCourse" position="bottom" round>
      <van-picker
        title="选择课程"
        :columns="courseColumns"
        :model-value="[form.courseId]"
        @confirm="onCourseConfirm"
        @cancel="showCourse = false"
      />
    </van-popup>
    <van-popup v-model:show="showClass" position="bottom" round>
      <van-picker
        title="选择班级"
        :columns="classColumns"
        :model-value="[form.classId]"
        @confirm="onClassConfirm"
        @cancel="showClass = false"
      />
    </van-popup>
    <van-popup v-model:show="showDate" position="bottom" round>
      <van-date-picker
        title="选择授课日期"
        :model-value="dateValues"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDate = false"
      />
    </van-popup>
  </div>
</template>

<style scoped>
.log__week {
  margin-bottom: 12px;
  text-align: center;
}
.log__week-range {
  font-size: 14px;
  font-weight: 600;
}
.log__nav {
  margin-top: 12px;
}
.log__title {
  font-size: 15px;
  font-weight: 600;
}
.log__meta {
  margin-top: 4px;
}
.log__content {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
}
/* 长文折叠：默认 2 行省略 */
.log__content--fold {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.log__homework {
  padding: 6px 8px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--st-text-light);
  background: var(--st-bg);
  border-radius: 6px;
}
.log__toggle {
  margin-top: 6px;
  font-size: 12px;
  color: var(--st-primary);
  text-align: right;
}
.log__form-title {
  padding: 14px 0;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
}
.log__submit {
  margin: 16px;
}
.log__tip {
  margin: 0 16px 16px;
  text-align: center;
}
</style>
