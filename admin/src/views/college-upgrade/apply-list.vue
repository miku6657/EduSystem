<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  approveUpgradeApply,
  getUpgradeAuditList,
  rejectUpgradeApply,
  type UpgradeApply,
  type UpgradeApplyStatus,
} from '@/api/college-upgrade'

const STATUS_OPTIONS: { label: string; value: '' | UpgradeApplyStatus }[] = [
  { label: '全部', value: '' },
  { label: '待审核', value: '待审核' },
  { label: '已通过', value: '已通过' },
  { label: '已驳回', value: '已驳回' },
]

const STATUS_META: Record<UpgradeApplyStatus, { type: 'warning' | 'success' | 'danger' }> = {
  待审核: { type: 'warning' },
  已通过: { type: 'success' },
  已驳回: { type: 'danger' },
}

const query = reactive({ studentName: '', status: '' as '' | UpgradeApplyStatus })

const loading = ref(false)
const rows = ref<UpgradeApply[]>([])
const selectedRows = ref<UpgradeApply[]>([])
const auditingId = ref(0)
const batchAuditing = ref(false)

const pendingSelected = computed(() => selectedRows.value.filter((row) => row.status === '待审核'))

async function loadData() {
  loading.value = true
  try {
    rows.value = await getUpgradeAuditList({
      studentName: query.studentName.trim() || undefined,
      status: query.status || undefined,
    })
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    loading.value = false
  }
}

function onQuery() {
  loadData()
}

function onReset() {
  query.studentName = ''
  query.status = ''
  loadData()
}

function onSelectionChange(selection: UpgradeApply[]) {
  selectedRows.value = selection
}

function statusMeta(status: UpgradeApplyStatus) {
  return STATUS_META[status]
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function auditRow(row: any, action: 'approve' | 'reject') {
  const actionText = action === 'approve' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(
      `确定${actionText} ${row.studentName}（${row.majorName} → ${row.targetSchool}）的专升本报名吗？`,
      `${actionText}报名审核`,
      { confirmButtonText: `确认${actionText}`, cancelButtonText: '取消', type: action === 'approve' ? 'warning' : 'error' },
    )
  } catch {
    return
  }
  auditingId.value = row.id
  try {
    if (action === 'approve') {
      await approveUpgradeApply(row.id)
      ElMessage.success(`已通过 ${row.studentName} 的报名审核`)
    } else {
      await rejectUpgradeApply(row.id)
      ElMessage.warning(`已驳回 ${row.studentName} 的报名审核`)
    }
    await loadData()
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    auditingId.value = 0
  }
}

async function batchApprove() {
  const targets = pendingSelected.value
  if (targets.length === 0) {
    ElMessage.warning('请先勾选处于“待审核”状态的记录')
    return
  }
  try {
    await ElMessageBox.confirm(`确定批量通过选中的 ${targets.length} 条报名记录吗？`, '批量通过', {
      confirmButtonText: '确认批量通过',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  batchAuditing.value = true
  try {
    await Promise.all(targets.map((row) => approveUpgradeApply(row.id)))
    ElMessage.success(`已批量通过 ${targets.length} 条报名记录`)
    selectedRows.value = []
    await loadData()
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    batchAuditing.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="audit-page">
    <el-card shadow="never">
      <el-form inline class="filter-form" @submit.prevent="onQuery">
        <el-form-item label="报名学生">
          <el-input v-model="query.studentName" placeholder="请输入学生姓名" clearable style="width: 180px" @keyup.enter="onQuery" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.status" style="width: 130px">
            <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onQuery">查询</el-button>
          <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-head">
          <span class="table-head__title">专升本报名审核（共 {{ rows.length }} 条）</span>
          <el-button
            type="success"
            plain
            :disabled="pendingSelected.length === 0"
            :loading="batchAuditing"
            @click="batchApprove"
          >
            批量通过{{ pendingSelected.length ? `（${pendingSelected.length}）` : '' }}
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="rows" stripe style="width: 100%" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="studentName" label="报名学生" width="120" />
        <el-table-column prop="majorName" label="专科专业" min-width="170" show-overflow-tooltip />
        <el-table-column prop="targetSchool" label="报考院校" min-width="180" show-overflow-tooltip />
        <el-table-column prop="rank" label="成绩排名" width="100" align="center" />
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" effect="dark" disable-transitions>
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '待审核'">
              <el-button type="success" size="small" :loading="auditingId === row.id" @click="auditRow(row, 'approve')">通过</el-button>
              <el-button type="danger" size="small" :loading="auditingId === row.id" @click="auditRow(row, 'reject')">驳回</el-button>
            </template>
            <span v-else class="audited-text">已审核</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有符合条件的报名记录" :image-size="80" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.audit-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.filter-form {
  margin-bottom: -18px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-head__title {
  font-weight: 600;
}

.audited-text {
  color: var(--el-text-color-placeholder);
  font-size: 13px;
}
</style>
