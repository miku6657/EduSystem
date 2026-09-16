<script setup lang="ts">
/**
 * 教师 · 我的调课
 * 数据源：GET  /api/course-adjusts/my（服务端按当前登录教师过滤）
 * 提交：  POST /api/course-adjusts（teacherId 由服务端按登录人写入）
 * 撤销：  PUT  /api/course-adjusts/{id}/cancel（仅本人、仅"待审核"）
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { showConfirmDialog, showSuccessToast, showToast } from 'vant'
import { cancelCourseAdjust, listMyCourseAdjusts, submitCourseAdjust } from '@/api/courseAdjust'
import type { CourseAdjust } from '@/api/courseAdjust'
import { listMyClasses, listMyCourses } from '@/api/base'
import type { ClassInfo, Course } from '@/api/base'
import { pageClassrooms } from '@/api/classroom'
import type { Classroom } from '@/api/classroom'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import StatBar from '@/components/StatBar.vue'
import {
  AUDIT_STATUS_TEXT,
  AUDIT_STATUS_TYPE,
  CLASSROOM_APPLY_STATUS_TYPE,
  TIME_SLOT_OPTIONS,
} from '@/constants/dict'
import { formatDate, todayStr } from '@/utils/format'

const userStore = useUserStore()
const teacherId = computed(() => userStore.businessId)
const refreshing = ref(false)

const {
  data: adjusts,
  loading,
  error,
  reload,
} = useAsyncData<CourseAdjust[]>(
  () => (teacherId.value ? listMyCourseAdjusts() : Promise.resolve([])),
  [],
)

const summary = computed(() => {
  const rows = adjusts.value
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

/** 状态标签颜色：复用在审状态字典，补齐"已撤销" */
function statusType(status?: string) {
  if (status === '已撤销') {
    return 'primary' as const
  }
  return AUDIT_STATUS_TYPE[status ?? ''] ?? CLASSROOM_APPLY_STATUS_TYPE[status ?? ''] ?? 'primary'
}

function statusText(status?: string) {
  return AUDIT_STATUS_TEXT[status ?? ''] ?? status ?? '待审核'
}

/* ------------------------------ 提交表单 ------------------------------ */
const showForm = ref(false)
const showCoursePicker = ref(false)
const showClassPicker = ref(false)
const showRoomPicker = ref(false)
const showOriginDate = ref(false)
const showTargetDate = ref(false)
const submitting = ref(false)

const courses = ref<Course[]>([])
const classes = ref<ClassInfo[]>([])
const rooms = ref<Classroom[]>([])
const optionsLoading = ref(false)

const form = reactive({
  courseId: 0,
  classId: 0,
  originDate: todayStr(),
  originSlot: '第1-2节',
  targetDate: todayStr(),
  targetSlot: '第3-4节',
  classroomId: 0,
  reason: '',
})

const courseText = computed(
  () => courses.value.find((item) => item.id === form.courseId)?.name ?? '',
)
const classText = computed(() => classes.value.find((item) => item.id === form.classId)?.name ?? '')
const roomText = computed(() => {
  const room = rooms.value.find((item) => item.id === form.classroomId)
  return room ? room.roomNo : ''
})

const courseColumns = computed(() =>
  courses.value.map((item) => ({ text: item.name, value: item.id as number })),
)
const classColumns = computed(() =>
  classes.value.map((item) => ({ text: item.name, value: item.id as number })),
)
const roomColumns = computed(() =>
  rooms.value.map((item) => ({
    text: `${item.roomNo}（${item.capacity ?? '—'} 座）`,
    value: item.id as number,
  })),
)

const originDateValues = computed({
  get: () => form.originDate.split('-'),
  set: (value: string[]) => {
    form.originDate = value.join('-')
  },
})
const targetDateValues = computed({
  get: () => form.targetDate.split('-'),
  set: (value: string[]) => {
    form.targetDate = value.join('-')
  },
})
const minDate = new Date()
const maxDate = (() => {
  const date = new Date()
  date.setMonth(date.getMonth() + 3)
  return date
})()

interface PickerPayload {
  selectedOptions?: Array<{ value?: string | number } | undefined>
}

function pickId(payload: PickerPayload): number {
  const value = payload.selectedOptions?.[0]?.value
  return typeof value === 'number' ? value : 0
}

async function loadOptions() {
  if (optionsLoading.value || courses.value.length > 0) {
    return
  }
  if (!teacherId.value) {
    return
  }
  optionsLoading.value = true
  try {
    const [courseList, classList, roomPage] = await Promise.all([
      listMyCourses(teacherId.value),
      listMyClasses(teacherId.value),
      pageClassrooms({ page: 1, pageSize: 50 }),
    ])
    courses.value = courseList
    classes.value = classList
    rooms.value = roomPage.list
  } catch {
    // 请求层已 toast
  } finally {
    optionsLoading.value = false
  }
}

function openForm() {
  if (!teacherId.value) {
    showToast('未解析到教师工号，请确认登录账号为工号')
    return
  }
  form.courseId = 0
  form.classId = 0
  form.originDate = todayStr()
  form.originSlot = '第1-2节'
  form.targetDate = todayStr()
  form.targetSlot = '第3-4节'
  form.classroomId = 0
  form.reason = ''
  showForm.value = true
  void loadOptions()
}

async function onSubmit() {
  if (!form.courseId) {
    showToast('请选择调课课程')
    return
  }
  if (!form.reason.trim()) {
    showToast('请填写调课原因')
    return
  }
  if (form.originDate === form.targetDate && form.originSlot === form.targetSlot) {
    showToast('调整后的时间不能与原时间相同')
    return
  }
  submitting.value = true
  try {
    await submitCourseAdjust({
      courseId: form.courseId,
      classId: form.classId || undefined,
      originDate: form.originDate,
      originSlot: form.originSlot,
      targetDate: form.targetDate,
      targetSlot: form.targetSlot,
      classroomId: form.classroomId || undefined,
      reason: form.reason.trim(),
    })
    showSuccessToast('调课申请已提交')
    showForm.value = false
    await reload()
  } catch {
    // 请求层已 toast
  } finally {
    submitting.value = false
  }
}

/* ------------------------------ 撤销 ------------------------------ */
const cancellingId = ref(0)

async function onCancel(row: CourseAdjust) {
  try {
    await showConfirmDialog({
      title: '撤销调课申请',
      message: `确定撤销「${row.courseName ?? `课程#${row.courseId}`}」的调课申请吗？`,
    })
  } catch {
    return
  }
  cancellingId.value = row.id ?? 0
  try {
    await cancelCourseAdjust(row.id as number)
    showToast('已撤销申请')
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
    <van-empty v-if="!teacherId" description="未解析到教师工号">
      <van-button round type="primary" size="small" @click="userStore.resolveProfile(true)">
        重新解析身份
      </van-button>
    </van-empty>

    <template v-else>
      <!-- 顶部：标题 + 主操作 + 概览（对齐 admin 的卡片头结构） -->
      <div class="st-card">
        <PageHeader title="调课申请">
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

        <van-empty v-else-if="adjusts.length === 0" description="暂无调课申请" />

        <template v-else>
          <div
            v-for="row in adjusts"
            :key="row.id ?? `${row.courseId}-${row.originDate}`"
            class="st-card"
          >
            <div class="st-row">
              <div class="adjust__course">{{ row.courseName || `课程#${row.courseId}` }}</div>
              <van-tag :type="statusType(row.status)" plain>{{ statusText(row.status) }}</van-tag>
            </div>
            <div class="adjust__change">
              <div class="adjust__line">
                <span class="st-muted">原</span>
                {{ formatDate(row.originDate) }} {{ row.originSlot }}
              </div>
              <div class="adjust__line adjust__line--target">
                <span class="st-muted">改</span>
                {{ formatDate(row.targetDate) }} {{ row.targetSlot }}
              </div>
            </div>
            <div class="adjust__meta st-muted">
              <template v-if="row.className">{{ row.className }}</template>
              <template v-if="row.roomName"> · 教室 {{ row.roomName }}</template>
            </div>
            <div v-if="row.reason" class="adjust__reason">{{ row.reason }}</div>
            <div v-if="row.approveRemark" class="adjust__remark st-muted">
              审批意见：{{ row.approveRemark }}
            </div>
            <div class="st-row adjust__footer">
              <span class="st-muted">提交于 {{ formatDate(row.createTime) }}</span>
              <van-button
                v-if="row.status === '待审核'"
                size="small"
                plain
                type="danger"
                :loading="cancellingId === row.id"
                @click="onCancel(row)"
              >
                撤销
              </van-button>
            </div>
          </div>
        </template>
      </van-pull-refresh>
    </template>

    <!-- 提交弹层 -->
    <van-popup v-model:show="showForm" position="bottom" round>
      <van-form @submit="onSubmit">
        <div class="adjust__popup-title">提交调课申请</div>

        <div v-if="optionsLoading" class="st-empty">
          <van-loading vertical>加载课程/班级…</van-loading>
        </div>

        <van-cell-group v-else inset>
          <van-field
            :model-value="courseText"
            label="课程"
            placeholder="请选择任教课程"
            readonly
            is-link
            @click="showCoursePicker = true"
          />
          <van-field
            :model-value="classText"
            label="班级"
            placeholder="选填"
            readonly
            is-link
            @click="showClassPicker = true"
          />
          <van-field
            :model-value="form.originDate"
            label="原上课日期"
            readonly
            is-link
            @click="showOriginDate = true"
          />
          <van-field name="originSlot" label="原时段">
            <template #input>
              <van-radio-group v-model="form.originSlot" direction="horizontal">
                <van-radio v-for="slot in TIME_SLOT_OPTIONS" :key="`o-${slot}`" :name="slot">
                  {{ slot }}
                </van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            :model-value="form.targetDate"
            label="调整后日期"
            readonly
            is-link
            @click="showTargetDate = true"
          />
          <van-field name="targetSlot" label="调整后时段">
            <template #input>
              <van-radio-group v-model="form.targetSlot" direction="horizontal">
                <van-radio v-for="slot in TIME_SLOT_OPTIONS" :key="`t-${slot}`" :name="slot">
                  {{ slot }}
                </van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            :model-value="roomText"
            label="调整后教室"
            placeholder="选填，不填表示教室不变"
            readonly
            is-link
            @click="showRoomPicker = true"
          />
          <van-field
            v-model="form.reason"
            label="调课原因"
            type="textarea"
            rows="2"
            autosize
            placeholder="必填"
          />
        </van-cell-group>

        <div class="adjust__popup-actions">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            提交申请
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <van-popup v-model:show="showCoursePicker" position="bottom" round>
      <van-empty v-if="courseColumns.length === 0" description="暂无任教课程" />
      <van-picker
        v-else
        :columns="courseColumns"
        @confirm="
          (payload: PickerPayload) => {
            form.courseId = pickId(payload)
            showCoursePicker = false
          }
        "
        @cancel="showCoursePicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showClassPicker" position="bottom" round>
      <van-empty v-if="classColumns.length === 0" description="暂无任教班级" />
      <van-picker
        v-else
        :columns="classColumns"
        @confirm="
          (payload: PickerPayload) => {
            form.classId = pickId(payload)
            showClassPicker = false
          }
        "
        @cancel="showClassPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showRoomPicker" position="bottom" round>
      <van-empty v-if="roomColumns.length === 0" description="暂无可选教室" />
      <van-picker
        v-else
        :columns="roomColumns"
        @confirm="
          (payload: PickerPayload) => {
            form.classroomId = pickId(payload)
            showRoomPicker = false
          }
        "
        @cancel="showRoomPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showOriginDate" position="bottom" round>
      <van-date-picker
        v-model="originDateValues"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="showOriginDate = false"
        @cancel="showOriginDate = false"
      />
    </van-popup>

    <van-popup v-model:show="showTargetDate" position="bottom" round>
      <van-date-picker
        v-model="targetDateValues"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="showTargetDate = false"
        @cancel="showTargetDate = false"
      />
    </van-popup>

    <div class="st-card adjust__tip st-muted">
      说明：提交后由教务处审批，审批前可撤销；审批结果与意见会显示在列表上。调课后请及时通知学生与相关教室管理员。
    </div>
  </div>
</template>

<style scoped>
.adjust__summary {
  display: flex;
  text-align: center;
}

.adjust__summary-item {
  flex: 1;
}

.adjust__summary-value {
  font-size: 20px;
  font-weight: 600;
}

.adjust__action {
  margin-bottom: 12px;
}

.adjust__course {
  font-size: 15px;
  font-weight: 600;
}

.adjust__change {
  margin-top: 8px;
}

.adjust__line {
  font-size: 14px;
  line-height: 1.7;
}

.adjust__line--target {
  color: var(--st-primary);
}

.adjust__meta {
  margin-top: 6px;
}

.adjust__reason {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.5;
}

.adjust__remark {
  margin-top: 6px;
}

.adjust__footer {
  margin-top: 10px;
}

.adjust__popup-title {
  padding: 14px 16px 6px;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
}

.adjust__popup-actions {
  padding: 12px 16px 20px;
}

.adjust__tip {
  margin-top: 16px;
  line-height: 1.6;
}
</style>
