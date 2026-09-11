<script setup lang="ts">
/**
 * 学生 · 补考重修
 * 数据源：GET /api/retake/list-by-student/{studentId}
 * 申请：POST /api/retake/apply?studentId&courseId（后端目前只写入"重修"类型）
 */
import { computed, onMounted, ref } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { applyRetake, listMyRetakes } from '@/api/retake'
import type { ExamRetake } from '@/api/retake'
import { pageCourses } from '@/api/base'
import type { Course } from '@/api/base'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'

const userStore = useUserStore()
const refreshing = ref(false)
/** 申请弹层状态 */
const showApply = ref(false)
const submitting = ref(false)
const selectedCourseId = ref<number | undefined>(undefined)
const courseOptions = ref<Course[]>([])
const coursesLoading = ref(false)

const { data: retakes, loading, error, reload } = useAsyncData<ExamRetake[]>(
  () => (userStore.businessId ? listMyRetakes(userStore.businessId) : Promise.resolve([])),
  [],
)

/** 概览：重修 / 补考 / 待安排（examId 为空表示教务还没安排场次） */
const summaryItems = computed(() => {
  const rows = retakes.value
  const pending = (row: ExamRetake) => row.examId === null || row.examId === undefined
  return [
    { label: '重修', value: rows.filter((row) => row.type === '重修').length },
    { label: '补考', value: rows.filter((row) => row.type === '补考').length },
    { label: '待安排', value: rows.filter(pending).length },
  ]
})

/** 类型标签：补考 warning / 重修 primary */
function typeTagOf(type: string): 'warning' | 'primary' {
  return type === '补考' ? 'warning' : 'primary'
}

/** 场次文案：已安排显示场次编号，否则等待教务安排 */
function examTextOf(row: ExamRetake): string {
  return row.examId === null || row.examId === undefined ? '待教务安排' : `已安排场次 #${row.examId}`
}

/** 课程选项：首次打开弹层时拉取（一次 50 条足够选课用） */
async function loadCourses() {
  if (courseOptions.value.length > 0) {
    return
  }
  coursesLoading.value = true
  try {
    const page = await pageCourses({ page: 1, pageSize: 50 })
    courseOptions.value = page.list
  } catch {
    // 失败提示已由请求层统一 toast，这里保持空态
  } finally {
    coursesLoading.value = false
  }
}

function openApply() {
  if (!userStore.businessId) {
    showToast('未解析到学号，请确认登录账号为学号')
    return
  }
  selectedCourseId.value = undefined
  showApply.value = true
  void loadCourses()
}

/** 提交申请：业务失败（如同课程重复申请）由请求层统一 toast，此处不重复弹错 */
async function onSubmit() {
  const studentId = userStore.businessId
  const courseId = selectedCourseId.value
  if (!studentId) {
    showToast('未解析到学号，请确认登录账号为学号')
    return
  }
  if (!courseId) {
    showToast('请选择要申请的课程')
    return
  }
  const courseName = courseOptions.value.find((item) => item.id === courseId)?.name ?? `课程#${courseId}`
  try {
    await showConfirmDialog({
      title: '确认申请',
      message: `确定申请「${courseName}」的补考 / 重修吗？提交后由教务安排考试场次。`,
    })
  } catch {
    return // 用户取消
  }
  submitting.value = true
  try {
    await applyRetake(studentId, courseId)
    showToast('申请已提交')
    showApply.value = false
    await reload()
  } catch {
    // 请求层已 toast，不重复提示
  } finally {
    submitting.value = false
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
    <div class="st-card retake__summary">
      <div v-for="item in summaryItems" :key="item.label" class="retake__summary-item">
        <div class="retake__summary-value">{{ item.value }}</div>
        <div class="st-muted">{{ item.label }}</div>
      </div>
    </div>

    <van-button class="retake__actions" round block type="primary" @click="openApply">
      申请重修 / 补考
    </van-button>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-empty v-if="!userStore.businessId" description="未解析到学号，请确认登录账号为学号" />

      <div v-else-if="loading && !refreshing" class="st-empty">
        <van-loading vertical>加载中…</van-loading>
      </div>

      <van-empty v-else-if="error" image="error" :description="error">
        <van-button round type="primary" size="small" @click="reload">重新加载</van-button>
      </van-empty>

      <van-empty v-else-if="retakes.length === 0" description="暂无补考重修记录" />

      <template v-else>
        <div v-for="row in retakes" :key="row.id ?? `${row.type}-${row.courseId}`" class="st-card">
          <div class="st-row">
            <div class="retake__course">{{ row.courseName || `课程#${row.courseId}` }}</div>
            <van-tag :type="typeTagOf(row.type)" plain>{{ row.type || '—' }}</van-tag>
          </div>
          <div class="retake__meta st-muted">{{ examTextOf(row) }}</div>
        </div>
      </template>
    </van-pull-refresh>

    <!-- 申请弹层：van-form + 课程单选 -->
    <van-popup v-model:show="showApply" position="bottom" round>
      <van-form @submit="onSubmit">
        <div class="retake__popup-title">申请重修 / 补考</div>

        <div v-if="coursesLoading" class="st-empty">
          <van-loading vertical>课程加载中…</van-loading>
        </div>
        <van-empty v-else-if="courseOptions.length === 0" description="暂无可申请课程" />
        <van-radio-group v-else v-model="selectedCourseId" class="retake__courses">
          <van-radio v-for="course in courseOptions" :key="course.id" :name="course.id" class="retake__course-item">
            {{ course.name }}
            <span v-if="course.credit" class="st-muted">（{{ course.credit }} 学分）</span>
          </van-radio>
        </van-radio-group>

        <div class="retake__popup-actions">
          <van-button round block type="primary" native-type="submit" :loading="submitting" :disabled="courseOptions.length === 0">
            提交申请
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- 静态说明 -->
    <div class="st-card retake__tip st-muted">
      说明：申请提交后由教务统一安排补考 / 重修场次，安排完成后列表会显示场次编号；同一门课程存在"待安排"申请时不能重复提交，如需取消请联系教务处。
    </div>
  </div>
</template>

<style scoped>
.retake__summary {
  display: flex;
  text-align: center;
}

.retake__summary-item { flex: 1; }

.retake__summary-value { font-size: 20px; font-weight: 600; }

.retake__actions { margin-bottom: 12px; }

.retake__course { font-size: 15px; font-weight: 600; }

.retake__meta { margin-top: 6px; }

.retake__popup-title { padding: 14px 16px 6px; font-size: 16px; font-weight: 600; text-align: center; }

.retake__courses { max-height: 45vh; padding: 4px 16px; overflow-y: auto; }

.retake__course-item { padding: 6px 0; }

.retake__popup-actions { padding: 8px 16px 20px; }

.retake__tip { margin-top: 16px; line-height: 1.6; }
</style>
