<script setup lang="ts">
/**
 * 卡片标题区：对齐 admin 端 CommonTable 的卡片头（标题左、操作右）。
 * 用法：<PageHeader title="课程管理"><template #actions><van-button .../></template></PageHeader>
 */
defineProps<{
  title?: string
}>()
</script>

<template>
  <div class="st-card__header">
    <span class="st-card__title">{{ title }}</span>
    <div class="st-card__actions">
      <slot name="actions" />
    </div>
  </div>
</template>
