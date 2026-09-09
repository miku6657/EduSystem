<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <div class="not-found__code">404</div>
    <p class="not-found__desc">抱歉，您访问的页面不存在或已被移除</p>
    <el-button type="primary" @click="router.replace('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.not-found__code {
  font-size: 72px;
  font-weight: 700;
  color: #409eff;
}

.not-found__desc {
  margin: 0 0 8px;
  color: #909399;
}
</style>
