<script setup lang="ts">
/**
 * 页面状态容器：统一「加载中 / 加载失败 / 空数据」三种状态，
 * 正常时渲染默认插槽内容。新页面请优先使用它，避免每个页面重复写三态。
 */
withDefaults(
  defineProps<{
    loading?: boolean
    error?: string
    empty?: boolean
    emptyText?: string
    loadingText?: string
  }>(),
  {
    loading: false,
    error: '',
    empty: false,
    emptyText: '暂无数据',
    loadingText: '加载中…',
  },
)

defineEmits<{ (e: 'retry'): void }>()
</script>

<template>
  <div v-if="loading" class="st-empty">
    <van-loading vertical>{{ loadingText }}</van-loading>
  </div>

  <van-empty v-else-if="error" image="error" :description="error">
    <van-button round type="primary" size="small" @click="$emit('retry')">重新加载</van-button>
  </van-empty>

  <van-empty v-else-if="empty" :description="emptyText" />

  <slot v-else />
</template>
