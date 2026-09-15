<script setup lang="ts">
/**
 * 学生 · 我的成绩
 * 数据源：GET /api/score/list-by-student/{studentId}（后端已实现）
 *
 * 本页是师生端的**样式与写法样板**，其余页面请保持同样的结构：
 * 顶部卡片头（PageHeader + StatBar）+ useAsyncData + 显式 import + Vant 组件 +
 * 底部下拉刷新 + PageState 三态。
 */
import { computed, onMounted, ref } from 'vue'
import { listMyScores } from '@/api/score'
import type { ExamScore } from '@/api/score'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import PageState from '@/components/PageState.vue'
import StatBar from '@/components/StatBar.vue'
import { formatScore } from '@/utils/format'
import { PASS_SCORE_LINE, SCORE_STATUS_TEXT } from '@/constants/dict'

const userStore = useUserStore()

/** 筛选：全部 / 及格 / 不及格 */
const filter = ref<'all' | 'pass' | 'fail'>('all')
const refreshing = ref(false)

const {
  data: scores,
  loading,
  error,
  reload,
} = useAsyncData<ExamScore[]>(
  () => (userStore.businessId ? listMyScores(userStore.businessId) : Promise.resolve([])),
  [],
)

/** 按筛选条件展示 */
const visibleScores = computed(() =>
  scores.value.filter((row) => {
    if (filter.value === 'all') {
      return true
    }
    if (row.status === 'ABSENT') {
      return filter.value === 'fail'
    }
    const score = Number(row.score ?? 0)
    return filter.value === 'pass' ? score >= PASS_SCORE_LINE : score < PASS_SCORE_LINE
  }),
)

/** 概览：门数 / 平均分 / 及格率（缺考不计入平均分与及格率） */
const summary = computed(() => {
  const scored = scores.value.filter(
    (row) => row.status !== 'ABSENT' && row.score !== null && row.score !== undefined,
  )
  const average = scored.length
    ? scored.reduce((sum, row) => sum + Number(row.score), 0) / scored.length
    : 0
  const passed = scored.filter((row) => Number(row.score) >= PASS_SCORE_LINE).length
  return {
    total: scores.value.length,
    average: scored.length ? average.toFixed(1) : '—',
    passRate: scored.length ? Math.round((passed / scored.length) * 100) : 0,
  }
})

/** 统计条数据（对齐公共组件 StatBar） */
const summaryItems = computed(() => [
  { label: '考试门数', value: summary.value.total },
  { label: '平均分', value: summary.value.average },
  { label: '及格率', value: `${summary.value.passRate}%` },
])

/** 分数颜色：不及格红、及格蓝（统一使用全局令牌，颜色随 admin 主题） */
function scoreColor(row: ExamScore): string {
  if (row.status === 'ABSENT') {
    return 'var(--st-text-light)'
  }
  return Number(row.score ?? 0) >= PASS_SCORE_LINE ? 'var(--st-primary)' : 'var(--st-danger)'
}

async function onRefresh() {
  refreshing.value = true
  try {
    await reload()
  } finally {
    refreshing.value = false
  }
}

onMounted(reload)
</script>

<template>
  <div>
    <!-- 顶部：标题 + 概览（对齐 admin 的卡片头结构） -->
    <div class="st-card">
      <PageHeader title="我的成绩" />
      <StatBar :items="summaryItems" />
    </div>

    <van-tabs v-model:active="filter" sticky>
      <van-tab title="全部" name="all" />
      <van-tab title="及格" name="pass" />
      <van-tab title="不及格" name="fail" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <PageState
        :loading="loading && !refreshing"
        :error="error"
        :empty="visibleScores.length === 0"
        empty-text="暂无成绩记录"
        @retry="reload"
      >
        <div
          v-for="row in visibleScores"
          :key="row.id ?? `${row.examId}-${row.studentId}`"
          class="st-card"
        >
          <div class="st-row">
            <div class="score__name">{{ row.courseName || row.examName }}</div>
            <div class="score__value" :style="{ color: scoreColor(row) }">
              {{ row.status === 'ABSENT' ? SCORE_STATUS_TEXT.ABSENT : formatScore(row.score) }}
            </div>
          </div>
          <div class="st-row score__meta">
            <span class="st-muted">{{ row.examName }}</span>
            <span class="st-muted">
              <template v-if="row.credit">学分 {{ row.credit }} · </template>
              {{ row.examDate || '—' }}
            </span>
          </div>
          <div v-if="row.status && row.status !== 'NORMAL'" class="score__tag">
            <van-tag type="warning" plain>{{
              SCORE_STATUS_TEXT[row.status] || row.status
            }}</van-tag>
          </div>
        </div>
      </PageState>
    </van-pull-refresh>
  </div>
</template>

<style scoped>
.score__name {
  font-size: 15px;
  font-weight: 600;
}

.score__value {
  font-size: 18px;
  font-weight: 600;
}

.score__meta {
  margin-top: 6px;
}

.score__tag {
  margin-top: 8px;
}
</style>
