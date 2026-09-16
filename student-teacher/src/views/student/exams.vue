<script setup lang="ts">
/**
 * 学生 · 考试信息（考试查询）
 * 数据源：GET /api/exams?pageNo&pageSize&name&termId（考试名称 + 学期 + 起止时间）
 *         GET /api/terms（把 termId 映射成学期名称）
 *
 * 页面结构对齐 admin：卡片头（标题/操作）+ 筛选栏 + 列表 + 分页。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { pageExams } from '@/api/exam'
import type { ExamInfo } from '@/api/exam'
import { listTerms } from '@/api/term'
import type { Term } from '@/api/term'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import FilterBar from '@/components/FilterBar.vue'
import type { FilterField } from '@/components/FilterBar.vue'
import PageState from '@/components/PageState.vue'
import { formatDate } from '@/utils/format'

const pageSize = 10
const page = ref(1)

/** 筛选条件：考试名称 + 学期 */
const query = reactive<Record<string, string | number | undefined>>({
  name: '',
  termId: undefined as number | undefined,
})

/** 学期列表（用于筛选下拉与 termId → 学期名称映射） */
const terms = ref<Term[]>([])
const termMap = computed(() => {
  const map = new Map<number, string>()
  for (const term of terms.value) {
    if (term.id !== undefined) {
      map.set(term.id, term.name)
    }
  }
  return map
})

const filterFields = computed<FilterField[]>(() => [
  { label: '考试名称', prop: 'name', type: 'input', placeholder: '请输入考试名称' },
  {
    label: '学期',
    prop: 'termId',
    type: 'select',
    options: terms.value
      .filter((term) => term.id !== undefined)
      .map((term) => ({ text: term.name, value: term.id as number })),
  },
])

const {
  data: pageData,
  loading,
  error,
  reload,
} = useAsyncData(
  () =>
    pageExams({
      page: page.value,
      pageSize,
      name: query.name ? String(query.name) : undefined,
      termId: (query.termId as number | undefined) ?? undefined,
    }),
  { list: [] as ExamInfo[], total: 0, page: 1, pageSize },
)

const exams = computed(() => pageData.value.list)
const total = computed(() => pageData.value.total)
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

/** 学期名称：优先用 termId 映射，后端若带 termName 则直接用 */
function termText(row: ExamInfo): string {
  const extended = row as ExamInfo & { termName?: string }
  if (extended.termName) {
    return extended.termName
  }
  return row.termId === undefined ? '—' : (termMap.value.get(row.termId) ?? `学期#${row.termId}`)
}

/** 起止时间：日期 + 起止时刻 */
function timeRangeText(row: ExamInfo): string {
  const date = formatDate(row.examDate)
  if (!row.startTime && !row.endTime) {
    return date
  }
  const start = String(row.startTime ?? '').slice(0, 5)
  const end = String(row.endTime ?? '').slice(0, 5)
  return `${date} ${start}${end ? `~${end}` : ''}`
}

function onFilterChange() {
  page.value = 1
  void reload()
}

/** 翻页：page 由本函数与筛选变更显式驱动，不用 watch，避免一次操作发两次请求 */
function goPage(target: number) {
  if (target < 1 || target > pageCount.value || target === page.value) {
    return
  }
  page.value = target
  void reload()
}

onMounted(async () => {
  try {
    terms.value = await listTerms()
  } catch {
    // 学期映射失败不影响考试列表
  }
  await reload()
})
</script>

<template>
  <div>
    <div class="st-card">
      <PageHeader title="考试信息" />
      <p class="st-muted exam__hint">共 {{ total }} 场考试</p>
    </div>

    <FilterBar
      v-model="query"
      :fields="filterFields"
      search-placeholder="请输入考试名称"
      @change="onFilterChange"
    />

    <PageState
      :loading="loading"
      :error="error"
      :empty="exams.length === 0"
      empty-text="暂无考试安排"
      @retry="reload"
    >
      <div v-for="row in exams" :key="row.id ?? row.name" class="st-card">
        <div class="st-row">
          <div class="exam__name">{{ row.name }}</div>
          <van-tag v-if="row.examType" type="primary" plain>{{ row.examType }}</van-tag>
        </div>
        <div class="exam__line">
          <span class="exam__label">学期</span>
          <span>{{ termText(row) }}</span>
        </div>
        <div class="exam__line">
          <span class="exam__label">时间</span>
          <span class="exam__time">{{ timeRangeText(row) }}</span>
        </div>
        <div v-if="row.courseName" class="exam__line">
          <span class="exam__label">课程</span>
          <span>{{ row.courseName }}</span>
        </div>
        <div v-if="row.status" class="exam__status">
          <van-tag :type="row.status === '已安排' ? 'success' : 'warning'" plain>
            {{ row.status }}
          </van-tag>
        </div>
      </div>

      <div v-if="pageCount > 1" class="st-pagination">
        <van-button size="small" plain :disabled="page <= 1" @click="goPage(page - 1)">
          上一页
        </van-button>
        <span class="exam__page-info">{{ page }} / {{ pageCount }}</span>
        <van-button size="small" plain :disabled="page >= pageCount" @click="goPage(page + 1)">
          下一页
        </van-button>
      </div>
    </PageState>
  </div>
</template>

<style scoped>
.exam__hint {
  margin: 0;
}

.exam__name {
  font-size: 15px;
  font-weight: 600;
}

.exam__line {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  font-size: 14px;
}

.exam__label {
  flex-shrink: 0;
  width: 36px;
  color: var(--st-text-light);
}

.exam__time {
  color: var(--st-primary);
}

.exam__status {
  margin-top: 10px;
}

.exam__page-info {
  padding: 0 12px;
  font-size: 13px;
  color: var(--st-text-light);
  line-height: 32px;
}
</style>
