<script setup lang="ts">
/**
 * 师生端 · 课表 / 我的教学任务（学生与教师共用）
 * 学生：GET /api/teaching-tasks?classId=（按所在班级）
 * 教师：GET /api/teaching-tasks?teacherId=（按本人任教关系）
 *
 * 说明：任课表 base_teaching_task 里带了 weekday / 节次 / 教室 / 周次，
 * 这里按星期分组展示；未排时间的任务单独归入"待排课"。
 *
 * 结构：顶部卡片头（PageHeader + StatBar）+ 下拉刷新 + PageState 三态。
 */
import { computed, onMounted } from 'vue'
import { listTasksByClass, listTasksByTeacher, WEEKDAY_TEXT } from '@/api/teachingTask'
import type { TeachingTask } from '@/api/teachingTask'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import PageState from '@/components/PageState.vue'
import StatBar from '@/components/StatBar.vue'

const userStore = useUserStore()

/** 学生所在班级（profile 里有 classId） */
const studentClassId = computed(() => {
  const profile = userStore.profile
  if (profile && 'classId' in profile) {
    return profile.classId ?? 0
  }
  return 0
})

/** 是否有可用于查询的身份 */
const canLoad = computed(() =>
  userStore.isTeacher ? userStore.businessId > 0 : studentClassId.value > 0,
)

const {
  data: tasks,
  loading,
  error,
  reload,
} = useAsyncData<TeachingTask[]>(() => {
  if (userStore.isTeacher) {
    return userStore.businessId ? listTasksByTeacher(userStore.businessId) : Promise.resolve([])
  }
  return studentClassId.value ? listTasksByClass(studentClassId.value) : Promise.resolve([])
}, [])

/** 按星期分组（1~7 顺序），未排时间的单独一组 */
const grouped = computed(() => {
  const groups: Array<{ weekday: number; title: string; rows: TeachingTask[] }> = []
  for (let weekday = 1; weekday <= 7; weekday += 1) {
    const rows = tasks.value.filter((task) => Number(task.weekday) === weekday)
    if (rows.length > 0) {
      groups.push({ weekday, title: WEEKDAY_TEXT[weekday] ?? `周${weekday}`, rows })
    }
  }
  return groups
})

const unscheduled = computed(() => tasks.value.filter((task) => !task.weekday))

/** 概览：课程门数（去重）/ 每周节数 */
const summary = computed(() => {
  const courseIds = new Set(tasks.value.map((task) => task.courseId))
  const sections = tasks.value.reduce((sum, task) => {
    const start = Number(task.startSection ?? 0)
    const end = Number(task.endSection ?? 0)
    return sum + (start && end && end >= start ? end - start + 1 : 0)
  }, 0)
  return { courses: courseIds.size, sections }
})

/** 统计条数据（对齐公共组件 StatBar） */
const summaryItems = computed(() => [
  { label: '课程门数', value: summary.value.courses },
  { label: '每周节数', value: summary.value.sections },
  { label: '有课天数', value: grouped.value.length },
])

function slotText(task: TeachingTask): string {
  if (!task.startSection && !task.endSection) {
    return '待排课'
  }
  return `第 ${task.startSection ?? '?'}-${task.endSection ?? '?'} 节`
}

onMounted(reload)
</script>

<template>
  <div>
    <van-empty v-if="!canLoad" description="未解析到班级或教师身份，请确认登录账号为学号/工号">
      <van-button round type="primary" size="small" @click="userStore.resolveProfile(true)">
        重新解析身份
      </van-button>
    </van-empty>

    <template v-else>
      <!-- 顶部：标题 + 概览（对齐 admin 的卡片头结构） -->
      <div class="st-card">
        <PageHeader title="课表" />
        <StatBar :items="summaryItems" />
      </div>

      <van-pull-refresh :model-value="false" @refresh="reload">
        <PageState
          :loading="loading"
          :error="error"
          :empty="tasks.length === 0"
          empty-text="暂无排课记录"
          @retry="reload"
        >
          <template v-for="group in grouped" :key="group.weekday">
            <div class="st-section-title">{{ group.title }}</div>
            <div
              v-for="task in group.rows"
              :key="task.id ?? `${task.courseId}-${task.classId}`"
              class="st-card"
            >
              <div class="st-row">
                <div class="tt__course">{{ task.courseName || `课程#${task.courseId}` }}</div>
                <van-tag type="primary" plain>{{ slotText(task) }}</van-tag>
              </div>
              <div class="tt__meta st-muted">
                <template v-if="userStore.isTeacher">{{
                  task.className || `班级#${task.classId}`
                }}</template>
                <template v-else>{{ task.teacherName || '教师待定' }}</template>
                <template v-if="task.roomName"> · {{ task.roomName }}</template>
                <template v-if="task.weeks"> · {{ task.weeks }}</template>
              </div>
            </div>
          </template>

          <template v-if="unscheduled.length > 0">
            <div class="st-section-title">待排课（未安排上课时间）</div>
            <div v-for="task in unscheduled" :key="`u-${task.id ?? task.courseId}`" class="st-card">
              <div class="st-row">
                <div class="tt__course">{{ task.courseName || `课程#${task.courseId}` }}</div>
                <van-tag type="warning" plain>待排课</van-tag>
              </div>
              <div class="tt__meta st-muted">
                <template v-if="userStore.isTeacher">{{
                  task.className || `班级#${task.classId}`
                }}</template>
                <template v-else>{{ task.teacherName || '教师待定' }}</template>
              </div>
            </div>
          </template>
        </PageState>
      </van-pull-refresh>
    </template>
  </div>
</template>

<style scoped>
.tt__course {
  font-size: 15px;
  font-weight: 600;
}

.tt__meta {
  margin-top: 6px;
}
</style>
