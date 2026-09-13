<script setup lang="ts">
/** 统计条：一排「数值 + 文案」，用于页面顶部概览 */
defineProps<{
  items: Array<{ label: string; value: string | number }>
}>()
</script>

<template>
  <div class="st-card stat-bar">
    <div v-for="item in items" :key="item.label" class="stat-bar__item">
      <div class="stat-bar__value">{{ item.value }}</div>
      <div class="st-muted">{{ item.label }}</div>
    </div>
  </div>
</template>

<style scoped>
.stat-bar {
  display: flex;
  text-align: center;
}

.stat-bar__item {
  flex: 1;
}

.stat-bar__value {
  font-size: 20px;
  font-weight: 600;
}
</style>
