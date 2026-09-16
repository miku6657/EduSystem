<script setup lang="ts">
/**
 * 统计条：一排「数值 + 文案」，用于页面顶部概览。
 * 自身不带卡片外壳，放在 `.st-card` 里使用（与 admin 的卡片结构保持一致）：
 *   <div class="st-card"><StatBar :items="items" /></div>
 */
defineProps<{
  items: Array<{ label: string; value: string | number }>
}>()
</script>

<template>
  <div class="stat-bar">
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
