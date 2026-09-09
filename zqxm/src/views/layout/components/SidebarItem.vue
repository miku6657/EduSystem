<script setup lang="ts">
import { computed } from 'vue'
import type { RouteRecordRaw } from 'vue-router'

defineOptions({ name: 'SidebarItem' })

const props = defineProps<{
  /** 路由记录（来自 src/router/modules） */
  item: RouteRecordRaw
  /** 父级完整路径，用于拼接相对路径的菜单项 */
  basePath?: string
}>()

/** 当前路由对应的完整路径（菜单 index / 跳转地址） */
const fullPath = computed(() => {
  if (props.item.path.startsWith('/')) {
    return props.item.path
  }
  const base = props.basePath ?? ''
  return `${base}/${props.item.path}`.replace(/\/{2,}/g, '/')
})

/** 递归：只展示带标题且允许进入菜单的子路由 */
const visibleChildren = computed(
  () =>
    props.item.children?.filter(
      (child) => child.meta?.title && child.meta?.menu !== false,
    ) ?? [],
)

const isGroup = computed(() => visibleChildren.value.length > 0)
</script>

<template>
  <el-sub-menu v-if="isGroup" :index="fullPath">
    <template #title>
      <el-icon>
        <component :is="item.meta?.icon" />
      </el-icon>
      <span>{{ item.meta?.title }}</span>
    </template>
    <SidebarItem
      v-for="child in visibleChildren"
      :key="child.path"
      :item="child"
      :base-path="fullPath"
    />
  </el-sub-menu>

  <el-menu-item v-else :index="fullPath">
    <el-icon>
      <component :is="item.meta?.icon" />
    </el-icon>
    <template #title>
      <span>{{ item.meta?.title }}</span>
    </template>
  </el-menu-item>
</template>
