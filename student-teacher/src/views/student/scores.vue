<script setup lang="ts">
/**
 * 学生 · 我的成绩
 * 数据源：GET /api/score/list-by-student/{studentId}（后端已实现）
 *
 * 本页是师生端的**样式与写法样板**，其余页面请保持同样的结构：
 * useAsyncData + 显式 import + Vant 组件 + 底部下拉刷新 + 空/错误状态。
 */
import { computed, onMounted, ref } from 'vue'
import { listMyScores } from '@/api/score'
import type { ExamScore } from '@/api/score'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import { formatScore } from '@/utils/format'
import { PASS_SCORE_LINE, SCORE_STATUS_TEXT } from '@/constants/dict'

const userStore = useUserStore()

/** 筛选：全部 / 及格 / 不及格 */
const filter = ref<'all' | 'pass' | 'fail'>('all')
const refreshing = ref(false)

const { data: scores, loading, error, reload } = useAsyncData<ExamScore[]>(
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

/** 分数颜色：不及格红、及格蓝 */
function scoreColor(row: ExamScore): string {
  if (row.status === 'ABSENT') {
    return '#969799'
  }
  return Number(row.score ?? 0) >= PASS_SCORE_LINE ? '#1989fa' : '#ee0a24'
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
    <!-- 概览 -->
    <div class="st-card score__summary">
      <div class="score__summary-item">
        <div class="score__summary-value">{{ summary.total }}</div>
        <div class="st-muted">考试门数</div>
      </div>
      <div class="score__summary-item">
        <div class="score__summary-value">{{ summary.average }}</div>
        <div class="st-muted">平均分</div>
      </div>
      <div class="score__summary-item">
        <div class="score__summary-value">{{ summary.passRate }}%</div>
        <div class="st-muted">及格率</div>
      </div>
    </div>

    <van-tabs v-model:active="filter" sticky>
      <van-tab title="全部" name="all" />
      <van-tab title="及格" name="pass" />
      <van-tab title="不及格" name="fail" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="loading && !refreshing" class="st-empty">
        <van-loading vertical>加载中…</van-loading>
      </div>

      <van-empty v-else-if="error" image="error" :description="error">
        <van-button round type="primary" size="small" @click="reload">重新加载</van-button>
      </van-empty>

      <van-empty v-else-if="visibleScores.length === 0" description="暂无成绩记录" />

      <template v-else>
        <div v-for="row in visibleScores" :key="row.id ?? `${row.examId}-${row.studentId}`" class="st-card">
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
            <van-tag type="warning" plain>{{ SCORE_STATUS_TEXT[row.status] || row.status }}</van-tag>
          </div>
        </div>
      </template>
    </van-pull-refresh>
  </div>
</template>

<style scoped>
.score__summary {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.score__summary-value {
  font-size: 20px;
  font-weight: 600;
}

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
