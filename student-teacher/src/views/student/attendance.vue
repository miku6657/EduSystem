<script setup lang="ts">
/**
 * 学生 · 我的考勤
 * 数据源：GET /api/student-attendances/students/{studentId}?startDate&endDate
 *
 * 结构与 scores.vue 保持一致：顶部卡片头（PageHeader + StatBar）+ useAsyncData +
 * 显式 import + Vant 组件 + 下拉刷新 + PageState 三态。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { listMyAttendance } from '@/api/attendance'
import type { StudentAttendanceRecord } from '@/api/attendance'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import PageState from '@/components/PageState.vue'
import StatBar from '@/components/StatBar.vue'
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

const {
  data: records,
  loading,
  error,
  reload,
} = useAsyncData<StudentAttendanceRecord[]>(
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

/** 统计条数据（对齐公共组件 StatBar） */
const summaryItems = computed(() => [
  { label: '出勤率', value: rateText.value },
  { label: '正常', value: summary.value.normal },
  { label: '迟到', value: summary.value.late },
  { label: '缺勤', value: summary.value.absent },
  { label: '请假', value: summary.value.leave },
])

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
    <!-- 顶部：标题 + 概览（对齐 admin 的卡片头结构） -->
    <div class="st-card">
      <PageHeader title="我的考勤" />
      <StatBar :items="summaryItems" />
      <div class="attendance__hint st-muted">
        出勤率 =（正常 + 迟到）/ 共 {{ summary.total }} 条
      </div>
    </div>

    <van-tabs v-model:active="rangeKey" sticky>
      <van-tab title="本周" name="week" />
      <van-tab title="近两周" name="biweek" />
      <van-tab title="近30天" name="month" />
    </van-tabs>

    <div class="attendance__range st-muted">
      统计区间：{{ dateRange.start }} ~ {{ dateRange.end }}
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 未解析到学号时优先提示、不发请求，因此不进入加载态 -->
      <PageState
        :loading="loading && !refreshing && !!userStore.businessId"
        :error="error"
        :empty="!userStore.businessId || records.length === 0"
        :empty-text="
          userStore.businessId ? '该时间段内暂无考勤记录' : '当前账号未绑定学生档案'
        "
        @retry="reload"
      >
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
      </PageState>
    </van-pull-refresh>
  </div>
</template>

<style scoped>
.attendance__hint {
  margin-top: 6px;
  font-size: 12px;
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
