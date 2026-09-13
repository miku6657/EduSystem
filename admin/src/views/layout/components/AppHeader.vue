<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Bell, Expand, Fold } from '@element-plus/icons-vue'
import type { TodoItem } from '@/api/todo'
import { useAppStore } from '@/stores/appStore'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const breadcrumbs = computed(() => route.matched.filter((item) => item.meta.title))
const displayName = computed(
  () => userStore.userInfo?.username || '系统管理员',
)

/** 审批待办（调课审批 + 教室申请审批） */
const todos = ref<TodoItem[]>([])
const todoCount = computed(() => todos.value.length)

/** 点击待办条目跳转到对应审核列表页 */
const handleTodoCommand = (todo: TodoItem) => {
  if (todo.route) {
    router.push(todo.route)
  }
}

onMounted(() => {
  // 顶部导航：加载当前学期（appStore + mock /api/term/current）
  if (!appStore.currentTerm) {
    appStore.fetchCurrentTerm().catch(() => undefined)
  }
  // 顶部导航：加载审批待办提醒
})

const handleUserCommand = async (command: string) => {
  if (command !== 'logout') {
    return
  }
  try {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    })
    await userStore.logout()
    router.replace({ path: '/login' })
  } catch {
    // 用户取消退出
  }
}
</script>

<template>
  <div class="app-header">
    <div class="app-header__left">
      <el-icon class="app-header__trigger" :size="20" @click="appStore.toggleSidebar()">
        <component :is="appStore.sidebarCollapsed ? Expand : Fold" />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.meta.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="app-header__right">
      <el-tag type="info" effect="plain" class="app-header__term">
        {{ appStore.currentTerm || '当前学期加载中…' }}
      </el-tag>

      <!-- 待办提醒：调课审批 + 教室申请审批 统一入口 -->
      <el-dropdown trigger="click" @command="handleTodoCommand">
        <div class="app-header__bell">
          <el-badge :value="todoCount" :hidden="todoCount === 0" :max="99" :offset="[0, 4]">
            <el-icon :size="18">
              <Bell />
            </el-icon>
          </el-badge>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="todo-menu">
            <el-dropdown-item disabled>
              <span class="todo-menu__summary">您有 {{ todoCount }} 条待审批</span>
            </el-dropdown-item>
            <el-dropdown-item
              v-for="todo in todos"
              :key="todo.id"
              class="todo-menu__item"
              :command="todo"
            >
              <span class="todo-menu__type">
                {{ todo.type === 'course-adjust' ? '调课审批' : '教室申请' }}
              </span>
              <span class="todo-menu__title">{{ todo.title }}</span>
            </el-dropdown-item>
            <el-dropdown-item v-if="!todos.length" disabled>暂无待办</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-dropdown trigger="click" @command="handleUserCommand">
        <div class="app-header__user">
          <el-avatar :size="30">{{ displayName.charAt(0) }}</el-avatar>
          <span class="app-header__name">{{ displayName }}</span>
          <el-icon :size="12">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 16px;
}

.app-header__left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.app-header__trigger {
  cursor: pointer;
  color: #606266;
}

.app-header__trigger:hover {
  color: #409eff;
}

.app-header__right {
  display: flex;
  gap: 16px;
  align-items: center;
}

.app-header__bell {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.app-header__bell:hover {
  color: #409eff;
}

.app-header__user {
  display: flex;
  gap: 8px;
  align-items: center;
  cursor: pointer;
  color: #303133;
}

.app-header__name {
  font-size: 14px;
}

.todo-menu__summary {
  font-weight: 600;
  color: #303133;
}

.todo-menu__item {
  display: flex;
  align-items: center;
  width: 260px;
}

.todo-menu__type {
  flex-shrink: 0;
  margin-right: 8px;
  padding: 0 6px;
  font-size: 12px;
  color: #409eff;
  background-color: #ecf5ff;
  border-radius: 4px;
}

.todo-menu__title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
