<script setup lang="ts">
/**
 * 教师 · 班级花名册
 * 数据源：GET /api/teachers/{id}/classes（任教班级）
 *         GET /api/students?classId=（班级学生）
 * 导出：  前端本地生成 Excel（不经过后端）
 */
import { computed, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { listMyClasses, listStudentsByClass } from '@/api/base'
import type { ClassInfo, Student } from '@/api/base'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import PageHeader from '@/components/PageHeader.vue'
import PageState from '@/components/PageState.vue'
import StatBar from '@/components/StatBar.vue'
import { exportToExcel, timestampedFileName } from '@/utils/excel'

const userStore = useUserStore()
const teacherId = computed(() => userStore.businessId)

const selectedClassId = ref(0)
const keyword = ref('')
const showClassPicker = ref(false)

/** 任教班级 */
const {
  data: classes,
  loading: classLoading,
  error: classError,
  reload: reloadClasses,
} = useAsyncData<ClassInfo[]>(
  () => (teacherId.value ? listMyClasses(teacherId.value) : Promise.resolve([])),
  [],
)

/** 班级学生 */
const {
  data: students,
  loading,
  error,
  reload: reloadStudents,
} = useAsyncData<Student[]>(
  () => (selectedClassId.value ? listStudentsByClass(selectedClassId.value) : Promise.resolve([])),
  [],
)

const classText = computed(
  () => classes.value.find((item) => item.id === selectedClassId.value)?.name ?? '',
)
const classColumns = computed(() =>
  classes.value.map((item) => ({ text: item.name, value: item.id as number })),
)

/** 关键字过滤（学号 / 姓名） */
const visibleStudents = computed(() => {
  const key = keyword.value.trim()
  if (!key) {
    return students.value
  }
  return students.value.filter(
    (item) => item.name.includes(key) || String(item.studentNo).includes(key),
  )
})

const summary = computed(() => {
  const rows = students.value
  return [
    { label: '班级人数', value: rows.length },
    { label: '在读', value: rows.filter((row) => row.status === '在读').length },
    { label: '筛选结果', value: visibleStudents.value.length },
  ]
})

interface PickerPayload {
  selectedOptions?: Array<{ value?: string | number } | undefined>
}

function onClassConfirm(payload: PickerPayload) {
  const value = payload.selectedOptions?.[0]?.value
  if (typeof value === 'number') {
    selectedClassId.value = value
    keyword.value = ''
    void reloadStudents()
  }
  showClassPicker.value = false
}

function onExport() {
  if (visibleStudents.value.length === 0) {
    showToast('当前没有可导出的学生')
    return
  }
  exportToExcel({
    rows: visibleStudents.value as unknown as Array<Record<string, unknown>>,
    columns: [
      { label: '学号', key: 'studentNo' },
      { label: '姓名', key: 'name' },
      { label: '性别', key: 'gender' },
      { label: '班级ID', key: 'classId' },
      { label: '联系电话', key: 'phone' },
      { label: '状态', key: 'status' },
    ],
    fileName: timestampedFileName(`${classText.value || '班级'}花名册`),
    sheetName: '花名册',
  })
  showToast('已导出 Excel')
}

onMounted(async () => {
  await reloadClasses()
  if (classes.value.length > 0 && !selectedClassId.value) {
    selectedClassId.value = classes.value[0].id ?? 0
    await reloadStudents()
  }
})
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
        <PageHeader title="班级花名册">
          <template #actions>
            <van-button
              size="small"
              type="primary"
              :disabled="visibleStudents.length === 0"
              @click="onExport"
            >
              导出 Excel
            </van-button>
          </template>
        </PageHeader>

        <van-field
          :model-value="classText"
          label="班级"
          placeholder="请选择任教班级"
          readonly
          is-link
          @click="showClassPicker = true"
        />

        <StatBar :items="summary" class="roster__stat" />
      </div>

      <van-search v-model="keyword" class="roster__filter" placeholder="搜索学号 / 姓名" />

      <PageState
        :loading="loading || classLoading"
        :error="error || classError"
        :empty="visibleStudents.length === 0"
        :empty-text="selectedClassId ? '该班级暂无学生' : '请先选择班级'"
        @retry="reloadStudents"
      >
        <div v-for="row in visibleStudents" :key="row.id ?? row.studentNo" class="st-card st-row">
          <div>
            <div class="roster__name">{{ row.name }}</div>
            <div class="st-muted">
              {{ row.studentNo }}<template v-if="row.gender"> · {{ row.gender }}</template>
            </div>
          </div>
          <div class="roster__right">
            <span class="st-muted">{{ row.phone || '—' }}</span>
            <van-tag plain type="primary">{{ row.status || '在读' }}</van-tag>
          </div>
        </div>
      </PageState>
    </template>

    <van-popup v-model:show="showClassPicker" position="bottom" round>
      <van-empty v-if="classColumns.length === 0" description="暂无任教班级" />
      <van-picker
        v-else
        :columns="classColumns"
        @confirm="onClassConfirm"
        @cancel="showClassPicker = false"
      />
    </van-popup>
  </div>
</template>

<style scoped>
.roster__picker {
  margin-bottom: 10px;
  background: #fff;
  border-radius: 10px;
}

.roster__toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}

.roster__search {
  flex: 1;
  padding: 0;
  background: transparent;
}

.roster__name {
  font-size: 15px;
  font-weight: 600;
}

.roster__right {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
