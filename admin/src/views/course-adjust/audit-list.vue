<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  approveCourseAdjust,
  getCourseAdjustAuditList,
  rejectCourseAdjust,
  type CourseAdjustRequest,
  type CourseAdjustStatus,
} from '@/api/course-adjust'

const STATUS_OPTIONS: { label: string; value: '' | CourseAdjustStatus }[] = [
  { label: '全部', value: '' },
  { label: '待审核', value: '待审核' },
  { label: '已通过', value: '已通过' },
  { label: '已驳回', value: '已驳回' },
]

const STATUS_META: Record<CourseAdjustStatus, { type: 'warning' | 'success' | 'danger' }> = {
  待审核: { type: 'warning' },
  已通过: { type: 'success' },
  已驳回: { type: 'danger' },
}

const query = reactive({ courseName: '', teacher: '', status: '' as '' | CourseAdjustStatus })

const loading = ref(false)
const rows = ref<CourseAdjustRequest[]>([])
const selectedRows = ref<CourseAdjustRequest[]>([])
const auditingId = ref(0)
const batchAuditing = ref(false)

const pendingSelected = computed(() => selectedRows.value.filter((row) => row.status === '待审核'))

async function loadData() {
  loading.value = true
  try {
    rows.value = await getCourseAdjustAuditList({
      courseName: query.courseName.trim() || undefined,
      teacher: query.teacher.trim() || undefined,
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
  query.courseName = ''
  query.teacher = ''
  query.status = ''
  loadData()
}

function onSelectionChange(selection: CourseAdjustRequest[]) {
  selectedRows.value = selection
}

function statusMeta(status: CourseAdjustStatus) {
  return STATUS_META[status]
}

// el-table 插槽 row 为泛型，使用 any 做最小收窄
// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function auditRow(row: any, action: 'approve' | 'reject') {
  const actionText = action === 'approve' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(
      `确定${actionText}「${row.courseName}」的调课申请（${row.teacher} ${row.originalTime} → ${row.newTime}）吗？`,
      `${actionText}调课申请`,
      { confirmButtonText: `确认${actionText}`, cancelButtonText: '取消', type: action === 'approve' ? 'warning' : 'error' },
    )
  } catch {
    return
  }
  auditingId.value = row.id
  try {
    if (action === 'approve') {
      await approveCourseAdjust(row.id)
      ElMessage.success(`已通过「${row.courseName}」的调课申请`)
    } else {
      await rejectCourseAdjust(row.id)
      ElMessage.warning(`已驳回「${row.courseName}」的调课申请`)
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
    await ElMessageBox.confirm(`确定批量通过选中的 ${targets.length} 条调课申请吗？`, '批量通过', {
      confirmButtonText: '确认批量通过',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  batchAuditing.value = true
  try {
    await Promise.all(targets.map((row) => approveCourseAdjust(row.id)))
    ElMessage.success(`已批量通过 ${targets.length} 条调课申请`)
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
        <el-form-item label="课程名">
          <el-input v-model="query.courseName" placeholder="请输入课程名" clearable style="width: 180px" @keyup.enter="onQuery" />
        </el-form-item>
        <el-form-item label="申请教师">
          <el-input v-model="query.teacher" placeholder="请输入教师姓名" clearable style="width: 150px" @keyup.enter="onQuery" />
        </el-form-item>
        <el-form-item label="审批状态">
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
          <span class="table-head__title">调课审批列表（共 {{ rows.length }} 条）</span>
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
        <el-table-column prop="courseName" label="课程名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="teacher" label="申请教师" width="110" />
        <el-table-column label="原时间" width="170">
          <template #default="{ row }">{{ row.originalTime }}</template>
        </el-table-column>
        <el-table-column label="新时间" width="170">
          <template #default="{ row }">{{ row.newTime }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="调课原因" min-width="240" show-overflow-tooltip />
        <el-table-column label="审批状态" width="100" align="center">
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
          <el-empty description="没有符合条件的调课申请" :image-size="80" />
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
