<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { filterTabbarRoutes } from '@/router/permission'
import { layoutModuleRoutes } from '@/router/modules'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 底部 tabbar：按当前角色过滤（学生/教师各 4 个） */
const tabs = computed(() => filterTabbarRoutes(layoutModuleRoutes, userStore.role))
/** 当前页标题 */
const pageTitle = computed(() => (route.meta.title as string | undefined) ?? '师生端')
/** tabbar 选中项（用完整路径匹配） */
const activePath = computed(() => route.path)

function onTabChange(path: string) {
  if (path !== route.path) {
    router.push(path)
  }
}
</script>

<template>
  <div class="layout">
    <van-nav-bar :title="pageTitle" fixed placeholder />

    <div class="st-page">
      <router-view />
    </div>

    <van-tabbar :model-value="activePath" fixed active-color="#409eff" @change="onTabChange">
      <van-tabbar-item
        v-for="item in tabs"
        :key="item.path"
        :name="`/${item.path}`"
        :icon="item.meta?.icon"
      >
        {{ item.meta?.title }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100%;
}

/* 顶栏底部细线，与 admin 的 el-header（白底 + 1px #e4e7ed 下边框）一致 */
.layout :deep(.van-nav-bar) {
  border-bottom: 1px solid var(--st-border);
}
</style>
