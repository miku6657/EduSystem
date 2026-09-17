<script setup lang="ts">
import {
  computed,
} from 'vue'

import {
  useRouter,
} from 'vue-router'

import {
  showConfirmDialog,
} from 'vant'

import {
  useUserStore,
} from '@/stores/user'

import {
  filterAccessibleRoutes,
} from '@/router/permission'

import {
  layoutModuleRoutes,
} from '@/router/modules'

import {
  roleLabel,
} from '@/utils/role'

const router =
  useRouter()

const userStore =
  useUserStore()

/**
 * 当前角色名称
 */
const roleText =
  computed(() =>
    roleLabel(
      userStore.role,
    ),
  )

/**
 * 当前角色可以访问的业务功能。
 *
 * 首页本身不重复显示。
 * tabbar页面也不重复显示。
 */
const features =
  computed(() =>
    filterAccessibleRoutes(
      layoutModuleRoutes,
      userStore.role,
    ).filter(
      (route) =>
        route.path !== 'home'
        && route.meta?.tabbar
          !== true,
    ),
  )

/**
 * 当前用户真实姓名首字。
 */
const avatarText =
  computed(() => {
    const name =
      userStore.displayName

    if (!name) {
      return '用'
    }

    return name.slice(
      0,
      1,
    )
  })

/**
 * 退出登录。
 *
 * 师生端已经改为CAS，
 * 所以不再调用原来的账号密码退出接口。
 *
 * userStore.logout()
 * 会：
 *
 * 1. 清除JWT
 * 2. 清除用户身份
 * 3. 清除业务档案缓存
 * 4. 返回统一登录入口
 */
async function onLogout() {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message:
        '确定要退出当前账号吗？',
    })
  } catch {
    return
  }

  userStore.logout()
}
</script>

<template>
  <div class="home">
    <!-- 当前用户 -->
    <div
      class="
        st-card
        home__profile
      "
    >
      <div class="home__avatar">
        {{ avatarText }}
      </div>

      <div class="home__info">
        <div class="home__name">
          {{ userStore.displayName }}
        </div>

        <div class="st-muted">
          {{ roleText }}
        </div>

        <div class="st-muted">
          学期：
          {{
            userStore.currentTerm
              || '—'
          }}
        </div>
      </div>
    </div>

    <!-- 身份信息 -->
    <div class="st-card">
      <div class="home__section-title">
        当前身份
      </div>

      <div class="home__identity">
        <div class="home__identity-row">
          <span class="st-muted">
            姓名
          </span>

          <span>
            {{
              userStore.profile?.name
                || '—'
            }}
          </span>
        </div>

        <div class="home__identity-row">
          <span class="st-muted">
            身份
          </span>

          <span>
            {{ roleText }}
          </span>
        </div>

        <div class="home__identity-row">
          <span class="st-muted">
            当前学期
          </span>

          <span>
            {{
              userStore.currentTerm
                || '—'
            }}
          </span>
        </div>
      </div>
    </div>

    <!-- 当前角色功能 -->
    <div class="st-card">
      <div class="home__section-title">
        常用功能
      </div>

      <van-grid
        :column-num="3"
        :border="false"
        square
      >
        <van-grid-item
          v-for="item in features"
          :key="item.path"
          :icon="item.meta?.icon"
          :text="item.meta?.title"
          @click="
            router.push(
              `/${item.path}`,
            )
          "
        />
      </van-grid>

      <div
        v-if="
          features.length === 0
        "
        class="st-muted"
      >
        当前角色暂无其他功能入口。
      </div>
    </div>

    <!-- 退出 -->
    <div class="home__logout">
      <van-button
        round
        block
        plain
        type="danger"
        @click="onLogout"
      >
        退出登录
      </van-button>
    </div>
  </div>
</template>

<style scoped>
.home {
  padding-bottom: 24px;
}

.home__profile {
  display: flex;
  gap: 12px;
  align-items: center;
}

.home__avatar {
  display: flex;
  flex-shrink: 0;
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
  gap: 3px;
  min-width: 0;
}

.home__name {
  overflow: hidden;
  font-size: 17px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home__section-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
}

.home__identity {
  display: flex;
  flex-direction: column;
}

.home__identity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 42px;
}

.home__identity-row
  + .home__identity-row {
  border-top:
    1px solid
    var(--st-border);
}

.home__logout {
  margin: 24px 4px 0;
}
</style>
