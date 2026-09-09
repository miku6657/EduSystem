<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  approveGraduation,
  getGraduationList,
  rejectGraduation,
  type GraduationAuditStatus,
  type GraduationStudent,
} from '@/api/graduation'

const STATUS_OPTIONS: { label: string; value: '' | GraduationAuditStatus }[] = [
  { label: '全部', value: '' },
  { label: '待审核', value: '待审核' },
  { label: '通过', value: '通过' },
  { label: '未通过', value: '未通过' },
]

const STATUS_META: Record<GraduationAuditStatus, { text: string; type: 'warning' | 'success' | 'danger' }> = {
  待审核: { text: '待审核', type: 'warning' },
  通过: { text: '通过', type: 'success' },
  未通过: { text: '未通过', type: 'danger' },
}

/** 查询条件 */
const query = reactive({ name: '', studentNo: '', status: '' as '' | GraduationAuditStatus })

/** 列表状态 */
const loading = ref(false)
const rows = ref<GraduationStudent[]>([])
const selectedRows = ref<GraduationStudent[]>([])
const auditingId = ref(0)
const batchAuditing = ref(false)

const pendingSelected = computed(() => selectedRows.value.filter((row) => row.status === '待审核'))

/** 加载列表（查询/重置共用） */
async function loadData() {
  loading.value = true
  try {
    rows.value = await getGraduationList({
      name: query.name.trim() || undefined,
      studentNo: query.studentNo.trim() || undefined,
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
  query.name = ''
  query.studentNo = ''
  query.status = ''
  loadData()
}

function onSelectionChange(selection: GraduationStudent[]) {
  selectedRows.value = selection
}

function statusMeta(status: GraduationAuditStatus) {
  return STATUS_META[status]
}

/** 单条通过 / 驳回（el-table 插槽 row 为泛型，参数做 any 收窄） */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function auditRow(row: any, action: 'approve' | 'reject') {
  const actionText = action === 'approve' ? '通过' : '驳回'
  const title = `${actionText}毕业审核`
  try {
    await ElMessageBox.confirm(
      `确定${actionText} ${row.name}（学号 ${row.studentNo}）的毕业审核吗？${action === 'reject' ? '驳回后学生需补充材料后重新提交。' : ''}`,
      title,
      { confirmButtonText: `确认${actionText}`, cancelButtonText: '取消', type: action === 'approve' ? 'warning' : 'error' },
    )
  } catch {
    return
  }
  auditingId.value = row.id
  try {
    if (action === 'approve') {
      await approveGraduation(row.id)
      ElMessage.success(`已通过 ${row.name} 的毕业审核`)
    } else {
      await rejectGraduation(row.id)
      ElMessage.warning(`已驳回 ${row.name} 的毕业审核`)
    }
    await loadData()
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    auditingId.value = 0
  }
}

/** 批量通过（仅对待审核行生效，跳过非待审核行） */
async function batchApprove() {
  const targets = pendingSelected.value
  if (targets.length === 0) {
    ElMessage.warning('请先勾选处于“待审核”状态的记录')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定批量通过选中的 ${targets.length} 条毕业审核记录吗？`,
      '批量通过',
      { confirmButtonText: '确认批量通过', cancelButtonText: '取消', type: 'warning' },
    )
  } catch {
    return
  }
  batchAuditing.value = true
  try {
    await Promise.all(targets.map((row) => approveGraduation(row.id)))
    ElMessage.success(`已批量通过 ${targets.length} 条记录`)
    selectedRows.value = []
    await loadData()
  } catch {
    // 单条失败时由请求层提示，列表保持原状供重试
  } finally {
    batchAuditing.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="graduation-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form inline class="filter-form" @submit.prevent="onQuery">
        <el-form-item label="姓名">
          <el-input
            v-model="query.name"
            placeholder="请输入学生姓名"
            clearable
            style="width: 170px"
            @keyup.enter="onQuery"
          />
        </el-form-item>
        <el-form-item label="学号">
          <el-input
            v-model="query.studentNo"
            placeholder="请输入学号"
            clearable
            style="width: 170px"
            @keyup.enter="onQuery"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.status" placeholder="全部" style="width: 130px">
            <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onQuery">查询</el-button>
          <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="table-head">
          <span class="table-head__title">毕业生列表（共 {{ rows.length }} 条）</span>
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

      <el-table
        v-loading="loading"
        :data="rows"
        stripe
        row-key="id"
        style="width: 100%"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="48" :reserve-selection="false" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="major" label="专业" min-width="200" show-overflow-tooltip />
        <el-table-column prop="graduateYear" label="毕业年份" width="110" align="center" />
        <el-table-column label="审核状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" effect="dark" disable-transitions>
              {{ statusMeta(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '待审核'">
              <el-button
                type="success"
                size="small"
                :loading="auditingId === row.id"
                @click="auditRow(row, 'approve')"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                :loading="auditingId === row.id"
                @click="auditRow(row, 'reject')"
              >
                驳回
              </el-button>
            </template>
            <span v-else class="audited-text">已审核</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有符合条件的毕业生记录" :image-size="80" />
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
