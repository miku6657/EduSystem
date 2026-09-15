<script setup lang="ts">
/**
 * 师生端 · 教室申请（学生 / 教师共用）
 * 数据源：GET  /api/classroom-applies/my（服务端按当前登录人过滤）
 *         GET  /api/classrooms（选教室）
 * 提交：  POST /api/classroom-applies（同教室+同日期+同时段冲突时后端拒绝）
 * 撤回：  PUT  /api/classroom-applies/{id}/cancel（仅本人、仅"待审核"）
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { showConfirmDialog, showToast, showSuccessToast } from 'vant'
import {
  cancelClassroomApply,
  listMyClassroomApplies,
  pageClassrooms,
  submitClassroomApply,
} from '@/api/classroom'
import type { Classroom, ClassroomApply } from '@/api/classroom'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import StatBar from '@/components/StatBar.vue'
import { CLASSROOM_APPLY_STATUS_TYPE, TIME_SLOT_OPTIONS } from '@/constants/dict'
import { formatDate, formatDateTime, todayStr } from '@/utils/format'

const userStore = useUserStore()
const refreshing = ref(false)

/** 我的申请 */
const {
  data: applies,
  loading,
  error,
  reload,
} = useAsyncData<ClassroomApply[]>(
  () => (userStore.token ? listMyClassroomApplies() : Promise.resolve([])),
  [],
)

/** 概览 */
const summary = computed(() => {
  const rows = applies.value
  return {
    total: rows.length,
    waiting: rows.filter((row) => row.status === '待审核').length,
    passed: rows.filter((row) => row.status === '已通过').length,
  }
})

/** 统计条数据（对齐公共组件 StatBar） */
const summaryItems = computed(() => [
  { label: '申请总数', value: summary.value.total },
  { label: '待审核', value: summary.value.waiting },
  { label: '已通过', value: summary.value.passed },
])

/* ------------------------------ 提交表单 ------------------------------ */
const showForm = ref(false)
const showRoomPicker = ref(false)
const showDatePicker = ref(false)
const submitting = ref(false)
const rooms = ref<Classroom[]>([])
const roomsLoading = ref(false)

const form = reactive<{
  roomId: number
  className: string
  applyDate: string
  timeSlot: string
  purpose: string
  reason: string
}>({
  roomId: 0,
  className: '',
  applyDate: todayStr(),
  timeSlot: '第3-4节',
  purpose: '',
  reason: '',
})

/** van-date-picker 的值形如 ['2026','09','13'] */
const dateValues = computed({
  get: () => form.applyDate.split('-'),
  set: (value: string[]) => {
    form.applyDate = value.join('-')
  },
})
const minDate = new Date()
const maxDate = (() => {
  const date = new Date()
  date.setMonth(date.getMonth() + 3)
  return date
})()

const roomText = computed(() => {
  const room = rooms.value.find((item) => item.id === form.roomId)
  return room ? `${room.roomNo}（${room.type ?? '教室'}·${room.capacity ?? '—'} 座）` : ''
})
const roomColumns = computed(() =>
  rooms.value.map((item) => ({
    text: `${item.roomNo}（${item.type ?? '教室'}·${item.capacity ?? '—'} 座）`,
    value: item.id,
  })),
)

interface PickerPayload {
  selectedOptions?: Array<{ value?: string | number } | undefined>
}

function onRoomConfirm(payload: PickerPayload) {
  const value = payload.selectedOptions?.[0]?.value
  if (typeof value === 'number') {
    form.roomId = value
  }
  showRoomPicker.value = false
}

/** 首次打开弹层时拉教室列表（一次 50 条够选） */
async function loadRooms() {
  if (rooms.value.length > 0) {
    return
  }
  roomsLoading.value = true
  try {
    const page = await pageClassrooms({ page: 1, pageSize: 50 })
    rooms.value = page.list
  } catch {
    // 失败提示已由请求层 toast
  } finally {
    roomsLoading.value = false
  }
}

function openForm() {
  form.roomId = 0
  form.className = ''
  form.applyDate = todayStr()
  form.timeSlot = '第3-4节'
  form.purpose = ''
  form.reason = ''
  showForm.value = true
  void loadRooms()
}

async function onSubmit() {
  if (!form.roomId) {
    showToast('请选择教室')
    return
  }
  if (!form.applyDate) {
    showToast('请选择使用日期')
    return
  }
  if (!form.timeSlot) {
    showToast('请选择使用时段')
    return
  }
  if (!form.reason.trim()) {
    showToast('请填写申请理由')
    return
  }
  submitting.value = true
  try {
    await submitClassroomApply({
      roomId: form.roomId,
      className: form.className || undefined,
      applyDate: form.applyDate,
      timeSlot: form.timeSlot,
      purpose: form.purpose || undefined,
      reason: form.reason.trim(),
    })
    showSuccessToast('申请已提交，等待审批')
    showForm.value = false
    await reload()
  } catch {
    // 业务失败（如"该时间段教室已被占用"）由请求层统一 toast
  } finally {
    submitting.value = false
  }
}

/* ------------------------------ 撤回 ------------------------------ */
const cancellingId = ref(0)

function canCancel(row: ClassroomApply): boolean {
  return row.status === '待审核'
}

async function onCancel(row: ClassroomApply) {
  try {
    await showConfirmDialog({
      title: '撤回申请',
      message: `确定撤回「${row.roomName ?? `教室#${row.roomId}`} ${row.applyDate} ${row.timeSlot}」的申请吗？`,
    })
  } catch {
    return
  }
  cancellingId.value = row.id ?? 0
  try {
    await cancelClassroomApply(row.id as number)
    showToast('已撤回申请')
    await reload()
  } catch {
    // 请求层已 toast
  } finally {
    cancellingId.value = 0
  }
}

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
    <!-- 顶部：标题 + 主操作 + 概览（对齐 admin 的卡片头结构） -->
    <div class="st-card">
      <PageHeader title="教室申请">
        <template #actions>
          <van-button size="small" type="primary" @click="openForm">提交申请</van-button>
        </template>
      </PageHeader>
      <StatBar :items="summaryItems" />
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="loading && !refreshing" class="st-empty">
        <van-loading vertical>加载中…</van-loading>
      </div>

      <van-empty v-else-if="error" image="error" :description="error">
        <van-button round type="primary" size="small" @click="reload">重新加载</van-button>
      </van-empty>

      <van-empty v-else-if="applies.length === 0" description="暂无教室申请记录" />

      <template v-else>
        <div class="st-section-title">我的申请</div>
        <div
          v-for="row in applies"
          :key="row.id ?? `${row.roomId}-${row.applyDate}-${row.timeSlot}`"
          class="st-card"
        >
          <div class="st-row">
            <div class="apply__room">{{ row.roomName || `教室#${row.roomId}` }}</div>
            <van-tag :type="CLASSROOM_APPLY_STATUS_TYPE[row.status ?? ''] || 'primary'" plain>
              {{ row.status || '待审核' }}
            </van-tag>
          </div>
          <div class="apply__meta st-muted">
            {{ formatDate(row.applyDate) }} · {{ row.timeSlot }}
            <template v-if="row.className"> · {{ row.className }}</template>
          </div>
          <div v-if="row.purpose || row.reason" class="apply__reason">
            <span v-if="row.purpose" class="apply__purpose">{{ row.purpose }}</span>
            <span v-if="row.reason">{{ row.reason }}</span>
          </div>
          <div class="st-row apply__footer">
            <span class="st-muted">提交于 {{ formatDateTime(row.createTime) }}</span>
            <van-button
              v-if="canCancel(row)"
              size="small"
              plain
              type="danger"
              :loading="cancellingId === row.id"
              @click="onCancel(row)"
            >
              撤回
            </van-button>
          </div>
        </div>
      </template>
    </van-pull-refresh>

    <!-- 提交弹层 -->
    <van-popup v-model:show="showForm" position="bottom" round>
      <van-form @submit="onSubmit">
        <div class="apply__popup-title">提交教室申请</div>

        <van-cell-group inset>
          <van-field
            :model-value="roomText"
            label="教室"
            placeholder="请选择教室"
            readonly
            is-link
            @click="showRoomPicker = true"
          />
          <van-field
            :model-value="form.applyDate"
            label="使用日期"
            readonly
            is-link
            @click="showDatePicker = true"
          />
          <van-field name="timeSlot" label="使用时段">
            <template #input>
              <van-radio-group v-model="form.timeSlot" direction="horizontal">
                <van-radio v-for="slot in TIME_SLOT_OPTIONS" :key="slot" :name="slot">
                  {{ slot }}
                </van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field v-model="form.className" label="班级" placeholder="选填，如 软件技术2301班" />
          <van-field v-model="form.purpose" label="用途" placeholder="选填，如 课程实训" />
          <van-field
            v-model="form.reason"
            label="申请理由"
            type="textarea"
            rows="2"
            autosize
            placeholder="必填"
          />
        </van-cell-group>

        <div class="apply__popup-actions">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            :loading="submitting"
            :disabled="roomsLoading"
          >
            提交申请
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- 教室选择 -->
    <van-popup v-model:show="showRoomPicker" position="bottom" round>
      <div v-if="roomsLoading" class="st-empty">
        <van-loading vertical>教室加载中…</van-loading>
      </div>
      <van-empty v-else-if="roomColumns.length === 0" description="暂无可选教室" />
      <van-picker
        v-else
        :columns="roomColumns"
        @confirm="onRoomConfirm"
        @cancel="showRoomPicker = false"
      />
    </van-popup>

    <!-- 日期选择 -->
    <van-popup v-model:show="showDatePicker" position="bottom" round>
      <van-date-picker
        v-model="dateValues"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="showDatePicker = false"
        @cancel="showDatePicker = false"
      />
    </van-popup>

    <div class="st-card apply__tip st-muted">
      说明：同一教室、同一日期、同一时段已被占用（待审核或已通过）时无法重复申请；提交后由教务处审批，审批前可在上方撤回，审批结果会显示在状态标签上。
    </div>
  </div>
</template>

<style scoped>
.apply__summary {
  display: flex;
  text-align: center;
}

.apply__summary-item {
  flex: 1;
}

.apply__summary-value {
  font-size: 20px;
  font-weight: 600;
}

.apply__action {
  margin-bottom: 12px;
}

.apply__room {
  font-size: 15px;
  font-weight: 600;
}

.apply__meta {
  margin-top: 6px;
}

.apply__reason {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.5;
}

.apply__purpose {
  color: var(--st-primary);
}

.apply__footer {
  margin-top: 10px;
}

.apply__popup-title {
  padding: 14px 16px 6px;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
}

.apply__popup-actions {
  padding: 12px 16px 20px;
}

.apply__tip {
  margin-top: 16px;
  line-height: 1.6;
}
</style>
