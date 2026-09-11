<script setup lang="ts">
/**
 * 教师 · 我的签到
 * 数据源：GET  /api/teacher-attendance/list-by-date?date（返回全校，前端筛本人）
 *         GET  /api/teacher-attendance/stat-by-date?date
 *         POST /api/teacher-attendance/check-in/{teacherId}（重复签到会失败）
 */
import { computed, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { checkIn, listByDate, statByDate } from '@/api/teacherAttendance'
import type { TeacherAttendanceRecord, TeacherAttendanceStat } from '@/api/teacherAttendance'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import { ATTENDANCE_STATUS_TYPE } from '@/constants/dict'
import { addDays, todayStr } from '@/utils/format'

const userStore = useUserStore()
/** 教师工号；0 = 业务身份未解析成功 */
const teacherId = computed(() => userStore.businessId)
const today = todayStr()
const submitting = ref(false)

/** 最近 7 天中的某一天（status 为空表示当天无记录） */
interface DayAttendance {
  date: string
  status: string
  checkTime: string
}

/** 今日全校记录（后端无"按教师查"接口，只能取全校再筛本人） */
const { data: todayRecords, loading, error, reload } = useAsyncData<TeacherAttendanceRecord[]>(
  () => listByDate(today),
  [],
)
/** 今日出勤统计 */
const { data: stat, reload: reloadStat } = useAsyncData<TeacherAttendanceStat>(
  () => statByDate(today),
  {},
)
/** 最近 7 天：逐日请求后筛出本人记录（缺口见 README：无 list-by-teacher） */
const {
  data: recent,
  loading: recentLoading,
  error: recentError,
  reload: reloadRecent,
} = useAsyncData<DayAttendance[]>(async () => {
  if (!teacherId.value) {
    return []
  }
  const days = Array.from({ length: 7 }, (_, index) => addDays(today, -index))
  const lists = await Promise.all(days.map((date) => listByDate(date)))
  return days.map((date, index) => {
    const mine = lists[index].find((item) => item.teacherId === teacherId.value)
    return { date, status: mine?.status ?? '', checkTime: mine?.checkTime ?? '' }
  })
}, [])

/** 今日本人签到记录：有记录即已签到 */
const myToday = computed(() => todayRecords.value.find((item) => item.teacherId === teacherId.value))

/** 今日统计（后端字段缺失时用总数兜底） */
const statView = computed(() => {
  const total = Number(stat.value.total ?? 0)
  const checked = Number(stat.value.checked ?? 0)
  return { total, checked, unchecked: Number(stat.value.unchecked ?? total - checked) }
})

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
  reloadRecent()
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
      <!-- 今日签到卡 -->
      <div class="st-card check__today">
        <div class="check__date">{{ today }}</div>
        <div class="check__status">
          <van-tag v-if="myToday" size="large"
            :type="ATTENDANCE_STATUS_TYPE[myToday?.status ?? ''] || 'primary'">
            {{ myToday.status }}
          </van-tag>
          <van-tag v-else size="large" type="warning">未签到</van-tag>
        </div>
        <div class="st-muted">
          {{ myToday ? `签到时间 ${myToday.checkTime || '—'}` : '今日暂无签到记录' }}
        </div>
        <van-button class="check__btn" round block type="primary" :loading="submitting" @click="onCheckIn">
          {{ checkButtonText }}
        </van-button>
      </div>

      <div v-if="loading && todayRecords.length === 0" class="st-empty">
        <van-loading vertical>加载中…</van-loading>
      </div>

      <van-empty v-else-if="error" image="error" :description="error">
        <van-button round type="primary" size="small" @click="reloadAll">重新加载</van-button>
      </van-empty>

      <!-- 今日全校签到统计 -->
      <div v-else class="st-card check__stat">
        <div class="check__stat-item">
          <div class="check__stat-value">{{ statView.total }}</div>
          <div class="st-muted">教师总数</div>
        </div>
        <div class="check__stat-item">
          <div class="check__stat-value">{{ statView.checked }}</div>
          <div class="st-muted">已签到</div>
        </div>
        <div class="check__stat-item">
          <div class="check__stat-value">{{ statView.unchecked }}</div>
          <div class="st-muted">未签到</div>
        </div>
      </div>

      <!-- 我最近 7 天的记录 -->
      <div class="st-section-title">我最近 7 天的记录</div>

      <div v-if="recentLoading" class="st-empty">
        <van-loading vertical>加载中…</van-loading>
      </div>

      <van-empty v-else-if="recentError" image="error" :description="recentError">
        <van-button round type="primary" size="small" @click="reloadRecent">重新加载</van-button>
      </van-empty>

      <van-empty v-else-if="recent.length === 0" description="暂无签到记录" />

      <template v-else>
        <div v-for="row in recent" :key="row.date" class="st-card st-row">
          <span>{{ row.date }}</span>
          <span class="check__recent-right">
            <van-tag v-if="row.status" plain :type="ATTENDANCE_STATUS_TYPE[row.status] || 'primary'">
              {{ row.status }}
            </van-tag>
            <van-tag v-else plain>未签到</van-tag>
            <span class="st-muted">{{ timeText(row.checkTime) }}</span>
          </span>
        </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.check__today { text-align: center; }
.check__date { font-size: 14px; font-weight: 600; }
.check__status { margin: 10px 0 6px; }
.check__btn { margin-top: 14px; }
.check__stat { display: flex; justify-content: space-around; text-align: center; }
.check__stat-value { font-size: 20px; font-weight: 600; }
.check__recent-right { display: flex; gap: 6px; align-items: center; }
</style>
