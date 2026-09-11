<script setup lang="ts">
/**
 * 学生 · 我的考勤
 * 数据源：GET /api/student-attendance/list-by-student?studentId&startDate&endDate
 *
 * 结构与 scores.vue 保持一致：useAsyncData + 显式 import + Vant 组件 + 下拉刷新 + 空/错误状态。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { listMyAttendance } from '@/api/attendance'
import type { StudentAttendanceRecord } from '@/api/attendance'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import { addDays, currentWeekRange, todayStr } from '@/utils/format'
import { ATTENDANCE_STATUS_TYPE } from '@/constants/dict'

const userStore = useUserStore()

/** 快捷时间范围：本周 / 近两周 / 近30天 */
type RangeKey = 'week' | 'biweek' | 'month'
const rangeKey = ref<RangeKey>('week')
const refreshing = ref(false)

/** 起止日期：本周取周一~周日，另两个按含今天往前推算 */
const dateRange = computed(() => {
  const end = todayStr()
  if (rangeKey.value === 'week') {
    const range = currentWeekRange()
    return { start: range.start, end: range.end }
  }
  return { start: addDays(end, rangeKey.value === 'biweek' ? -13 : -29), end }
})

const { data: records, loading, error, reload } = useAsyncData<StudentAttendanceRecord[]>(
  () =>
    userStore.businessId
      ? listMyAttendance(userStore.businessId, dateRange.value.start, dateRange.value.end)
      : Promise.resolve([]),
  [],
)

/** 概览：出勤率 = (正常 + 迟到) / 总数，无记录时为 null（模板显示 —） */
const summary = computed(() => {
  const count = (status: string) => records.value.filter((row) => row.status === status).length
  const total = records.value.length
  const normal = count('正常')
  const late = count('迟到')
  return {
    total,
    normal,
    late,
    absent: count('缺勤'),
    leave: count('请假'),
    rate: total ? Math.round(((normal + late) / total) * 100) : null,
  }
})

/** 出勤率文案：无记录显示 — */
const rateText = computed(() => (summary.value.rate === null ? '—' : `${summary.value.rate}%`))

/** 切换快捷区间后按新日期重新查询 */
watch(rangeKey, () => {
  void reload()
})

async function onRefresh() {
  refreshing.value = true
  try {
    await reload()
  } finally {
    refreshing.value = false
  }
}

onMounted(reload)
</script>

<template>
  <div>
    <!-- 概览：出勤率 + 四项计数（前端 computed） -->
    <div class="st-card attendance__summary">
      <div class="attendance__rate">
        <div class="attendance__rate-value">{{ rateText }}</div>
        <div class="st-muted">出勤率（正常 + 迟到）/ 共 {{ summary.total }} 条</div>
      </div>
      <div class="attendance__counts">
        <div class="attendance__count">
          <div class="attendance__count-value">{{ summary.normal }}</div>
          <div class="st-muted">正常</div>
        </div>
        <div class="attendance__count">
          <div class="attendance__count-value">{{ summary.late }}</div>
          <div class="st-muted">迟到</div>
        </div>
        <div class="attendance__count">
          <div class="attendance__count-value">{{ summary.absent }}</div>
          <div class="st-muted">缺勤</div>
        </div>
        <div class="attendance__count">
          <div class="attendance__count-value">{{ summary.leave }}</div>
          <div class="st-muted">请假</div>
        </div>
      </div>
    </div>

    <van-tabs v-model:active="rangeKey" sticky>
      <van-tab title="本周" name="week" />
      <van-tab title="近两周" name="biweek" />
      <van-tab title="近30天" name="month" />
    </van-tabs>

    <div class="attendance__range st-muted">统计区间：{{ dateRange.start }} ~ {{ dateRange.end }}</div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 未解析到学号：明确提示，不发请求 -->
      <van-empty v-if="!userStore.businessId" description="未解析到学号，请确认登录账号为学号" />

      <div v-else-if="loading && !refreshing" class="st-empty">
        <van-loading vertical>加载中…</van-loading>
      </div>

      <van-empty v-else-if="error" image="error" :description="error">
        <van-button round type="primary" size="small" @click="reload">重新加载</van-button>
      </van-empty>

      <van-empty v-else-if="records.length === 0" description="该时间段内暂无考勤记录" />

      <template v-else>
        <div
          v-for="row in records"
          :key="row.id ?? `${row.attendanceDate}-${row.courseId}`"
          class="st-card"
        >
          <div class="st-row">
            <div class="attendance__course">{{ row.courseName || `课程#${row.courseId}` }}</div>
            <van-tag :type="ATTENDANCE_STATUS_TYPE[row.status] || 'primary'">
              {{ row.status || '—' }}
            </van-tag>
          </div>
          <div class="attendance__meta st-muted">{{ row.attendanceDate || '—' }}</div>
        </div>
      </template>
    </van-pull-refresh>
  </div>
</template>

<style scoped>
.attendance__summary {
  padding: 14px 12px;
}

.attendance__rate {
  padding-bottom: 10px;
  text-align: center;
  border-bottom: 1px solid var(--st-border);
}

.attendance__rate-value {
  font-size: 26px;
  font-weight: 600;
  color: var(--st-primary);
}

.attendance__counts {
  display: flex;
  padding-top: 10px;
  text-align: center;
}

.attendance__count {
  flex: 1;
}

.attendance__count-value {
  font-size: 18px;
  font-weight: 600;
}

.attendance__range {
  margin: 8px 4px 10px;
}

.attendance__course {
  font-size: 15px;
  font-weight: 600;
}

.attendance__meta {
  margin-top: 6px;
}
</style>
