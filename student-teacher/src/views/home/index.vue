<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useUserStore } from '@/stores/user'
import { filterAccessibleRoutes } from '@/router/permission'
import { layoutModuleRoutes } from '@/router/modules'
import { roleLabel } from '@/utils/role'
import { todayStr } from '@/utils/format'
import { listMyAttendance } from '@/api/teacherAttendance'
import { listMyTeachingLogs } from '@/api/teachingLog'
import { listMyInvigilations } from '@/api/examMonitor'
import { listMyClassroomApplies } from '@/api/classroom'
import { listMyRetakes } from '@/api/retake'
import { listMyUpgradeApplies } from '@/api/upgrade'

const router = useRouter()
const userStore = useUserStore()

/** 当前角色可用的功能入口（不含首页自身、不含 tabbar 已直接展示的页面） */
const features = computed(() =>
  filterAccessibleRoutes(layoutModuleRoutes, userStore.role).filter(
    (route) => route.path !== 'home' && route.meta?.tabbar !== true,
  ),
)

const roleText = computed(() => roleLabel(userStore.role))

/* ------------------------------ 我的待办 ------------------------------ */
interface TodoItem {
  text: string
  route: string
}

const todos = ref<TodoItem[]>([])
const todosLoading = ref(false)

/**
 * 待办由现有接口在前端拼装，不依赖后端待办接口：
 * 教师看「今日是否签到 / 本周日志是否提交 / 近期待监考」，学生看「待审批申请 / 待安排补考 / 待审核报名」。
 */
async function loadTodos() {
  if (!userStore.token) {
    return
  }
  todosLoading.value = true
  const items: TodoItem[] = []
  const today = todayStr()
  try {
    if (userStore.isTeacher && userStore.businessId) {
      const teacherId = userStore.businessId
      const [attendance, logs, monitors] = await Promise.all([
        listMyAttendance(teacherId, today, today),
        listMyTeachingLogs(teacherId, today),
        listMyInvigilations(teacherId),
      ])
      if (attendance.length === 0) {
        items.push({ text: '今日还未签到', route: '/teacher/check-in' })
      }
      if (logs.length === 0) {
        items.push({ text: '本周还未提交教学日志', route: '/teacher/teaching-log' })
      }
      const upcoming = monitors.filter((item) => item.examDate && item.examDate >= today)
      if (upcoming.length > 0) {
        items.push({ text: `有 ${upcoming.length} 场监考待完成`, route: '/teacher/invigilation' })
      }
    } else if (userStore.isStudent && userStore.businessId) {
      const studentId = userStore.businessId
      const [applies, retakes, upgrades] = await Promise.all([
        listMyClassroomApplies(),
        listMyRetakes(studentId),
        listMyUpgradeApplies(studentId),
      ])
      const waitingApplies = applies.filter((item) => item.status === '待审核').length
      if (waitingApplies > 0) {
        items.push({ text: `有 ${waitingApplies} 条教室申请待审批`, route: '/classroom-apply' })
      }
      const pendingRetakes = retakes.filter(
        (item) => item.examId === null || item.examId === undefined,
      ).length
      if (pendingRetakes > 0) {
        items.push({ text: `有 ${pendingRetakes} 条补考/重修待安排`, route: '/student/retake' })
      }
      const waitingUpgrade = upgrades.filter((item) => item.applyStatus === 'WAIT').length
      if (waitingUpgrade > 0) {
        items.push({ text: `专升本报名待审核 ${waitingUpgrade} 条`, route: '/student/upgrade' })
      }
    }
  } catch {
    // 待办属于增强信息，失败不阻塞首页
  } finally {
    todos.value = items
    todosLoading.value = false
  }
}

async function onLogout() {
  try {
    await showConfirmDialog({ title: '退出登录', message: '确定要退出当前账号吗？' })
  } catch {
    return
  }
  await userStore.logout()
  router.replace('/login')
}

onMounted(loadTodos)
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

    <div class="st-section-title">我的待办</div>
    <div v-if="todosLoading" class="st-card st-muted">加载中…</div>
    <div v-else-if="todos.length === 0" class="st-card st-muted">暂无待办事项</div>
    <template v-else>
      <div
        v-for="item in todos"
        :key="item.text"
        class="st-card st-row home__todo"
        @click="router.push(item.route)"
      >
        <span>{{ item.text }}</span>
        <van-icon name="arrow" />
      </div>
    </template>

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

    <div v-if="features.length === 0" class="st-card st-muted">当前角色暂无其他功能入口。</div>

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
