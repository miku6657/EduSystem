<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  auditGraduation,
  buildGraduationAuditRows,
  getGraduateChecks,
  getGraduationStudents,
  type GraduationAuditRow,
} from '@/api/graduation'

type CheckStatus = '' | 'WAIT' | 'PASS' | 'FAIL'

const STATUS_OPTIONS: {
  label: string
  value: CheckStatus
}[] = [
  {
    label: '全部',
    value: '',
  },
  {
    label: '未审核',
    value: 'WAIT',
  },
  {
    label: '通过',
    value: 'PASS',
  },
  {
    label: '不通过',
    value: 'FAIL',
  },
]

const STATUS_META = {
  WAIT: {
    text: '未审核',
    type: 'warning' as const,
  },
  PASS: {
    text: '通过',
    type: 'success' as const,
  },
  FAIL: {
    text: '不通过',
    type: 'danger' as const,
  },
}

const query = reactive({
  keyword: '',
  checkStatus: '' as CheckStatus,
})

const loading = ref(false)

const rows = ref<GraduationAuditRow[]>([])

const auditingStudentId = ref('')

const filteredRows = computed(() => {
  if (!query.checkStatus) {
    return rows.value
  }

  return rows.value.filter(
    (row) =>
      row.checkStatus === query.checkStatus,
  )
})

const totalCount = computed(
  () => rows.value.length,
)

const waitCount = computed(
  () =>
    rows.value.filter(
      (row) => row.checkStatus === 'WAIT',
    ).length,
)

const passCount = computed(
  () =>
    rows.value.filter(
      (row) => row.checkStatus === 'PASS',
    ).length,
)

const failCount = computed(
  () =>
    rows.value.filter(
      (row) => row.checkStatus === 'FAIL',
    ).length,
)

async function loadData() {
  loading.value = true

  try {
    const [
      studentPage,
      checks,
    ] = await Promise.all([
      getGraduationStudents({
        pageNo: 1,
        pageSize: 1000,
        keyword:
          query.keyword.trim()
          || undefined,
      }),

      getGraduateChecks(),
    ])

    rows.value =
      buildGraduationAuditRows(
        studentPage.records,
        checks,
      )
  } catch {
    // 错误由 request.ts 统一提示
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  loadData()
}

function handleReset() {
  query.keyword = ''
  query.checkStatus = ''

  loadData()
}

function getStatusMeta(
  status: 'WAIT' | 'PASS' | 'FAIL',
) {
  return STATUS_META[status]
}

async function handleAudit(
  row: GraduationAuditRow,
) {
  try {
    await ElMessageBox.confirm(
      `确定审核 ${row.name}（${row.studentNo}）吗？系统将自动检查该学生全部已录入成绩，存在低于60分的成绩则不通过，否则通过。`,
      '毕业资格审核',
      {
        confirmButtonText: '开始审核',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  auditingStudentId.value =
    row.studentId

  try {
    await auditGraduation(
      row.studentId,
      'admin',
    )

    ElMessage.success(
      `${row.name} 的毕业资格审核完成`,
    )

    await loadData()
  } catch {
    // 错误由 request.ts 统一提示
  } finally {
    auditingStudentId.value = ''
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="graduation-page">
    <el-card
      shadow="never"
      class="rule-card"
    >
      <el-alert
        title="毕业资格审核规则"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          系统自动查询学生全部已录入成绩。
          只要存在一门成绩低于 60 分，
          毕业审核即为“不通过”；
          所有已录入成绩均不低于 60 分，
          则审核“通过”。
        </template>
      </el-alert>
    </el-card>

    <el-row
      :gutter="14"
      class="stat-row"
    >
      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-value">
            {{ totalCount }}
          </div>

          <div class="stat-label">
            学生总数
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-value warning">
            {{ waitCount }}
          </div>

          <div class="stat-label">
            未审核
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-value success">
            {{ passCount }}
          </div>

          <div class="stat-label">
            审核通过
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card
          shadow="never"
          class="stat-card"
        >
          <div class="stat-value danger">
            {{ failCount }}
          </div>

          <div class="stat-label">
            审核不通过
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card
      shadow="never"
      class="filter-card"
    >
      <el-form
        inline
        class="filter-form"
        @submit.prevent="handleQuery"
      >
        <el-form-item label="学生">
          <el-input
            v-model="query.keyword"
            placeholder="姓名或学号"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="审核状态">
          <el-select
            v-model="query.checkStatus"
            style="width: 140px"
          >
            <el-option
              v-for="option in STATUS_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :icon="Search"
            @click="handleQuery"
          >
            查询
          </el-button>

          <el-button
            :icon="RefreshLeft"
            @click="handleReset"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <span class="table-title">
            毕业资格审核
          </span>

          <span class="table-count">
            当前显示
            {{ filteredRows.length }}
            条
          </span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="filteredRows"
        row-key="studentId"
        stripe
        style="width: 100%"
      >
        <el-table-column
          type="index"
          label="#"
          width="60"
          align="center"
        />

        <el-table-column
          prop="studentNo"
          label="学号"
          width="160"
        />

        <el-table-column
          prop="name"
          label="姓名"
          width="140"
        />

        <el-table-column
          label="审核结果"
          width="130"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="
                getStatusMeta(
                  row.checkStatus,
                ).type
              "
              effect="dark"
            >
              {{
                getStatusMeta(
                  row.checkStatus,
                ).text
              }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="remark"
          label="审核说明"
          min-width="320"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span
              v-if="row.remark"
              :class="{
                'remark-pass':
                  row.checkStatus
                  === 'PASS',
                'remark-fail':
                  row.checkStatus
                  === 'FAIL',
              }"
            >
              {{ row.remark }}
            </span>

            <span
              v-else
              class="empty-text"
            >
              尚未执行毕业审核
            </span>
          </template>
        </el-table-column>

        <el-table-column
          prop="checker"
          label="审核人"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            {{
              row.checker
              || '-'
            }}
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="140"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :loading="
                auditingStudentId
                === row.studentId
              "
              @click="handleAudit(row)"
            >
              {{
                row.checkStatus === 'WAIT'
                  ? '执行审核'
                  : '重新审核'
              }}
            </el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty
            description="暂无学生数据"
            :image-size="90"
          />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.graduation-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.rule-card :deep(.el-card__body) {
  padding: 14px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-color-primary);
}

.stat-value.warning {
  color: var(--el-color-warning);
}

.stat-value.success {
  color: var(--el-color-success);
}

.stat-value.danger {
  color: var(--el-color-danger);
}

.stat-label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.filter-form {
  margin-bottom: -18px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-title {
  font-weight: 600;
}

.table-count {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.remark-pass {
  color: var(--el-color-success);
}

.remark-fail {
  color: var(--el-color-danger);
}

.empty-text {
  color: var(--el-text-color-placeholder);
}
</style>