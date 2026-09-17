<script setup lang="ts">
import {
  computed,
  onMounted,
  ref,
} from 'vue'

import {
  getTeacherAttendanceStat,
  listTeacherAttendanceByDate,
} from '@/api/teacher-attendance'

import type {
  TeacherAttendanceRecord,
  TeacherAttendanceStat,
} from '@/api/teacher-attendance'

const today =
  new Date()
    .toISOString()
    .slice(0, 10)

const queryDate =
  ref(today)

const teacherName =
  ref('')

const loading =
  ref(false)

const records =
  ref<
    TeacherAttendanceRecord[]
  >([])

const stat =
  ref<
    TeacherAttendanceStat
  >({})

/**
 * 前端教师姓名筛选。
 *
 * 后端没有teacherName条件接口，
 * 所以不造接口。
 */
const rows =
  computed(() => {

    const keyword =
      teacherName.value
        .trim()
        .toLowerCase()

    if (!keyword) {
      return records.value
    }

    return records.value.filter(
      (item) =>
        item.teacherName
          ?.toLowerCase()
          .includes(
            keyword,
          )
        ||
        item.teacherNo
          ?.toLowerCase()
          .includes(
            keyword,
          ),
    )
  })

const summary =
  computed(() => {

    const total =
      Number(
        stat.value.total
        ?? 0,
      )

    const checked =
      Number(
        stat.value.checked
        ?? 0,
      )

    const absent =
      Number(
        stat.value.absent
        ?? (
          total
          - checked
        ),
      )

    return {
      total,
      checked,
      absent,
    }
  })

async function loadData() {

  if (!queryDate.value) {
    return
  }

  loading.value =
    true

  try {

    const [
      list,
      statistics,
    ] =
      await Promise.all([
        listTeacherAttendanceByDate(
          queryDate.value,
        ),

        getTeacherAttendanceStat(
          queryDate.value,
        ),
      ])

    records.value =
      list

    stat.value =
      statistics

  } catch {
    // request.ts已经统一提示
  } finally {

    loading.value =
      false
  }
}

function resetQuery() {

  queryDate.value =
    today

  teacherName.value =
    ''

  loadData()
}

/**
 * 签到时间格式。
 */
function checkTimeText(
  value?: string,
) {

  if (!value) {
    return '—'
  }

  return value
    .replace(
      'T',
      ' ',
    )
    .slice(
      0,
      16,
    )
}

function statusType(
  status?: string,
) {

  if (
    status === '正常'
  ) {
    return 'success'
  }

  if (
    status === '迟到'
  ) {
    return 'warning'
  }

  if (
    status === '缺勤'
  ) {
    return 'danger'
  }

  return 'info'
}

onMounted(
  loadData,
)
</script>

<template>
  <div class="attendance-page">
    <!-- 查询条件 -->
    <el-card shadow="never">
      <el-form
        inline
        @submit.prevent="
          loadData
        "
      >
        <el-form-item label="考勤日期">
          <el-date-picker
            v-model="
              queryDate
            "
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择日期"
          />
        </el-form-item>

        <el-form-item label="教师">
          <el-input
            v-model="
              teacherName
            "
            clearable
            placeholder="教师姓名 / 工号"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="
              loadData
            "
          >
            查询
          </el-button>

          <el-button
            @click="
              resetQuery
            "
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 当日统计 -->
    <div class="attendance-stat">
      <el-card shadow="never">
        <div class="stat-item">
          <div class="stat-value">
            {{
              summary.total
            }}
          </div>

          <div class="stat-label">
            教师总数
          </div>
        </div>
      </el-card>

      <el-card shadow="never">
        <div class="stat-item">
          <div class="stat-value">
            {{
              summary.checked
            }}
          </div>

          <div class="stat-label">
            已签到
          </div>
        </div>
      </el-card>

      <el-card shadow="never">
        <div class="stat-item">
          <div class="stat-value">
            {{
              summary.absent
            }}
          </div>

          <div class="stat-label">
            未签到
          </div>
        </div>
      </el-card>
    </div>

    <!-- 考勤记录 -->
    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <strong>
            教师考勤记录
          </strong>

          <span class="table-count">
            {{
              queryDate
            }}
            · 共
            {{
              rows.length
            }}
            条签到记录
          </span>
        </div>
      </template>

      <el-table
        v-loading="
          loading
        "
        :data="
          rows
        "
        stripe
        style="
          width: 100%
        "
      >
        <el-table-column
          prop="teacherName"
          label="教师姓名"
          min-width="140"
        />

        <el-table-column
          prop="teacherNo"
          label="教师工号"
          min-width="130"
        />

        <el-table-column
          prop="attendanceDate"
          label="考勤日期"
          width="130"
        />

        <el-table-column
          label="签到状态"
          width="120"
          align="center"
        >
          <template
            #default="{ row }"
          >
            <el-tag
              :type="
                statusType(
                  row.status,
                )
              "
            >
              {{
                row.status
              }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="签到时间"
          min-width="180"
        >
          <template
            #default="{ row }"
          >
            {{
              checkTimeText(
                row.checkTime,
              )
            }}
          </template>
        </el-table-column>

        <template #empty>
          <el-empty
            description="该日期暂无教师签到记录"
          />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.attendance-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.attendance-stat {
  display: grid;
  grid-template-columns:
    repeat(
      3,
      1fr
    );
  gap: 14px;
}

.stat-item {
  padding: 8px 0;
  text-align: center;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
}

.stat-label {
  margin-top: 5px;
  color:
    var(
      --el-text-color-secondary
    );
  font-size: 13px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-count {
  color:
    var(
      --el-text-color-secondary
    );
  font-size: 13px;
}

@media (
  max-width: 900px
) {
  .attendance-stat {
    grid-template-columns:
      1fr;
  }
}
</style>
