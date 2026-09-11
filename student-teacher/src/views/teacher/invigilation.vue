<script setup lang="ts">
/**
 * 教师 · 我的监考
 * 数据源：GET /api/exam-monitor/list-by-teacher/{teacherId}
 * 按考试日期分组：今天及以后的排前面，已过期的置灰并标记「已结束」。
 */
import { computed, onMounted } from 'vue'
import { listMyInvigilations } from '@/api/examMonitor'
import type { ExamMonitor } from '@/api/examMonitor'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import { MONITOR_ROLE_TEXT } from '@/constants/dict'
import { currentWeekRange, formatDate, todayStr } from '@/utils/format'

const userStore = useUserStore()
/** 教师工号；0 = 业务身份未解析成功 */
const teacherId = computed(() => userStore.businessId)
const today = todayStr()

const { data: monitors, loading, error, reload } = useAsyncData<ExamMonitor[]>(
  () => (teacherId.value ? listMyInvigilations(teacherId.value) : Promise.resolve([])),
  [],
)

/** 按考试日期分组（同一天一张卡） */
const groups = computed(() => {
  const map = new Map<string, ExamMonitor[]>()
  for (const item of monitors.value) {
    const date = item.examDate || '待安排'
    const list = map.get(date)
    if (list) {
      list.push(item)
    } else {
      map.set(date, [item])
    }
  }
  return [...map.entries()]
    .map(([date, items]) => ({ date, finished: date < today, items }))
    .sort((a, b) => {
      if (a.finished !== b.finished) {
        return a.finished ? 1 : -1
      }
      // 未结束的按日期升序（最近的在前），已结束的按日期降序（新近的在前）
      return a.finished ? b.date.localeCompare(a.date) : a.date.localeCompare(b.date)
    })
})

/** 本周监考场次 */
const weekCount = computed(() => {
  const range = currentWeekRange()
  return monitors.value.filter(
    (item) => item.examDate && item.examDate >= range.start && item.examDate <= range.end,
  ).length
})

/** 最近一场：今天及以后最早的一场（无则为 undefined） */
const nextExam = computed(() =>
  monitors.value
    .filter((item) => item.examDate && item.examDate >= today)
    .sort((a, b) => String(a.examDate).localeCompare(String(b.examDate)))
    .at(0),
)

const nextText = computed(() => {
  const exam = nextExam.value
  if (!exam) {
    return '暂无待监考安排'
  }
  const time = exam.startTime && exam.endTime ? ` ${exam.startTime}~${exam.endTime}` : ''
  return `${formatDate(exam.examDate)}${time}`
})

/** 工号未解析时重试解析业务身份 */
async function retryProfile() {
  await userStore.resolveProfile(true)
  reload()
}

onMounted(reload)
</script>

<template>
  <div>
    <van-empty v-if="!teacherId" description="未解析到教师工号">
      <van-button round type="primary" size="small" @click="retryProfile">重新解析身份</van-button>
    </van-empty>

    <template v-else>
      <!-- 顶部统计 -->
      <div class="st-card inv__summary">
        <div class="inv__summary-item">
          <div class="inv__summary-value">{{ weekCount }}</div>
          <div class="st-muted">本周监考场次</div>
        </div>
        <div class="inv__summary-item">
          <div class="inv__summary-next">{{ nextText }}</div>
          <div class="st-muted">最近一场</div>
        </div>
      </div>

      <div v-if="loading" class="st-empty">
        <van-loading vertical>加载中…</van-loading>
      </div>

      <van-empty v-else-if="error" image="error" :description="error">
        <van-button round type="primary" size="small" @click="reload">重新加载</van-button>
      </van-empty>

      <van-empty v-else-if="groups.length === 0" description="暂无监考安排" />

      <template v-else>
        <div v-for="group in groups" :key="group.date" class="st-card"
          :class="{ 'inv__group--finished': group.finished }">
          <div class="st-row">
            <span class="inv__date">{{ group.date }}</span>
            <van-tag v-if="group.finished" plain>已结束</van-tag>
            <van-tag v-else type="primary" plain>{{ group.date === today ? '今天' : '待监考' }}</van-tag>
          </div>
          <div v-for="item in group.items" :key="item.id ?? `${item.examId}-${item.teacherId}`"
            class="inv__item">
            <div class="st-row">
              <span class="inv__exam">{{ item.examName || `考试#${item.examId}` }}</span>
              <van-tag :type="item.monitorRole === 'MAIN' ? 'danger' : 'primary'">
                {{ MONITOR_ROLE_TEXT[item.monitorRole ?? ''] || '监考' }}
              </van-tag>
            </div>
            <div class="st-muted inv__meta">
              {{ item.courseName || '—' }} · {{ item.startTime || '—' }}~{{ item.endTime || '—' }}
            </div>
            <div class="st-muted">考场：{{ item.roomName || '待安排' }}</div>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.inv__summary { display: flex; justify-content: space-around; text-align: center; }
.inv__summary-value { font-size: 20px; font-weight: 600; }
.inv__summary-next { font-size: 13px; font-weight: 600; }
.inv__date { font-size: 15px; font-weight: 600; }
.inv__item { padding-top: 10px; margin-top: 10px; border-top: 1px solid var(--st-border); }
.inv__exam { font-size: 14px; font-weight: 600; }
.inv__meta { margin: 4px 0; }
/* 已过期的监考置灰 */
.inv__group--finished { color: var(--st-text-light); background: #fafafa; }
</style>
