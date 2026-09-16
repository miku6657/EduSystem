<script setup lang="ts">
/**
 * 教师 · 我的签到
 * 数据源：GET  /api/teacher-attendance/list-by-teacher?teacherId&startDate&endDate
 *          （本人近 7 天记录，1 次请求即可，无需逐日拉全校再筛本人）
 *         GET  /api/teacher-attendance/stat-by-date?date（全校当日统计）
 *         POST /api/teacher-attendance/check-in/{teacherId}（重复签到会返回失败）
 *
 * 结构：顶部卡片头（PageHeader + 主操作 + StatBar）+ PageState 三态。
 */
import { computed, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { checkIn, listMyAttendance, statByDate } from '@/api/teacherAttendance'
import type { TeacherAttendanceRecord, TeacherAttendanceStat } from '@/api/teacherAttendance'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import PageState from '@/components/PageState.vue'
import StatBar from '@/components/StatBar.vue'
import { ATTENDANCE_STATUS_TYPE } from '@/constants/dict'
import { addDays, todayStr } from '@/utils/format'

const userStore = useUserStore()
/** 教师工号；0 = 业务身份未解析成功 */
const teacherId = computed(() => userStore.businessId)
const today = todayStr()
/** 近 7 天区间起点 */
const weekStart = addDays(today, -6)
const submitting = ref(false)

/** 最近 7 天中的某一天（status 为空表示当天无记录） */
interface DayAttendance {
  date: string
  status: string
  checkTime: string
}

/** 本人近 7 天签到记录 */
const {
  data: myRecords,
  loading,
  error,
  reload,
} = useAsyncData<TeacherAttendanceRecord[]>(
  () =>
    teacherId.value ? listMyAttendance(teacherId.value, weekStart, today) : Promise.resolve([]),
  [],
)
/** 今日全校出勤统计 */
const { data: stat, reload: reloadStat } = useAsyncData<TeacherAttendanceStat>(
  () => statByDate(today),
  {},
)

/** 今日本人签到记录 */
const myToday = computed(() => myRecords.value.find((item) => item.attendanceDate === today))

/** 近 7 天逐日视图：缺失的日期显示"未签到" */
const recent = computed<DayAttendance[]>(() => {
  const map = new Map(myRecords.value.map((item) => [item.attendanceDate, item]))
  return Array.from({ length: 7 }, (_, index) => {
    const date = addDays(today, -index)
    const record = map.get(date)
    return { date, status: record?.status ?? '', checkTime: record?.checkTime ?? '' }
  })
})

/** 今日统计（后端字段缺失时用总数兜底） */
const statView = computed(() => {
  const total = Number(stat.value.total ?? 0)
  const checked = Number(stat.value.checked ?? 0)
  return { total, checked, unchecked: Number(stat.value.unchecked ?? total - checked) }
})

/** 统计条数据（对齐公共组件 StatBar）：今日全校签到概览 */
const summaryItems = computed(() => [
  { label: '教师总数', value: statView.value.total },
  { label: '已签到', value: statView.value.checked },
  { label: '未签到', value: statView.value.unchecked },
])

/** 签到按钮文案：已有记录时仍可点击，重复签到由接口返回失败并 toast */
const checkButtonText = computed(() => {
  if (!myToday.value) {
    return '立即签到'
  }
  return myToday.value.status === '缺勤' ? '今日已记录缺勤' : '今日已签到'
})

/** 签到时间只显示 HH:mm（'2026-09-11 07:52' → '07:52'） */
function timeText(checkTime?: string): string {
  if (!checkTime) {
    return ''
  }
  return checkTime.replace('T', ' ').slice(11, 16)
}

function reloadAll() {
  reload()
  reloadStat()
}

async function onCheckIn() {
  if (!teacherId.value) {
    showToast('未解析到教师工号')
    return
  }
  submitting.value = true
  try {
    await checkIn(teacherId.value)
    showToast('签到成功')
  } catch {
    // 失败原因（如"今日已签到"）由请求层统一 toast
  } finally {
    submitting.value = false
    reloadAll()
  }
}

/** 工号未解析时重试解析业务身份 */
async function retryProfile() {
  await userStore.resolveProfile(true)
  reloadAll()
}

onMounted(reloadAll)
</script>

<template>
  <div>
    <van-empty v-if="!teacherId" description="未解析到教师工号">
      <van-button round type="primary" size="small" @click="retryProfile">重新解析身份</van-button>
    </van-empty>

    <template v-else>
      <!-- 顶部：标题 + 主操作 + 今日签到 + 概览（对齐 admin 的卡片头结构） -->
      <div class="st-card">
        <PageHeader title="我的签到">
          <template #actions>
            <van-button size="small" type="primary" :loading="submitting" @click="onCheckIn">
              {{ checkButtonText }}
            </van-button>
          </template>
        </PageHeader>

        <!-- 今日签到状态 -->
        <div class="check__today">
          <div class="check__date">{{ today }}</div>
          <div class="check__status">
            <van-tag
              v-if="myToday"
              size="large"
              :type="ATTENDANCE_STATUS_TYPE[myToday?.status ?? ''] || 'primary'"
            >
              {{ myToday.status }}
            </van-tag>
            <van-tag v-else size="large" type="warning">未签到</van-tag>
          </div>
          <div class="st-muted">
            {{
              myToday
                ? `签到时间 ${timeText(myToday.checkTime) || myToday.checkTime || '—'}`
                : '今日暂无签到记录'
            }}
          </div>
        </div>

        <StatBar :items="summaryItems" />
      </div>

      <PageState :loading="loading && myRecords.length === 0" :error="error" @retry="reloadAll">
        <!-- 我最近 7 天的记录 -->
        <div class="st-section-title">我最近 7 天的记录</div>
        <div v-for="row in recent" :key="row.date" class="st-card st-row">
          <span>{{ row.date }}</span>
          <span class="check__recent-right">
            <van-tag
              v-if="row.status"
              plain
              :type="ATTENDANCE_STATUS_TYPE[row.status] || 'primary'"
            >
              {{ row.status }}
            </van-tag>
            <van-tag v-else plain>未签到</van-tag>
            <span class="st-muted">{{ timeText(row.checkTime) }}</span>
          </span>
        </div>
      </PageState>
    </template>
  </div>
</template>

<style scoped>
.check__today {
  margin-bottom: 12px;
  text-align: center;
}
.check__date {
  font-size: 14px;
  font-weight: 600;
}
.check__status {
  margin: 10px 0 6px;
}
.check__recent-right {
  display: flex;
  gap: 6px;
  align-items: center;
}
</style>
