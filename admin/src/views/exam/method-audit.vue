<script setup lang="ts">
import {
  computed,
  onMounted,
  reactive,
  ref,
} from 'vue'

import {
  ElMessage,
  ElMessageBox,
} from 'element-plus'

import {
  RefreshLeft,
  Search,
} from '@element-plus/icons-vue'

import {
  approveMethodAudit,
  getMethodAuditList,
  rejectMethodAudit,
} from '@/api/exam'

import type {
  ExamMethodApply,
  MethodAuditStatus,
} from '@/api/exam'

/**
 * 后端真实状态：
 *
 * WAIT / PASS / FAIL
 */
const STATUS_OPTIONS:
  Array<{
    label: string

    value:
      | ''
      | MethodAuditStatus
  }> = [
    {
      label: '全部',
      value: '',
    },

    {
      label: '待审核',
      value: 'WAIT',
    },

    {
      label: '已通过',
      value: 'PASS',
    },

    {
      label: '已驳回',
      value: 'FAIL',
    },
  ]

const STATUS_META:
  Record<
    MethodAuditStatus,
    {
      text: string

      type:
        | 'warning'
        | 'success'
        | 'danger'
    }
  > = {

    WAIT: {
      text: '待审核',
      type: 'warning',
    },

    PASS: {
      text: '已通过',
      type: 'success',
    },

    FAIL: {
      text: '已驳回',
      type: 'danger',
    },
  }

const query =
  reactive({
    courseName: '',

    teacher: '',

    status:
      '' as
        | ''
        | MethodAuditStatus,
  })

const loading =
  ref(false)

const rows =
  ref<
    ExamMethodApply[]
  >([])

const selectedRows =
  ref<
    ExamMethodApply[]
  >([])

/**
 * Snowflake ID使用string。
 */
const auditingId =
  ref('')

const batchAuditing =
  ref(false)

/**
 * 只有WAIT才能审核。
 */
const pendingSelected =
  computed(
    () =>
      selectedRows.value.filter(
        (row) =>
          row.status
          === 'WAIT',
      ),
  )

async function loadData() {

  loading.value =
    true

  try {

    rows.value =
      await getMethodAuditList({
        courseName:
          query.courseName
            .trim()
          || undefined,

        teacher:
          query.teacher
            .trim()
          || undefined,

        status:
          query.status
          || undefined,
      })

  } catch {
    // request.ts统一提示
  } finally {

    loading.value =
      false
  }
}

function onQuery() {
  loadData()
}

function onReset() {

  query.courseName =
    ''

  query.teacher =
    ''

  query.status =
    ''

  loadData()
}

function onSelectionChange(
  selection:
    ExamMethodApply[],
) {

  selectedRows.value =
    selection
}

function statusMeta(
  status:
    MethodAuditStatus,
) {

  return STATUS_META[
    status
  ]
}

/**
 * 单条审批。
 */
async function auditRow(
  row:
    ExamMethodApply,

  action:
    | 'approve'
    | 'reject',
) {

  const actionText =
    action === 'approve'
      ? '通过'
      : '驳回'

  try {

    await ElMessageBox.confirm(
      `确定${actionText}「${row.courseName}」采用「${row.applyType}」的考核方式申报吗？`,

      `${actionText}考核方式申报`,

      {
        confirmButtonText:
          `确认${actionText}`,

        cancelButtonText:
          '取消',

        type:
          action === 'approve'
            ? 'warning'
            : 'error',
      },
    )

  } catch {
    return
  }

  auditingId.value =
    row.id

  try {

    if (
      action === 'approve'
    ) {

      await approveMethodAudit(
        row.id,
      )

      ElMessage.success(
        `已通过「${row.courseName}」的考核方式申报`,
      )

    } else {

      await rejectMethodAudit(
        row.id,
      )

      ElMessage.warning(
        `已驳回「${row.courseName}」的考核方式申报`,
      )
    }

    await loadData()

  } catch {
    // request统一提示
  } finally {

    auditingId.value =
      ''
  }
}

/**
 * 批量通过。
 *
 * 后端没有批量接口，
 * 所以使用现有单条audit接口并发调用。
 */
async function batchApprove() {

  const targets =
    pendingSelected.value

  if (
    targets.length === 0
  ) {

    ElMessage.warning(
      '请先勾选处于“待审核”状态的记录',
    )

    return
  }

  try {

    await ElMessageBox.confirm(
      `确定批量通过选中的 ${targets.length} 条考核方式申报吗？`,

      '批量通过',

      {
        confirmButtonText:
          '确认批量通过',

        cancelButtonText:
          '取消',

        type:
          'warning',
      },
    )

  } catch {
    return
  }

  batchAuditing.value =
    true

  try {

    await Promise.all(
      targets.map(
        (row) =>
          approveMethodAudit(
            row.id,
          ),
      ),
    )

    ElMessage.success(
      `已批量通过 ${targets.length} 条考核方式申报`,
    )

    selectedRows.value =
      []

    await loadData()

  } catch {
    // request统一提示
  } finally {

    batchAuditing.value =
      false
  }
}

onMounted(
  loadData,
)
</script>

<template>
  <div class="audit-page">
    <!-- 查询 -->
    <el-card shadow="never">
      <el-form
        inline
        class="filter-form"
        @submit.prevent="onQuery"
      >
        <el-form-item label="课程">
          <el-input
            v-model="
              query.courseName
            "
            placeholder="课程名称 / 课程代码"
            clearable
            style="width: 200px"
            @keyup.enter="onQuery"
          />
        </el-form-item>

        <el-form-item label="申报教师">
          <el-input
            v-model="
              query.teacher
            "
            placeholder="教师姓名"
            clearable
            style="width: 150px"
            @keyup.enter="onQuery"
          />
        </el-form-item>

        <el-form-item label="审核状态">
          <el-select
            v-model="
              query.status
            "
            style="width: 130px"
          >
            <el-option
              v-for="
                opt in STATUS_OPTIONS
              "
              :key="
                opt.value
              "
              :label="
                opt.label
              "
              :value="
                opt.value
              "
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :icon="Search"
            @click="onQuery"
          >
            查询
          </el-button>

          <el-button
            :icon="RefreshLeft"
            @click="onReset"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <template #header>
        <div class="table-head">
          <span class="table-head__title">
            考核方式申报审核
            （共 {{ rows.length }} 条）
          </span>

          <el-button
            type="success"
            plain
            :disabled="
              pendingSelected.length
              === 0
            "
            :loading="
              batchAuditing
            "
            @click="
              batchApprove
            "
          >
            批量通过{{
              pendingSelected.length
                ? `（${pendingSelected.length}）`
                : ''
            }}
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="rows"
        stripe
        style="width: 100%"
        @selection-change="
          onSelectionChange
        "
      >
        <el-table-column
          type="selection"
          width="48"
        />

        <el-table-column
          label="课程"
          min-width="190"
        >
          <template #default="{ row }">
            <div>
              {{ row.courseName }}
            </div>

            <div
              v-if="
                row.courseCode
              "
              class="sub-text"
            >
              {{ row.courseCode }}
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="teacherName"
          label="申报教师"
          width="120"
        />

        <el-table-column
          prop="applyType"
          label="考核方式"
          min-width="140"
        />

        <el-table-column
          prop="reason"
          label="申报说明"
          min-width="260"
          show-overflow-tooltip
        />

        <el-table-column
          prop="createTime"
          label="提交时间"
          width="180"
        />

        <el-table-column
          label="审核状态"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="
                statusMeta(
                  row.status,
                ).type
              "
              effect="dark"
              disable-transitions
            >
              {{
                statusMeta(
                  row.status,
                ).text
              }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="180"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <template
              v-if="
                row.status
                === 'WAIT'
              "
            >
              <el-button
                type="success"
                size="small"
                :loading="
                  auditingId
                  === row.id
                "
                @click="
                  auditRow(
                    row,
                    'approve',
                  )
                "
              >
                通过
              </el-button>

              <el-button
                type="danger"
                size="small"
                :loading="
                  auditingId
                  === row.id
                "
                @click="
                  auditRow(
                    row,
                    'reject',
                  )
                "
              >
                驳回
              </el-button>
            </template>

            <span
              v-else
              class="audited-text"
            >
              已审核
            </span>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty
            description="没有符合条件的考核方式申报"
            :image-size="80"
          />
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

.sub-text {
  margin-top: 2px;
  color: var(
    --el-text-color-secondary
  );
  font-size: 12px;
}

.audited-text {
  color: var(
    --el-text-color-placeholder
  );
  font-size: 13px;
}
</style>
