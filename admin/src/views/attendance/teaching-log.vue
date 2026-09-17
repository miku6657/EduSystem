<script setup lang="ts">
import {
  onMounted,
  ref,
} from 'vue'

import {
  ElMessage,
} from 'element-plus'

import {
  getTeachers,
  getTeacherWeeklyLogs,
} from '@/api/teaching-log'

import type {
  TeacherItem,
  TeachingLogItem,
} from '@/api/teaching-log'

const teachers =
  ref<TeacherItem[]>([])

const rows =
  ref<TeachingLogItem[]>([])

const teacherId =
  ref('')

const date =
  ref(
    new Date()
      .toISOString()
      .slice(0, 10),
  )

const loading =
  ref(false)

async function loadTeachers() {

  try {

    const page =
      await getTeachers()

    teachers.value =
      page.records

    if (
      !teacherId.value
      && teachers.value.length
    ) {
      teacherId.value =
        String(
          teachers.value[0].id,
        )
    }

  } catch {
    // request统一提示
  }
}

async function loadLogs() {

  if (!teacherId.value) {
    ElMessage.warning(
      '请选择教师',
    )

    return
  }

  loading.value =
    true

  try {

    rows.value =
      await getTeacherWeeklyLogs(
        teacherId.value,
        date.value,
      )

  } catch {
    // request统一提示
  } finally {

    loading.value =
      false
  }
}

onMounted(
  async () => {

    await loadTeachers()

    if (teacherId.value) {
      await loadLogs()
    }
  },
)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <el-form
        inline
        @submit.prevent="
          loadLogs
        "
      >
        <el-form-item label="教师">
          <el-select
            v-model="teacherId"
            filterable
            style="width: 200px"
            placeholder="请选择教师"
          >
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="
                `${teacher.name}（${teacher.teacherNo}）`
              "
              :value="
                String(
                  teacher.id,
                )
              "
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所在周">
          <el-date-picker
            v-model="date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="loadLogs"
          >
            查询
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <strong>
          教学日志
          （{{ rows.length }} 条）
        </strong>
      </template>

      <el-table
        v-loading="loading"
        :data="rows"
        stripe
      >
        <el-table-column
          prop="teachingDate"
          label="授课日期"
          width="120"
        />

        <el-table-column
          prop="teacherName"
          label="教师"
          width="120"
        />

        <el-table-column
          prop="courseName"
          label="课程"
          min-width="150"
        />

        <el-table-column
          prop="className"
          label="班级"
          min-width="150"
        />

        <el-table-column
          prop="content"
          label="授课内容"
          min-width="280"
          show-overflow-tooltip
        />

        <el-table-column
          prop="homework"
          label="课后作业"
          min-width="220"
          show-overflow-tooltip
        />

        <template #empty>
          <el-empty
            description="该教师本周暂无教学日志"
          />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
</style>
