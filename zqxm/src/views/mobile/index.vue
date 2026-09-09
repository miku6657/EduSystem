<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStatistics } from '@/api/dashboard'
import type { DashboardStatistics } from '@/api/dashboard'

const router = useRouter()
const loading = ref(false)
const stats = ref<DashboardStatistics>()

const loadData = async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStatistics()
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    loading.value = false
  }
}

loadData()
</script>

<template>
  <div class="mobile-demo">
    <van-nav-bar title="教学过程管理系统" left-text="返回" fixed placeholder @click-left="router.back()" />

    <van-loading v-if="loading" class="mobile-demo__loading" vertical>加载中...</van-loading>

    <template v-else>
      <van-cell-group inset title="运行数据（Vant 4 演示）">
        <van-cell title="在授课程（门）" :value="String(stats?.courseCount ?? '-')" />
        <van-cell title="待审调课（条）" :value="String(stats?.pendingAdjust ?? '-')" />
        <van-cell title="待审教室申请（条）" :value="String(stats?.pendingClassroom ?? '-')" />
        <van-cell
          title="教室平均使用率"
          :value="stats ? `${stats.classroomUsageRate}%` : '-'"
        />
      </van-cell-group>

      <div class="mobile-demo__actions">
        <van-button type="primary" block round @click="loadData">刷新数据</van-button>
      </div>

      <p class="mobile-demo__tip">
        说明：本页为移动端 H5 示例。PC 端请使用 Element Plus（见左侧布局），移动端页面可直接使用 Vant 4 组件开发。
      </p>
    </template>
  </div>
</template>

<style>
.mobile-demo {
  min-height: 100%;
  background-color: #f7f8fa;
}

.mobile-demo__loading {
  padding-top: 80px;
}

.mobile-demo__actions {
  padding: 20px 16px;
}

.mobile-demo__tip {
  margin: 0;
  padding: 4px 20px 24px;
  font-size: 12px;
  line-height: 1.7;
  color: #969799;
}
</style>
