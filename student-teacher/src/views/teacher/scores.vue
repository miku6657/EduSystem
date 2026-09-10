<script setup lang="ts">
/**
 * 教师 · 成绩录入
 * 考试列表：GET  /api/exam/page
 * 成绩分页：GET  /api/score/page-by-exam?pageNo&pageSize&examId
 * 统计：    GET  /api/score/stat/{examId}
 * 保存：    POST /api/score/save/{examId}（只提交被修改过的行）
 */
import { computed, onMounted, ref, watch } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { pageExams } from '@/api/exam'
import type { ExamInfo } from '@/api/exam'
import { getScoreStat, pageScoresByExam, saveScores } from '@/api/score'
import type { ExamScore, ExamScoreStat } from '@/api/score'
import type { PageResult } from '@/types/api'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import { SCORE_STATUS_OPTIONS } from '@/constants/dict'

const userStore = useUserStore()
/** 教师工号；0 = 业务身份未解析成功（成绩接口按考试维度，允许确认后继续录入） */
const teacherId = computed(() => userStore.businessId)
const skipProfileCheck = ref(false)

const pageSize = 10
const page = ref(1)
const examId = ref(0)
const showExamPicker = ref(false)
const saving = ref(false)

/** 可编辑的成绩行：分数用字符串保存，便于输入框双向绑定 */
interface ScoreRow {
  studentId: number
  name: string
  score: string
  status: string
  originScore: string
  originStatus: string
}

const rows = ref<ScoreRow[]>([])

const { data: exams, error: examError, reload: reloadExams } = useAsyncData<ExamInfo[]>(
  async () => (await pageExams({ page: 1, pageSize: 50 })).list,
  [],
)
const { data: stat, reload: reloadStat } = useAsyncData<ExamScoreStat>(
  () => (examId.value ? getScoreStat(examId.value) : Promise.resolve({})),
  {},
)
const { data: pageData, loading, error, reload } = useAsyncData<PageResult<ExamScore>>(
  () =>
    examId.value
      ? pageScoresByExam({ page: page.value, pageSize, examId: examId.value })
      : Promise.resolve({ list: [], total: 0, page: 1, pageSize }),
  { list: [], total: 0, page: 1, pageSize },
)

/** 分页结果 → 可编辑行 + 原始快照（用于"只提交修改过的行"） */
watch(pageData, (value) => {
  rows.value = value.list.map((item) => {
    const score = item.score === null || item.score === undefined ? '' : String(item.score)
    const status = item.status || 'NORMAL'
    return {
      studentId: item.studentId,
      name: item.studentName || item.studentNo || `学生#${item.studentId}`,
      score,
      status,
      originScore: score,
      originStatus: status,
    }
  })
})

const examText = computed(() => {
  const exam = exams.value.find((item) => item.id === examId.value)
  return exam ? `${exam.name}${exam.examDate ? `（${exam.examDate}）` : ''}` : ''
})
const examColumns = computed(() =>
  exams.value.map((item) => ({
    text: `${item.name}${item.examDate ? ` ${item.examDate}` : ''}`,
    value: item.id,
  })),
)
/** 统计卡：应考 / 实考 / 缺考 / 及格 / 不及格 */
const statItems = computed(() => {
  const value = stat.value
  return [
    { label: '应考', value: Number(value.total ?? 0) },
    { label: '实考', value: Number(value.actual ?? 0) },
    { label: '缺考', value: Number(value.absent ?? 0) },
    { label: '及格', value: Number(value.passed ?? 0) },
    { label: '不及格', value: Number(value.failed ?? 0) },
  ]
})
const total = computed(() => pageData.value.total)
const pageCount = computed(
  () => Math.max(1, Math.ceil(total.value / (pageData.value.pageSize || pageSize))),
)
/** 已修改（未保存）的行数 */
const dirtyCount = computed(
  () => rows.value.filter((row) => row.score !== row.originScore || row.status !== row.originStatus).length,
)

function openExamPicker() {
  if (!exams.value.length) {
    showToast('暂无可录入的考试')
    return
  }
  showExamPicker.value = true
}

function onExamConfirm(payload: { selectedOptions?: Array<{ value?: string | number } | undefined> }) {
  const id = Number(payload.selectedOptions?.[0]?.value ?? 0)
  showExamPicker.value = false
  if (!id || id === examId.value) {
    return
  }
  examId.value = id
  page.value = 1
  reload()
  reloadStat()
}

/** 缺考：清空并禁用分数输入 */
function onStatusChange(row: ScoreRow) {
  if (row.status === 'ABSENT') {
    row.score = ''
  }
}

/** 分数校验：允许留空；否则必须是 0~100 的整数或一位小数 */
function invalidScore(raw: string): boolean {
  const value = raw.trim()
  if (!value) {
    return false
  }
  if (!/^\d{1,3}(\.\d)?$/.test(value)) {
    return true
  }
  const score = Number(value)
  return score < 0 || score > 100
}

async function goPage(next: number) {
  if (next < 1 || next > pageCount.value || next === page.value) {
    return
  }
  if (dirtyCount.value > 0) {
    try {
      await showConfirmDialog({
        title: '未保存的修改',
        message: `有 ${dirtyCount.value} 条成绩尚未保存，翻页将丢失这些修改，是否继续？`,
      })
    } catch {
      return
    }
  }
  page.value = next
  await reload()
}

async function onSave() {
  if (!examId.value) {
    showToast('请先选择考试')
    return
  }
  const changed = rows.value.filter(
    (row) => row.score !== row.originScore || row.status !== row.originStatus,
  )
  if (changed.length === 0) {
    showToast('没有需要保存的修改')
    return
  }
  for (const row of changed) {
    if (invalidScore(row.score)) {
      showToast(`${row.name} 的分数需为 0~100 的整数或一位小数`)
      return
    }
  }
  const payload: ExamScore[] = changed.map((row) => ({
    examId: examId.value,
    studentId: row.studentId,
    status: row.status,
    score: row.score.trim() === '' ? null : Number(row.score),
  }))
  saving.value = true
  try {
    await saveScores(examId.value, payload)
    showToast(`已保存 ${payload.length} 条成绩`)
    await reload()
    await reloadStat()
  } catch {
    // 失败原因（如分数越界）由请求层统一 toast
  } finally {
    saving.value = false
  }
}

/** 工号未解析时重试解析业务身份 */
async function retryProfile() {
  await userStore.resolveProfile(true)
  reloadExams()
}

onMounted(reloadExams)
</script>

<template>
  <div>
    <!-- 工号未解析：给出提示，并允许确认后继续（成绩接口不依赖工号） -->
    <van-empty v-if="!teacherId && !skipProfileCheck" description="未解析到教师工号">
      <van-button round type="primary" size="small" @click="retryProfile">重新解析身份</van-button>
      <div class="score__skip">
        <van-button round size="small" plain @click="skipProfileCheck = true">继续录入成绩</van-button>
      </div>
    </van-empty>

    <template v-else>
      <div class="st-card">
        <van-field readonly is-link label="考试" placeholder="请选择考试" :model-value="examText"
          @click="openExamPicker" />
      </div>

      <van-empty v-if="examError" image="error" :description="examError">
        <van-button round type="primary" size="small" @click="reloadExams">重新加载考试</van-button>
      </van-empty>

      <van-empty v-else-if="!examId" description="请先选择考试" />

      <template v-else>
        <!-- 成绩统计 -->
        <div class="st-card score__stat">
          <div v-for="item in statItems" :key="item.label" class="score__stat-item">
            <div class="score__stat-value">{{ item.value }}</div>
            <div class="st-muted">{{ item.label }}</div>
          </div>
        </div>

        <div v-if="loading" class="st-empty">
          <van-loading vertical>加载中…</van-loading>
        </div>

        <van-empty v-else-if="error" image="error" :description="error">
          <van-button round type="primary" size="small" @click="reload">重新加载</van-button>
        </van-empty>

        <van-empty v-else-if="rows.length === 0" description="该考试暂无成绩记录" />

        <template v-else>
          <div v-for="row in rows" :key="row.studentId" class="st-card">
            <div class="st-row">
              <span class="score__name">{{ row.name }}</span>
              <van-field v-model="row.score" type="number" placeholder="分数" input-align="right"
                class="score__input" :disabled="row.status === 'ABSENT'" />
            </div>
            <van-radio-group v-model="row.status" direction="horizontal" class="score__status"
              @change="() => onStatusChange(row)">
              <van-radio v-for="option in SCORE_STATUS_OPTIONS" :key="option.value" :name="option.value"
                shape="dot">
                {{ option.text }}
              </van-radio>
            </van-radio-group>
          </div>

          <!-- 分页 + 未保存提示 -->
          <div class="st-card">
            <div class="st-row">
              <van-button size="small" plain :disabled="page <= 1" @click="goPage(page - 1)">上一页</van-button>
              <span class="st-muted">第 {{ page }} / {{ pageCount }} 页 · 共 {{ total }} 条</span>
              <van-button size="small" plain :disabled="page >= pageCount" @click="goPage(page + 1)">
                下一页
              </van-button>
            </div>
            <div v-if="dirtyCount > 0" class="st-muted score__dirty">已修改 {{ dirtyCount }} 条，尚未保存</div>
          </div>

          <van-button round block type="primary" :loading="saving" @click="onSave">保存修改</van-button>
        </template>
      </template>
    </template>

    <van-popup v-model:show="showExamPicker" position="bottom" round>
      <van-picker title="选择考试" :columns="examColumns" :model-value="[examId]"
        @confirm="onExamConfirm" @cancel="showExamPicker = false" />
    </van-popup>
  </div>
</template>

<style scoped>
.score__skip { margin-top: 10px; }
.score__stat { display: flex; justify-content: space-around; text-align: center; }
.score__stat-value { font-size: 18px; font-weight: 600; }
.score__name { flex: none; font-size: 15px; font-weight: 600; }
.score__input { flex: 1; padding: 4px 0; }
.score__status { margin-top: 10px; }
.score__dirty { margin-top: 8px; text-align: center; }
</style>
