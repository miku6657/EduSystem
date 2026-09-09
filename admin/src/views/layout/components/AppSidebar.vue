<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Notebook } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/appStore'
import { useUserStore } from '@/stores/user'
import { layoutModuleRoutes } from '@/router/modules'
import { filterMenusByRoles } from '@/router/permission'
import SidebarItem from './SidebarItem.vue'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

/**
 * 菜单由 src/router/modules 下的路由配置自动生成，
 * 并按 useUserStore 中的 roles 动态过滤（admin 全部可见；manager 仅可见教务相关菜单）。
 */
const menus = computed(() => filterMenusByRoles(layoutModuleRoutes, userStore.roles))
const activePath = computed(() => route.path)
/** 按当前路由匹配结果自动展开对应的一级/多级分组 */
const openedPaths = computed(() => {
  const opened: string[] = []
  let parent = ''
  for (const record of route.matched) {
    const path = record.path.startsWith('/')
      ? record.path
      : `${parent}/${record.path}`
    parent = path.replace(/\/{2,}/g, '/')
    // 只展开菜单分组（有标题且不是当前叶子路由）
    if (record.meta.title && route.matched[route.matched.length - 1] !== record) {
      opened.push(parent)
    }
  }
  return opened
})
</script>

<template>
  <div class="app-sidebar">
    <div class="app-sidebar__logo">
      <el-icon :size="24" color="#409eff">
        <component :is="Notebook" />
      </el-icon>
      <span v-show="!appStore.sidebarCollapsed" class="app-sidebar__title">
        教学过程管理系统
      </span>
    </div>
    <el-scrollbar class="app-sidebar__scroll">
      <el-menu
        class="app-sidebar__menu"
        :default-active="activePath"
        :default-openeds="openedPaths"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        router
      >
        <SidebarItem v-for="item in menus" :key="item.path" :item="item" />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<style scoped>
.app-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.app-sidebar__logo {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
  align-items: center;
  justify-content: center;
  height: 56px;
  overflow: hidden;
  white-space: nowrap;
}

.app-sidebar__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.app-sidebar__scroll {
  flex: 1;
}

.app-sidebar__menu {
  border-right: none;
}

.app-sidebar__menu :deep(.el-menu-item),
.app-sidebar__menu :deep(.el-sub-menu__title) {
  margin: 4px 8px;
  border-radius: 6px;
}
</style>
