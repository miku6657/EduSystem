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
  approveRetake,
  arrangeRetake,
  listRetakeAudits,
  rejectRetake,
} from '@/api/retake'

import type {
  RetakeAuditItem,
  RetakeStatus,
} from '@/api/retake'

import {
  getArrangeTeachers,
  getFreeClassrooms,
} from '@/api/exam'

import type {
  ClassroomItem,
  TeacherItem,
} from '@/api/exam'

import {
  getCurrentTerm,
} from '@/api/term'

import type {
  Term,
} from '@/api/term'

const rows =
  ref<RetakeAuditItem[]>([])

const classrooms =
  ref<ClassroomItem[]>([])

const teachers =
  ref<TeacherItem[]>([])

const currentTerm =
  ref<Term | null>(null)

const loading =
  ref(false)

const arranging =
  ref(false)

const operatingId =
  ref('')

const statusFilter =
  ref<'' | RetakeStatus>('')

const arrangeVisible =
  ref(false)

const currentRow =
  ref<RetakeAuditItem | null>(
    null,
  )

/**
 * 创建专门重修考试的表单。
 */
const arrangeForm =
  reactive({
    examDate: '',

    startTime: '',

    endTime: '',

    classroomId: '',

    teacherIds:
      [] as string[],
  })

const filteredRows =
  computed(() => {

    if (!statusFilter.value) {
      return rows.value
    }

    return rows.value.filter(
      item =>
        item.status
        === statusFilter.value,
    )
  })

/**
 * 状态文字。
 */
function statusText(
  status: RetakeStatus,
) {

  const map:
    Record<
      RetakeStatus,
      string
    > = {
      WAIT:
        '待审批',

      APPROVED:
        '已批准',

      REJECTED:
        '已驳回',

      ARRANGED:
        '已安排考试',

      COMPLETED:
        '重修完成',
    }

  return map[status]
}

function statusType(
  status: RetakeStatus,
):
  | 'success'
  | 'warning'
  | 'danger'
  | 'info'
  | 'primary' {

  if (
    status === 'WAIT'
  ) {
    return 'warning'
  }

  if (
    status === 'APPROVED'
  ) {
    return 'primary'
  }

  if (
    status === 'REJECTED'
  ) {
    return 'danger'
  }

  if (
    status === 'COMPLETED'
  ) {
    return 'success'
  }

  return 'info'
}

/**
 * 页面加载：
 *
 * 1. 重修申请
 * 2. 当前学期
 * 3. 教室
 * 4. 教师
 */
async function loadData() {

  loading.value =
    true

  try {

    const [
      retakeRows,
      term,
      roomRows,
      teacherPage,
    ] =
      await Promise.all([
        listRetakeAudits(),

        getCurrentTerm(),

        getFreeClassrooms(),

        getArrangeTeachers(),
      ])

    rows.value =
      retakeRows

    currentTerm.value =
      term

    classrooms.value =
      roomRows

    teachers.value =
      teacherPage.records

  } catch {
    // request.ts统一提示
  } finally {

    loading.value =
      false
  }
}

/**
 * 通过。
 *
 * WAIT -> APPROVED
 */
async function onApprove(
  row: RetakeAuditItem,
) {

  try {

    await ElMessageBox.confirm(
      `确定通过 ${row.studentName} 的「${row.courseName}」重修申请吗？`,
      '重修审批',
      {
        type:
          'warning',

        confirmButtonText:
          '通过',

        cancelButtonText:
          '取消',
      },
    )

  } catch {
    return
  }

  operatingId.value =
    row.id

  try {

    await approveRetake(
      row.id,
    )

    ElMessage.success(
      '审批通过',
    )

    await loadData()

  } catch {
    // request统一提示
  } finally {

    operatingId.value =
      ''
  }
}

/**
 * 驳回。
 *
 * WAIT -> REJECTED
 */
async function onReject(
  row: RetakeAuditItem,
) {

  try {

    await ElMessageBox.confirm(
      `确定驳回 ${row.studentName} 的「${row.courseName}」重修申请吗？`,
      '驳回申请',
      {
        type:
          'warning',

        confirmButtonText:
          '确认驳回',

        cancelButtonText:
          '取消',
      },
    )

  } catch {
    return
  }

  operatingId.value =
    row.id

  try {

    await rejectRetake(
      row.id,
    )

    ElMessage.success(
      '申请已驳回',
    )

    await loadData()

  } catch {
    // request统一提示
  } finally {

    operatingId.value =
      ''
  }
}

/**
 * 打开“安排重修考试”。
 */
function openArrange(
  row: RetakeAuditItem,
) {

  if (!currentTerm.value) {

    ElMessage.warning(
      '当前没有可用学期',
    )

    return
  }

  if (
    classrooms.value.length === 0
  ) {

    ElMessage.warning(
      '当前没有可用教室',
    )

    return
  }

  if (
    teachers.value.length === 0
  ) {

    ElMessage.warning(
      '当前没有教师数据',
    )

    return
  }

  currentRow.value =
    row

  arrangeForm.examDate =
    ''

  arrangeForm.startTime =
    ''

  arrangeForm.endTime =
    ''

  arrangeForm.classroomId =
    ''

  arrangeForm.teacherIds =
    []

  arrangeVisible.value =
    true
}

/**
 * 防止选择过去日期。
 */
function disabledDate(
  date: Date,
) {

  const today =
    new Date()

  today.setHours(
    0,
    0,
    0,
    0,
  )

  return (
    date.getTime()
    <
    today.getTime()
  )
}

/**
 * 真正创建专门重修考试。
 */
async function confirmArrange() {

  if (
    !currentRow.value
  ) {
    return
  }

  if (
    !currentTerm.value
  ) {

    ElMessage.warning(
      '当前学期不存在',
    )

    return
  }

  if (
    !arrangeForm.examDate
  ) {

    ElMessage.warning(
      '请选择考试日期',
    )

    return
  }

  if (
    !arrangeForm.startTime
    ||
    !arrangeForm.endTime
  ) {

    ElMessage.warning(
      '请选择考试时间',
    )

    return
  }

  if (
    arrangeForm.startTime
    >=
    arrangeForm.endTime
  ) {

    ElMessage.warning(
      '结束时间必须晚于开始时间',
    )

    return
  }

  if (
    !arrangeForm.classroomId
  ) {

    ElMessage.warning(
      '请选择考试教室',
    )

    return
  }

  if (
    arrangeForm.teacherIds.length
    === 0
  ) {

    ElMessage.warning(
      '请选择监考教师',
    )

    return
  }

  try {

    await ElMessageBox.confirm(
      `确定为 ${currentRow.value.studentName} 创建「${currentRow.value.courseName}重修考试」吗？`,
      '确认安排考试',
      {
        type:
          'warning',

        confirmButtonText:
          '确认安排',

        cancelButtonText:
          '取消',
      },
    )

  } catch {
    return
  }

  arranging.value =
    true

  try {

    await arrangeRetake(
      currentRow.value.id,
      {
        examInfo: {
          termId:
            String(
              currentTerm.value.id,
            ),

          examDate:
            arrangeForm.examDate,

          startTime:
            arrangeForm.startTime,

          endTime:
            arrangeForm.endTime,
        },

        classroomIds: [
          arrangeForm.classroomId,
        ],

        monitorTeacherIds:
          arrangeForm.teacherIds,
      },
    )

    ElMessage.success(
      '重修考试创建成功',
    )

    arrangeVisible.value =
      false

    await loadData()

  } catch {
    /**
     * 如果时间冲突，
     * 后端原有排考逻辑会直接返回：
     *
     * 教室冲突
     * 或
     * 监考教师冲突
     *
     * request.ts统一显示。
     */
  } finally {

    arranging.value =
      false
  }
}

onMounted(
  loadData,
)
</script>

<template>
  <div class="retake-page">
    <!-- 顶部 -->
    <el-card shadow="never">
      <div class="toolbar">
        <div>
          <div class="title">
            补考重修审批
          </div>

          <div class="sub">
            审批重修申请并创建专门的重修考试
          </div>
        </div>

        <div class="toolbar__right">
          <el-select
            v-model="statusFilter"
            style="width: 150px"
          >
            <el-option
              label="全部状态"
              value=""
            />

            <el-option
              label="待审批"
              value="WAIT"
            />

            <el-option
              label="已批准"
              value="APPROVED"
            />

            <el-option
              label="已驳回"
              value="REJECTED"
            />

            <el-option
              label="已安排"
              value="ARRANGED"
            />

            <el-option
              label="已完成"
              value="COMPLETED"
            />
          </el-select>

          <el-button
            @click="
              loadData
            "
          >
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="filteredRows"
        stripe
      >
        <el-table-column
          label="学生"
          width="160"
        >
          <template #default="{ row }">
            <div>
              {{ row.studentName }}
            </div>

            <div class="sub">
              {{ row.studentNo }}
            </div>
          </template>
        </el-table-column>

        <el-table-column
          label="挂科课程"
          min-width="190"
        >
          <template #default="{ row }">
            <div>
              {{ row.courseName }}
            </div>

            <div class="sub">
              {{ row.courseCode }}
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="type"
          label="申请类型"
          width="100"
        />

        <el-table-column
          label="状态"
          width="130"
        >
          <template #default="{ row }">
            <el-tag
              :type="
                statusType(
                  row.status,
                )
              "
            >
              {{
                statusText(
                  row.status,
                )
              }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="考试"
          min-width="210"
        >
          <template #default="{ row }">
            <span
              v-if="row.examName"
            >
              {{ row.examName }}
            </span>

            <span
              v-else
              class="sub"
            >
              尚未安排
            </span>
          </template>
        </el-table-column>

        <el-table-column
          prop="createTime"
          label="申请时间"
          width="180"
        />

        <el-table-column
          label="操作"
          width="220"
          fixed="right"
        >
          <template #default="{ row }">
            <!-- 待审批 -->
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
                  operatingId
                  === row.id
                "
                @click="
                  onApprove(row)
                "
              >
                通过
              </el-button>

              <el-button
                type="danger"
                plain
                size="small"
                :loading="
                  operatingId
                  === row.id
                "
                @click="
                  onReject(row)
                "
              >
                驳回
              </el-button>
            </template>

            <!-- 已批准 -->
            <el-button
              v-else-if="
                row.status
                === 'APPROVED'
              "
              type="primary"
              size="small"
              @click="
                openArrange(row)
              "
            >
              安排考试
            </el-button>

            <el-tag
              v-else-if="
                row.status
                === 'ARRANGED'
              "
              type="info"
            >
              等待录入成绩
            </el-tag>

            <el-tag
              v-else-if="
                row.status
                === 'COMPLETED'
              "
              type="success"
            >
              已完成
            </el-tag>

            <span
              v-else
              class="sub"
            >
              无需操作
            </span>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty
            description="暂无重修申请"
          />
        </template>
      </el-table>
    </el-card>

    <!-- 创建专门重修考试 -->
    <el-dialog
      v-model="arrangeVisible"
      title="安排重修考试"
      width="620px"
      destroy-on-close
    >
      <template
        v-if="currentRow"
      >
        <el-descriptions
          :column="1"
          border
        >
          <el-descriptions-item
            label="学生"
          >
            {{
              currentRow.studentName
            }}

            （{{
              currentRow.studentNo
            }}）
          </el-descriptions-item>

          <el-descriptions-item
            label="课程"
          >
            {{
              currentRow.courseName
            }}

            （{{
              currentRow.courseCode
            }}）
          </el-descriptions-item>

          <el-descriptions-item
            label="考试名称"
          >
            {{
              currentRow.courseName
            }}重修考试
          </el-descriptions-item>

          <el-descriptions-item
            label="考试类型"
          >
            重修考试
          </el-descriptions-item>

          <el-descriptions-item
            label="所属学期"
          >
            {{
              currentTerm?.name
              || '当前学期'
            }}
          </el-descriptions-item>
        </el-descriptions>

        <el-form
          label-width="100px"
          style="margin-top: 20px"
        >
          <!-- 日期 -->
          <el-form-item
            label="考试日期"
            required
          >
            <el-date-picker
              v-model="
                arrangeForm.examDate
              "
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择考试日期"
              :disabled-date="
                disabledDate
              "
              style="width: 100%"
            />
          </el-form-item>

          <!-- 开始时间 -->
          <el-form-item
            label="开始时间"
            required
          >
            <el-time-picker
              v-model="
                arrangeForm.startTime
              "
              value-format="HH:mm:ss"
              format="HH:mm"
              placeholder="请选择开始时间"
              style="width: 100%"
            />
          </el-form-item>

          <!-- 结束时间 -->
          <el-form-item
            label="结束时间"
            required
          >
            <el-time-picker
              v-model="
                arrangeForm.endTime
              "
              value-format="HH:mm:ss"
              format="HH:mm"
              placeholder="请选择结束时间"
              style="width: 100%"
            />
          </el-form-item>

          <!-- 教室 -->
          <el-form-item
            label="考试教室"
            required
          >
            <el-select
              v-model="
                arrangeForm.classroomId
              "
              filterable
              placeholder="请选择教室"
              style="width: 100%"
            >
              <el-option
                v-for="
                  room
                  in classrooms
                "
                :key="
                  String(room.id)
                "
                :label="
                  room.roomNo
                "
                :value="
                  String(room.id)
                "
              />
            </el-select>
          </el-form-item>

          <!-- 监考 -->
          <el-form-item
            label="监考教师"
            required
          >
            <el-select
              v-model="
                arrangeForm.teacherIds
              "
              multiple
              filterable
              collapse-tags
              placeholder="请选择监考教师"
              style="width: 100%"
            >
              <el-option
                v-for="
                  teacher
                  in teachers
                "
                :key="
                  String(
                    teacher.id,
                  )
                "
                :label="
                  teacher.name
                "
                :value="
                  String(
                    teacher.id,
                  )
                "
              />
            </el-select>
          </el-form-item>
        </el-form>

        <el-alert
          type="info"
          :closable="false"
          title="系统会在提交时检查教室和监考教师是否存在时间冲突。"
        />
      </template>

      <template #footer>
        <el-button
          @click="
            arrangeVisible =
              false
          "
        >
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="arranging"
          @click="
            confirmArrange
          "
        >
          创建重修考试
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.retake-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar__right {
  display: flex;
  gap: 10px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.sub {
  margin-top: 3px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
