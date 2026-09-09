<script setup lang="ts">
import { Tools } from '@element-plus/icons-vue'

defineProps<{
  /** 模块名称（与需求文档一致） */
  title: string
  /** 模块职责描述 */
  description: string
}>()
</script>

<template>
  <el-card shadow="never" class="module-placeholder">
    <template #header>
      <div class="module-placeholder__header">
        <el-icon :size="18" color="#409eff">
          <component :is="Tools" />
        </el-icon>
        <span class="module-placeholder__title">{{ title }}</span>
      </div>
    </template>

    <el-empty :description="description" />
    <div class="module-placeholder__tip">
      <p>
        页面骨架已挂接好菜单与路由，请在对应的
        <code>src/views/xxx</code> 目录下继续开发；接口定义与模拟数据分别在
        <code>src/api</code>、<code>src/mock</code> 中扩展。
      </p>
    </div>
  </el-card>
</template>

<style scoped>
.module-placeholder__header {
  display: flex;
  gap: 8px;
  align-items: center;
}

.module-placeholder__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.module-placeholder__tip {
  display: flex;
  justify-content: center;
  padding: 0 24px 24px;
}

.module-placeholder__tip p {
  margin: 0;
  font-size: 12px;
  line-height: 1.8;
  color: #909399;
}

.module-placeholder__tip code {
  padding: 2px 6px;
  color: #409eff;
  background-color: #ecf5ff;
  border-radius: 4px;
}
</style>
