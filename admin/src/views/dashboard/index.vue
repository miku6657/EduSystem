<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Calendar, CircleCheck, Notebook, OfficeBuilding } from '@element-plus/icons-vue'
import type { DashboardStatistics } from '@/api/dashboard'

const loading = ref(false)
const stats = ref<DashboardStatistics>()

const pendingApprovals = computed(() => stats.value?.pendingApprovals ?? [])

const cards = computed(() => [
  {
    label: '总课程数（门）',
    value: stats.value?.courseCount ?? 0,
    icon: Notebook,
    color: '#409eff',
  },
  {
    label: '总教室数（间）',
    value: stats.value?.classroomCount ?? 0,
    icon: OfficeBuilding,
    color: '#67c23a',
  },
  {
    label: '今日调课申请（条）',
    value: stats.value?.todayAdjustCount ?? 0,
    icon: Calendar,
    color: '#e6a23c',
  },
  {
    label: '待审核毕业（人）',
    value: stats.value?.pendingGraduationCount ?? 0,
    icon: CircleCheck,
    color: '#f56c6c',
  },
])

onMounted(() => {})
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.label" :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="dashboard__stat">
          <div class="dashboard__stat-body">
            <el-icon :size="36" :color="card.color">
              <component :is="card.icon" />
            </el-icon>
            <div class="dashboard__stat-meta">
              <div class="dashboard__stat-value">{{ card.value }}</div>
              <div class="dashboard__stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" header="最近 5 条待办审批">
      <el-table :data="pendingApprovals" stripe>
        <el-table-column prop="type" label="审批类型" width="130" align="center" />
        <el-table-column prop="title" label="审批事项" min-width="260" show-overflow-tooltip />
        <el-table-column prop="applicant" label="申请人" width="120" />
        <el-table-column prop="applyTime" label="申请时间" width="170" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '已通过' ? 'success' : 'warning'">
              {{ row.status || '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard__stat {
  margin-bottom: 16px;
}

.dashboard__stat-body {
  display: flex;
  gap: 14px;
  align-items: center;
}

.dashboard__stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.dashboard__stat-label {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}
</style>
