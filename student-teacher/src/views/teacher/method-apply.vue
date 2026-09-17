<script setup lang="ts">
/**
 * 教师 · 考核方式申报
 * 列表：GET  /api/exam-apply/my/list?teacherId（后端缺口接口，现由 Mock 提供）
 * 提交：POST /api/exam-apply/apply（同课程已有待审核申报时会失败）
 * 审核在后台管理端完成，本页只负责提交与查看状态。
 *
 * 结构：顶部卡片头（PageHeader + 主操作）+ PageState 三态。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import {
  listExamCourses,
  listMyExamApplies,
  submitExamApply,
} from '@/api/examApply'

import type {
  ExamApply,
  ExamCourse,
} from '@/api/examApply'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import PageState from '@/components/PageState.vue'
import StatBar from '@/components/StatBar.vue'
import { AUDIT_STATUS_TEXT, AUDIT_STATUS_TYPE, EXAM_METHOD_OPTIONS } from '@/constants/dict'
import { formatDateTime } from '@/utils/format'

const userStore = useUserStore()
/** 教师ID，即后端 base_teacher.id */
const teacherId = computed(() => userStore.businessId)

const {
  data: applies,
  loading,
  error,
  reload,
} = useAsyncData<ExamApply[]>(
  () => (teacherId.value ? listMyExamApplies(teacherId.value) : Promise.resolve([])),
  [],
)
const {
  data: courses,
  reload: reloadCourses,
} =
  useAsyncData<
    ExamCourse[]
  >(
    () =>
      listExamCourses(),
    [],
  )

/* ------------------------------ 新增申报 ------------------------------ */
const showForm = ref(false)
const showCoursePicker = ref(false)
const submitting = ref(false)
const form = reactive({
  courseId: '',
  applyType: EXAM_METHOD_OPTIONS[0] as string,
  reason: '',
})

const courseText = computed(
  () =>
    courses.value.find(
      (item) =>
        String(item.id)
        === form.courseId,
    )?.name ?? '',
)
const courseColumns = computed(() =>
  courses.value.map(
    (item) => ({
      text: `${item.name}（${item.courseCode}）`,
      value: String(item.id),
    }),
  ),
)

function openForm() {
  if (courses.value.length === 0) {
    showToast('系统暂无课程数据')
    return
  }

  form.courseId = String(courses.value[0]?.id ?? '')
  form.applyType = EXAM_METHOD_OPTIONS[0]
  form.reason = ''
  showForm.value = true
}

function openCoursePicker() {
  if (courses.value.length === 0) {
    showToast('系统暂无课程数据')
    return
  }

  showCoursePicker.value = true
}

function onCourseConfirm(payload: {
  selectedOptions?: Array<{
    value?: string | number
  }>
}) {
  const value =
    payload.selectedOptions?.[0]?.value

  form.courseId =
    value === null
    || value === undefined
      ? ''
      : String(value)

  showCoursePicker.value = false
}

async function onSubmit() {
  if (!teacherId.value) {
    showToast('当前账号未绑定教师档案')
    return
  }
  if (!form.courseId) {
    showToast('请选择申报课程')
    return
  }
  if (!form.reason.trim()) {
    showToast('请填写申请理由')
    return
  }
  submitting.value = true
  try {
    await submitExamApply({
      courseId: form.courseId,

      teacherId: teacherId.value,

      applyType: form.applyType,

      reason: form.reason.trim(),
    })
    showToast('申报已提交')
    showForm.value = false
    await reload()
  } catch {
    // request.ts统一处理
  } finally {
    submitting.value = false
  }
}

/** 工号未解析时重试解析业务身份 */
async function retryProfile() {
  await userStore.resolveProfile(true)
  reload()
  reloadCourses()
}

/** 统计条数据（对齐公共组件 StatBar）：全部由本人申报列表派生，不新增接口 */
const summaryItems = computed(() => [
  { label: '申报总数', value: applies.value.length },
  { label: '待审核', value: applies.value.filter((item) => item.status === 'WAIT').length },
  { label: '已通过', value: applies.value.filter((item) => item.status === 'PASS').length },
])

onMounted(() => {
  reload()
  reloadCourses()
})
</script>

<template>
  <div>
    <van-empty v-if="!teacherId" description="当前账号未绑定教师档案">
      <van-button round type="primary" size="small" @click="retryProfile">重新解析身份</van-button>
    </van-empty>

    <template v-else>
      <!-- 顶部：标题 + 主操作（对齐 admin 的卡片头结构） -->
      <div class="st-card">
        <PageHeader title="考核方式申报">
          <template #actions>
            <van-button size="small" type="primary" icon="plus" @click="openForm">
              新增申报
            </van-button>
          </template>
        </PageHeader>
        <StatBar :items="summaryItems" />
      </div>

      <PageState
        :loading="loading"
        :error="error"
        :empty="applies.length === 0"
        empty-text="还没有考核方式申报记录"
        @retry="reload"
      >
        <div
          v-for="item in applies"
          :key="item.id ?? `${item.courseId}-${item.createTime}`"
          class="st-card"
        >
          <div class="st-row">
            <span class="apply__course">{{ item.courseName || `课程#${item.courseId}` }}</span>
            <van-tag :type="AUDIT_STATUS_TYPE[item.status ?? ''] || 'primary'">
              {{ AUDIT_STATUS_TEXT[item.status ?? ''] || '待审核' }}
            </van-tag>
          </div>
          <div class="st-row apply__type">
            <span class="st-muted">考核方式</span>
            <van-tag plain type="primary">{{ item.applyType || '—' }}</van-tag>
          </div>
          <div v-if="item.reason" class="apply__reason">{{ item.reason }}</div>
          <div class="st-muted apply__time">提交时间：{{ formatDateTime(item.createTime) }}</div>
        </div>
      </PageState>

      <div
        class="
          st-card
          st-muted
          apply__note
        "
      >
        申报提交后进入待审核状态，
        由后台管理员进行审核。
        审核结果会同步显示在本页。
      </div>
    </template>

    <!-- 新增申报 -->
    <van-popup v-model:show="showForm" position="bottom" round :style="{ height: '76%' }">
      <div class="apply__form-title">新增考核方式申报</div>
      <van-form class="apply__form" @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            readonly
            is-link
            name="course"
            label="课程"
            placeholder="请选择课程"
            :model-value="courseText"
            :rules="[{ required: true, message: '请选择课程' }]"
            @click="openCoursePicker"
          />
          <van-field
            name="applyType"
            label="考核方式"
            :model-value="form.applyType"
            :rules="[{ required: true, message: '请选择考核方式' }]"
          >
            <template #input>
              <van-radio-group v-model="form.applyType" direction="horizontal">
                <van-radio
                  v-for="method in EXAM_METHOD_OPTIONS"
                  :key="method"
                  :name="method"
                  shape="dot"
                >
                  {{ method }}
                </van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            v-model="form.reason"
            name="reason"
            label="申请理由"
            type="textarea"
            rows="3"
            autosize
            maxlength="300"
            placeholder="请说明本课程采用该考核方式的理由"
            :rules="[{ required: true, message: '请填写申请理由' }]"
          />
        </van-cell-group>
        <div class="apply__submit">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            提交申报
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <van-popup v-model:show="showCoursePicker" position="bottom" round>
      <van-picker
        title="选择课程"
        :columns="courseColumns"
        :model-value="[form.courseId]"
        @confirm="onCourseConfirm"
        @cancel="showCoursePicker = false"
      />
    </van-popup>
  </div>
</template>

<style scoped>
.apply__course {
  font-size: 15px;
  font-weight: 600;
}
.apply__type {
  justify-content: flex-start;
  gap: 8px;
  margin-top: 8px;
}
.apply__reason {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
}
.apply__time {
  margin-top: 8px;
}
.apply__note {
  line-height: 1.6;
}
.apply__form-title {
  padding: 14px 0;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
}
.apply__submit {
  margin: 16px;
}
.apply__tip {
  margin: 0 16px 16px;
  text-align: center;
}
</style>
