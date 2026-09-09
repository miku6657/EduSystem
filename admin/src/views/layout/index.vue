<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/appStore'
import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'

const appStore = useAppStore()
const asideWidth = computed(() => (appStore.sidebarCollapsed ? '64px' : '220px'))
</script>

<template>
  <el-container class="layout">
    <el-aside class="layout__aside" :width="asideWidth">
      <AppSidebar />
    </el-aside>
    <el-container class="layout__body">
      <el-header class="layout__header" height="56px">
        <AppHeader />
      </el-header>
      <el-main class="layout__main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100%;
}

.layout__aside {
  overflow: hidden;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.2s ease;
}

.layout__header {
  padding: 0;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.layout__main {
  padding: 16px;
  overflow: auto;
  background-color: #f5f7fa;
}
</style>
