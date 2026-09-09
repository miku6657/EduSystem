<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, RefreshLeft, Tickets } from '@element-plus/icons-vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import zhCnLocale from '@fullcalendar/core/locales/zh-cn'
import type { CalendarOptions, DayCellMountArg, EventClickArg, EventInput } from '@fullcalendar/core'
import {
  approveClassroomApply,
  getClassroomApprovalList,
  getClassroomOccupancy,
  rejectClassroomApply,
  type ClassroomApplyRecord,
  type ClassroomApprovalFilter,
  type ClassroomCalendarEvent,
} from '@/api/classroom'

/* ==================== 工具 ==================== */

function toDateText(d: Date) {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function addDaysText(base: string, days: number) {
  const d = new Date(`${base}T00:00:00`)
  d.setDate(d.getDate() + days)
  return toDateText(d)
}

/* ==================== 上半区：教室占用总览 ==================== */

const calendarRef = ref<InstanceType<typeof FullCalendar>>()
const occupancyLoading = ref(false)
const occupancy = ref<ClassroomCalendarEvent[]>([])
/** 日历当前“自然月”（start 含 / end 不含） */
const monthStart = ref('')
const monthEnd = ref('')

/** 空闲日期单元格（date → td），供红/黄/绿着色 */
const dayCellEls = new Map<string, HTMLElement>()

/** 事件颜色：红 = 考试/教学占用或已通过；黄 = 待审核申请 */
const STATUS_COLOR: Record<ClassroomCalendarEvent['status'], string> = {
  exam: '#f56c6c',
  class: '#f56c6c',
  approved: '#f56c6c',
  pending: '#e6a23c',
}

/** 日期 → 占用事件（用于详情弹窗与单元格着色） */
const dayItemsMap = computed(() => {
  const map = new Map<string, ClassroomCalendarEvent[]>()
  for (const item of occupancy.value) {
    const list = map.get(item.date)
    if (list) list.push(item)
    else map.set(item.date, [item])
  }
  return map
})

const stats = computed(() => {
  const monthTotal = monthDays.value.length
  const busyCount = monthDays.value.filter((date) => (dayItemsMap.value.get(date)?.length ?? 0) > 0).length
  return { monthTotal, busyCount, freeCount: monthTotal - busyCount }
})

const monthDays = computed(() => {
  if (!monthStart.value || !monthEnd.value) return []
  const days: string[] = []
  let cursor = monthStart.value
  while (cursor < monthEnd.value) {
    days.push(cursor)
    cursor = addDaysText(cursor, 1)
  }
  return days
})

/* ---------- 日历事件与格子着色 ---------- */

function eventMeta(status: ClassroomCalendarEvent['status']) {
  if (status === 'pending') return { text: '待审核', type: 'warning' as const }
  if (status === 'approved') return { text: '已通过', type: 'success' as const }
  if (status === 'exam') return { text: '考试占用', type: 'danger' as const }
  return { text: '教学占用', type: 'info' as const }
}

function visibleEvents(): EventInput[] {
  return occupancy.value.map((item) => {
    const color = STATUS_COLOR[item.status]
    return {
      id: item.id,
      start: item.date,
      allDay: true,
      title: item.title,
      backgroundColor: color,
      borderColor: color,
      textColor: '#ffffff',
      classNames: [item.status === 'pending' ? 'room-pending-event' : 'room-busy-event'],
    }
  })
}

function cellKind(date: string): 'free' | 'pending' | 'busy' {
  const items = dayItemsMap.value.get(date)
  if (!items || items.length === 0) return 'free'
  return items.every((item) => item.status === 'pending') ? 'pending' : 'busy'
}

function paintCell(el: HTMLElement, date: string) {
  el.classList.remove('room-day-free', 'room-day-busy', 'room-day-pending')
  el.classList.add(`room-day-${cellKind(date)}`)
}

function onDayCellMount(arg: DayCellMountArg) {
  const date = toDateText(arg.date)
  dayCellEls.set(date, arg.el)
  paintCell(arg.el, date)
}

function repaintCells() {
  dayCellEls.forEach((el, date) => paintCell(el, date))
}

async function loadOccupancy() {
  if (!monthStart.value) return
  occupancyLoading.value = true
  try {
    const end = addDaysText(monthEnd.value, -1)
    occupancy.value = await getClassroomOccupancy({ start: monthStart.value, end })
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    occupancyLoading.value = false
    repaintCells()
    calendarRef.value?.getApi().refetchEvents()
  }
}

function onDatesSet() {
  const api = calendarRef.value?.getApi()
  if (!api) return
  monthStart.value = toDateText(api.view.currentStart)
  monthEnd.value = toDateText(api.view.currentEnd)
  loadOccupancy()
}

/** 点击日期/事件：只弹只读详情，绝不弹出申请表单 */
function onDateClick(arg: { date: Date }) {
  openDayDetail(toDateText(arg.date))
}

function onEventClick(arg: EventClickArg) {
  if (arg.event.start) openDayDetail(toDateText(arg.event.start))
}

/* ---------- 日期只读详情弹窗 ---------- */

const detailVisible = ref(false)
const detailDate = ref('')

const detailItems = computed(() => dayItemsMap.value.get(detailDate.value) ?? [])

function openDayDetail(date: string) {
  detailDate.value = date
  detailVisible.value = true
}

/* ==================== 下半区：待审批列表（管理端审批） ==================== */

const FILTER_OPTIONS: { label: string; value: ClassroomApprovalFilter }[] = [
  { label: '全部', value: '' },
  { label: '待审核', value: '待审核' },
  { label: '已通过', value: '已通过' },
  { label: '已驳回', value: '已驳回' },
]

const approvalFilter = ref<ClassroomApprovalFilter>('')
const approvals = ref<ClassroomApplyRecord[]>([])
const approvalLoading = ref(false)
const auditingId = ref(0)

async function loadApprovals() {
  approvalLoading.value = true
  try {
    approvals.value = await getClassroomApprovalList({
      status: approvalFilter.value || undefined,
    })
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    approvalLoading.value = false
  }
}

watch(approvalFilter, loadApprovals)

function approvalStatusMeta(status: ClassroomApplyRecord['status']) {
  if (status === '待审核') return { text: '待审核', type: 'warning' as const }
  if (status === '已通过') return { text: '已通过', type: 'success' as const }
  if (status === '已驳回') return { text: '已驳回', type: 'danger' as const }
  return { text: '已取消', type: 'info' as const }
}

// el-table 插槽的 row 是 Element Plus 的 DefaultRow 泛型，做一次收窄
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function statusOf(row: any) {
  return approvalStatusMeta(row.status)
}

/** 执行审批：通过 / 驳回（PUT /api/classroom/approve/{id} | reject/{id}） */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function handleAudit(row: any, action: 'approve' | 'reject') {
  const actionText = action === 'approve' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(
      `确定${actionText} ${row.applicant}（${row.className}）申请的“${row.roomName}”${row.date} ${row.timeSlot} 吗？`,
      `${actionText}教室申请`,
      { confirmButtonText: `确认${actionText}`, cancelButtonText: '再想想', type: action === 'approve' ? 'warning' : 'error' },
    )
  } catch {
    return // 用户取消
  }
  auditingId.value = row.id
  try {
    if (action === 'approve') {
      await approveClassroomApply(row.id)
      ElMessage.success(`已通过：${row.roomName} ${row.date} ${row.timeSlot}`)
    } else {
      await rejectClassroomApply(row.id)
      ElMessage.warning(`已驳回：${row.roomName} ${row.date} ${row.timeSlot}`)
    }
    // 审批后同时刷新：待审批列表 + 上方占用日历（已通过实时变红、已驳回恢复空闲）
    await Promise.all([loadApprovals(), loadOccupancy()])
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    auditingId.value = 0
  }
}

/* ==================== 日历配置 ==================== */

const calendarOptions: CalendarOptions = {
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: zhCnLocale,
  firstDay: 1,
  height: 'auto',
  dayMaxEvents: 3,
  nowIndicator: true,
  headerToolbar: { left: 'prev,next today', center: 'title', right: '' },
  buttonText: { today: '今天' },
  dayCellDidMount: onDayCellMount,
  datesSet: onDatesSet,
  dateClick: onDateClick,
  eventClick: onEventClick,
  events: (_info, success) => success(visibleEvents()),
  eventDisplay: 'block',
}

onMounted(() => {
  loadApprovals()
})
</script>

<template>
  <div class="classroom-approval-page">
    <!-- 上半区：教室占用总览 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span class="card-head__title">
            <el-icon><Tickets /></el-icon>
            教室占用总览（{{ monthStart.slice(0, 7) || '本月' }} · 可查看 {{ stats.freeCount }} 个空闲日 / 占用
            {{ stats.busyCount }} 天）
          </span>
          <span class="legend">
            <span class="legend-item"><i class="dot dot-red" />已占用/已通过</span>
            <span class="legend-item"><i class="dot dot-yellow" />待审核申请</span>
            <span class="legend-item"><i class="dot dot-green" />空闲可申请</span>
          </span>
        </div>
      </template>

      <div v-loading="occupancyLoading" class="calendar-wrap">
        <FullCalendar ref="calendarRef" :options="calendarOptions" />
      </div>

      <el-alert type="info" :closable="false" show-icon class="calendar-tip">
        <template #title>
          全局只读视图：点击任意日期可查看该日各教室的占用明细（使用人 / 用途 / 审批状态）。教室申请由学生/教师端发起，管理端仅审批，不提供申请入口。
        </template>
      </el-alert>
    </el-card>

    <!-- 下半区：待审批列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span class="card-head__title"><el-icon><Check /></el-icon> 教室使用申请审批</span>
          <div class="card-head__actions">
            <span class="filter-label">状态筛选：</span>
            <el-select v-model="approvalFilter" style="width: 130px" placeholder="全部">
              <el-option
                v-for="opt in FILTER_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <el-button :icon="RefreshLeft" :disabled="approvalLoading" @click="loadApprovals">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="approvalLoading" :data="approvals" stripe style="width: 100%">
        <el-table-column type="index" label="#" width="52" />
        <el-table-column prop="applicant" label="申请人姓名" width="110" />
        <el-table-column prop="className" label="所在班级" width="150" show-overflow-tooltip />
        <el-table-column prop="roomName" label="申请教室" min-width="140" show-overflow-tooltip />
        <el-table-column label="使用时间段" min-width="200">
          <template #default="{ row }">
            {{ row.date }}<br /><span class="cell-time">{{ row.timeSlot }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用途说明" min-width="240">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" type="info">{{ row.purpose }}</el-tag>
            <span class="cell-reason">{{ row.reason }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusOf(row).type" effect="dark" disable-transitions>
              {{ statusOf(row).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '待审核'">
              <el-button
                type="success"
                size="small"
                :loading="auditingId === row.id"
                @click="handleAudit(row, 'approve')"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                :loading="auditingId === row.id"
                @click="handleAudit(row, 'reject')"
              >
                驳回
              </el-button>
            </template>
            <span v-else class="cell-done">已处理</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="当前筛选下暂无教室申请" :image-size="80" />
        </template>
      </el-table>
    </el-card>

    <!-- 日期只读详情 -->
    <el-dialog
      v-model="detailVisible"
      :title="`${detailDate} 教室占用详情（只读）`"
      width="760px"
    >
      <el-empty v-if="detailItems.length === 0" description="该日期全校教室均空闲，可被师生端申请" :image-size="90" />
      <el-table v-else :data="detailItems" size="small" border max-height="380">
        <el-table-column prop="roomName" label="教室" width="140" />
        <el-table-column label="时段" width="170">
          <template #default="{ row }">
            {{ row.timeSlot || '全天' }}
          </template>
        </el-table-column>
        <el-table-column label="使用人" min-width="130">
          <template #default="{ row }">
            {{ row.applicant }}<span v-if="row.className" class="cell-class">（{{ row.className }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="用途说明" min-width="220">
          <template #default="{ row }">
            <template v-if="row.purpose">
              <el-tag size="small" type="info" effect="plain">{{ row.purpose }}</el-tag>
            </template>
            <span class="cell-reason">{{ row.reason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="eventMeta(row.status).type" effect="dark" disable-transitions>
              {{ eventMeta(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.classroom-approval-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.card-head__title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
}

.card-head__actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.legend {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.dot-red {
  background: #f56c6c;
}

.dot-yellow {
  background: #e6a23c;
}

.dot-green {
  background: #67c23a;
}

.calendar-wrap {
  overflow-x: auto;
}

.calendar-tip {
  margin-bottom: 2px;
}

.cell-time,
.cell-class {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.cell-reason {
  margin-left: 6px;
  color: var(--el-text-color-regular);
  font-size: 13px;
}

.cell-done {
  color: var(--el-text-color-placeholder);
  font-size: 13px;
}

/* ===== FullCalendar 单元格着色：绿=空闲 / 黄=有待审核 / 红=占用 ===== */

:deep(.fc-daygrid-day.room-day-free .fc-daygrid-day-frame) {
  background: #f0f9eb;
  cursor: pointer;
}

:deep(.fc-daygrid-day.room-day-busy .fc-daygrid-day-frame) {
  background: #fef0f0;
  cursor: pointer;
}

:deep(.fc-daygrid-day.room-day-pending .fc-daygrid-day-frame) {
  background: #fdf6ec;
  cursor: pointer;
}

:deep(.room-free-event .fc-event-main) {
  font-size: 12px;
  text-align: center;
}

:deep(.fc-daygrid-day-top) {
  position: relative;
  z-index: 1;
}
</style>
