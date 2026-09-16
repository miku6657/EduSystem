<script setup lang="ts">
/**
 * 教师 · 我的监考
 * 数据源：GET /api/exam-monitor/list-by-teacher/{teacherId}
 * 按考试日期分组：今天及以后的排前面，已过期的置灰并标记「已结束」。
 *
 * 结构：顶部卡片头（PageHeader + StatBar）+ PageState 三态。
 */
import { computed, onMounted } from 'vue'
import { listMyInvigilations } from '@/api/examMonitor'
import type { ExamMonitor } from '@/api/examMonitor'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import PageState from '@/components/PageState.vue'
import StatBar from '@/components/StatBar.vue'
import { MONITOR_ROLE_TEXT } from '@/constants/dict'
import { currentWeekRange, formatDate, todayStr } from '@/utils/format'

const userStore = useUserStore()
/** 教师工号；0 = 业务身份未解析成功 */
const teacherId = computed(() => userStore.businessId)
const today = todayStr()

const {
  data: monitors,
  loading,
  error,
  reload,
} = useAsyncData<ExamMonitor[]>(
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

/** 统计条里的「最近一场」只放日期（MM-DD），完整时间放下面一行 muted 文案，避免 20px 字号折行 */
const nextShort = computed(() => {
  const exam = nextExam.value
  return exam?.examDate ? formatDate(exam.examDate).slice(5) : '—'
})

/** 统计条数据（对齐公共组件 StatBar）：数值型指标放 StatBar，长文本另起一行 */
const summaryItems = computed(() => [
  { label: '本周监考场次', value: weekCount.value },
  { label: '累计监考场次', value: monitors.value.length },
  { label: '最近一场', value: nextShort.value },
])

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
      <!-- 顶部：标题 + 概览（对齐 admin 的卡片头结构） -->
      <div class="st-card">
        <PageHeader title="我的监考" />
        <StatBar :items="summaryItems" />
        <div v-if="nextExam" class="st-muted inv__next">下一场：{{ nextText }}</div>
      </div>

      <PageState
        :loading="loading"
        :error="error"
        :empty="groups.length === 0"
        empty-text="暂无监考安排"
        @retry="reload"
      >
        <div
          v-for="group in groups"
          :key="group.date"
          class="st-card"
          :class="{ 'inv__group--finished': group.finished }"
        >
          <div class="st-row">
            <span class="inv__date">{{ group.date }}</span>
            <van-tag v-if="group.finished" plain>已结束</van-tag>
            <van-tag v-else type="primary" plain>{{
              group.date === today ? '今天' : '待监考'
            }}</van-tag>
          </div>
          <div
            v-for="item in group.items"
            :key="item.id ?? `${item.examId}-${item.teacherId}`"
            class="inv__item"
          >
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
      </PageState>
    </template>
  </div>
</template>

<style scoped>
.inv__date {
  font-size: 15px;
  font-weight: 600;
}
.inv__item {
  padding-top: 10px;
  margin-top: 10px;
  border-top: 1px solid var(--st-border);
}
.inv__exam {
  font-size: 14px;
  font-weight: 600;
}
.inv__meta {
  margin: 4px 0;
}
.inv__next {
  margin-top: 10px;
}
/* 已过期的监考置灰 */
.inv__group--finished {
  color: var(--st-text-light);
  background: var(--st-bg);
}
</style>
