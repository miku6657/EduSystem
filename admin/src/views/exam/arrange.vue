<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, MagicStick, RefreshLeft } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { autoArrangeExam, type AutoArrangeResult } from '@/api/exam'

/* ==================== 筛选条件 ==================== */

const TERM_OPTIONS = [
  '2025-2026学年第一学期',
  '2025-2026学年第二学期',
  '2026-2027学年第一学期',
  '2026-2027学年第二学期',
  '2027-2028学年第一学期',
]

const GRADE_OPTIONS = ['2023级', '2024级', '2025级', '2026级']

const term = ref('2026-2027学年第二学期')
const grade = ref('2024级')

/* ==================== 排考状态 ==================== */

const arranging = ref(false)
const result = ref<AutoArrangeResult | null>(null)
const exporting = ref(false)

const rows = computed(() => result.value?.items ?? [])
const summary = computed(() => result.value?.summary)

/** 统计卡片数据 */
const statCards = computed(() => [
  { label: '排考场次', value: summary.value?.examCount ?? 0, unit: '场' },
  { label: '使用教室', value: summary.value?.roomCount ?? 0, unit: '间' },
  { label: '正常安排', value: summary.value?.normalCount ?? 0, unit: '条' },
  { label: '冲突条目', value: summary.value?.conflictCount ?? 0, unit: '条', danger: true },
])

const hasConflict = computed(() => Boolean(summary.value && summary.value.conflictCount > 0))

/* ==================== 动作 ==================== */

async function handleArrange() {
  if (!term.value || !grade.value) {
    ElMessage.warning('请先选择学期与年级')
    return
  }
  arranging.value = true
  result.value = null
  try {
    result.value = await autoArrangeExam({ term: term.value, grade: grade.value })
    if (result.value.summary.conflictCount > 0) {
      ElMessage.warning(`自动排考完成，发现 ${result.value.summary.conflictCount} 条冲突，请人工处理后再发布`)
    } else {
      ElMessage.success('自动排考完成，未发现冲突')
    }
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    arranging.value = false
  }
}

function resetArrange() {
  result.value = null
}

/* ==================== Excel 导出 ==================== */

function exportExcel() {
  const data = result.value
  if (!data || data.items.length === 0) {
    ElMessage.info('暂无排考结果可导出，请先执行自动排考')
    return
  }
  exporting.value = true
  try {
    const sheetRows = data.items.map((row, index) => ({
      '序号': index + 1,
      '课程名称': row.courseName,
      '班级': row.className,
      '考生人数': row.studentCount,
      '考试日期': row.examDate,
      '场次': row.session,
      '考场': row.examRoom,
      '监考教师': row.invigilators.join('、'),
      '排考状态': row.conflict ? '冲突' : '正常',
      '冲突说明': row.conflictReason || '',
    }))
    const worksheet = XLSX.utils.json_to_sheet(sheetRows)
    // 设置列宽，保证导出后中文可读
    worksheet['!cols'] = [
      { wch: 6 },
      { wch: 20 },
      { wch: 16 },
      { wch: 10 },
      { wch: 12 },
      { wch: 18 },
      { wch: 18 },
      { wch: 20 },
      { wch: 10 },
      { wch: 60 },
    ]
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '排考结果')
    const safeName = data.term.replace(/\s+/g, '')
    XLSX.writeFile(workbook, `自动排考结果_${safeName}_${data.grade}.xlsx`)
    ElMessage.success('排考结果已导出为 Excel')
  } catch (error) {
    ElMessage.error('导出失败：' + (error instanceof Error ? error.message : String(error)))
  } finally {
    exporting.value = false
  }
}

/* ==================== 表格样式 ==================== */

function tableRowClass({ row }: { row: AutoArrangeResult['items'][number] }) {
  return row.conflict ? 'arrange-conflict-row' : ''
}
</script>

<template>
  <div class="exam-arrange-page">
    <!-- 筛选 -->
    <el-card shadow="never">
      <el-form inline class="filter-form">
        <el-form-item label="学期">
          <el-select v-model="term" style="width: 240px">
            <el-option v-for="t in TERM_OPTIONS" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="grade" style="width: 160px">
            <el-option v-for="g in GRADE_OPTIONS" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 一键排考入口 -->
    <el-card shadow="never" class="arrange-action-card">
      <div class="arrange-action">
        <div class="arrange-action__title">
          为 <b>{{ term }}</b> · <b>{{ grade }}</b> 自动编排期末考试考场与监考教师
        </div>
        <el-button
          type="primary"
          size="large"
          class="arrange-action__btn"
          :loading="arranging"
          :icon="MagicStick"
          @click="handleArrange"
        >
          {{ arranging ? '排考中，正在检查冲突…' : '一键自动排考' }}
        </el-button>
        <div class="arrange-action__tip">
          系统将综合 教室容量、考试时间、监考教师当日安排 自动生成排考方案，并自动识别<span class="highlight">监考员重复</span>、<span class="highlight">教室重复占用</span>等冲突。
        </div>
      </div>
    </el-card>

    <!-- 排考结果 -->
    <template v-if="result">
      <!-- 统计 -->
      <el-row :gutter="14" class="stat-row">
        <el-col v-for="card in statCards" :key="card.label" :span="6">
          <el-card shadow="never" class="stat-card" :class="{ 'stat-card--danger': card.danger }">
            <div class="stat-card__value" :class="{ 'stat-card__value--danger': card.danger }">
              {{ card.value }}<span class="stat-card__unit">{{ card.unit }}</span>
            </div>
            <div class="stat-card__label">{{ card.label }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 冲突警告 / 结果操作 -->
      <el-alert
        v-if="hasConflict"
        type="error"
        :closable="false"
        show-icon
        class="result-alert"
      >
        <template #title>
          排考结果存在 {{ summary?.conflictCount }} 条冲突（红色行），请先处理冲突后再对外发布考试安排。
        </template>
      </el-alert>
      <el-alert v-else type="success" :closable="false" show-icon class="result-alert">
        <template #title>排考结果无冲突，可直接发布。</template>
      </el-alert>

      <el-card shadow="never">
        <template #header>
          <div class="result-head">
            <span class="result-head__title">
              排考明细（{{ rows.length }} 条 · 生成于 {{ result.generatedAt }}）
            </span>
            <div class="result-head__actions">
              <el-button :icon="RefreshLeft" @click="resetArrange">重新排考</el-button>
              <el-button type="success" :icon="Download" :loading="exporting" @click="exportExcel">
                导出 Excel
              </el-button>
            </div>
          </div>
        </template>

        <el-table :data="rows" stripe :row-class-name="tableRowClass" style="width: 100%">
          <el-table-column type="index" label="#" width="52" />
          <el-table-column prop="courseName" label="课程名称" min-width="170" show-overflow-tooltip />
          <el-table-column prop="className" label="班级" width="150" show-overflow-tooltip />
          <el-table-column prop="studentCount" label="人数" width="80" align="center" />
          <el-table-column label="考试时间" min-width="200">
            <template #default="{ row }">
              {{ row.examDate }}<br /><span class="cell-session">{{ row.session }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="examRoom" label="考场" width="150" show-overflow-tooltip />
          <el-table-column label="监考教师" min-width="150">
            <template #default="{ row }">
              <el-tag
                v-for="teacher in row.invigilators"
                :key="teacher"
                size="small"
                :type="row.conflict ? 'danger' : 'info'"
                effect="plain"
                class="invigilator-tag"
              >
                {{ teacher }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center" fixed="right">
            <template #default="{ row }">
              <el-tag :type="row.conflict ? 'danger' : 'success'" effect="dark" disable-transitions>
                {{ row.conflict ? '冲突' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="冲突说明" min-width="280">
            <template #default="{ row }">
              <span v-if="row.conflict" class="conflict-reason">{{ row.conflictReason }}</span>
              <span v-else class="cell-normal">-</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- 未排考时的占位引导 -->
    <el-card v-else shadow="never" class="arrange-empty-card">
      <el-empty description="尚未执行排考。请选择学期与年级后，点击上方按钮一键自动排考">
        <template #image>
          <div class="arrange-empty-icon">📋</div>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<style scoped>
.exam-arrange-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.filter-form {
  margin-bottom: -18px;
}

/* 一键排考 */
.arrange-action-card :deep(.el-card__body) {
  padding: 0;
}

.arrange-action {
  padding: 30px 24px;
  text-align: center;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
  border-radius: 4px;
}

.arrange-action__title {
  font-size: 15px;
  color: var(--el-text-color-regular);
  margin-bottom: 18px;
}

.arrange-action__btn {
  min-width: 260px;
  font-size: 16px;
  height: 46px;
}

.arrange-action__tip {
  margin-top: 14px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.highlight {
  color: var(--el-color-danger);
}

/* 统计卡片 */
.stat-card {
  text-align: center;
}

.stat-card--danger {
  border-color: var(--el-color-danger);
}

.stat-card__value {
  font-size: 30px;
  font-weight: 700;
  color: var(--el-color-primary);
  line-height: 1.2;
}

.stat-card__value--danger {
  color: var(--el-color-danger);
}

.stat-card__unit {
  font-size: 13px;
  font-weight: 400;
  margin-left: 4px;
  color: var(--el-text-color-secondary);
}

.stat-card__label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.result-alert {
  margin-top: 2px;
}

/* 结果表头 */
.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.result-head__title {
  font-weight: 600;
}

.invigilator-tag {
  margin-right: 6px;
}

.cell-session {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.conflict-reason {
  color: var(--el-color-danger);
  font-size: 13px;
}

.cell-normal {
  color: var(--el-text-color-placeholder);
}

/* 冲突行整体浅红底色 */
:deep(.arrange-conflict-row > td.el-table__cell) {
  background: #fef0f0 !important;
}

.arrange-empty-icon {
  font-size: 64px;
  line-height: 1;
}
</style>