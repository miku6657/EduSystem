<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useUserStore } from '@/stores/user'
import { filterAccessibleRoutes } from '@/router/permission'
import { layoutModuleRoutes } from '@/router/modules'
import { roleLabel } from '@/utils/role'

const router = useRouter()
const userStore = useUserStore()

/** 当前角色可用的功能入口（不含首页自身、不含 tabbar 已直接展示的页面） */
const features = computed(() =>
  filterAccessibleRoutes(layoutModuleRoutes, userStore.role).filter(
    (route) => route.path !== 'home' && route.meta?.tabbar !== true,
  ),
)

const roleText = computed(() => roleLabel(userStore.role))

async function onLogout() {
  try {
    await showConfirmDialog({ title: '退出登录', message: '确定要退出当前账号吗？' })
  } catch {
    return
  }
  await userStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div>
    <div class="st-card home__profile">
      <div class="home__avatar">{{ userStore.displayName.slice(0, 1) }}</div>
      <div class="home__info">
        <div class="home__name">{{ userStore.displayName }}</div>
        <div class="st-muted">{{ roleText }} · {{ userStore.businessNo }}</div>
        <div class="st-muted">学期：{{ userStore.currentTerm || '—' }}</div>
      </div>
    </div>

    <van-notice-bar
      v-if="!userStore.businessId"
      left-icon="info-o"
      wrapable
      :scrollable="false"
      text="未解析到当前身份的业务ID，部分数据可能无法加载：请确认登录账号为学号/工号，或联系教务在后台维护对应关系。"
    />

    <div class="st-section-title">常用功能</div>
    <van-grid :column-num="3" :border="false" square>
      <van-grid-item
        v-for="item in features"
        :key="item.path"
        :icon="item.meta?.icon"
        :text="item.meta?.title"
        @click="router.push(`/${item.path}`)"
      />
    </van-grid>

    <div v-if="features.length === 0" class="st-card st-muted">
      当前角色暂无其他功能入口。
    </div>

    <div class="home__logout">
      <van-button round block plain type="danger" @click="onLogout">退出登录</van-button>
    </div>
  </div>
</template>

<style scoped>
.home__profile {
  display: flex;
  gap: 12px;
  align-items: center;
}

.home__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  font-size: 20px;
  color: #fff;
  background: var(--st-primary);
  border-radius: 50%;
}

.home__info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.home__name {
  font-size: 16px;
  font-weight: 600;
}

.home__logout {
  margin: 24px 4px 0;
}
</style>
