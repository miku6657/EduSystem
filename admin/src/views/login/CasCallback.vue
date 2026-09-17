<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const STUDENT_TEACHER_CALLBACK =
  'http://localhost:5174/cas/callback'

onMounted(async () => {
  const token = route.query.token
  const error = route.query.error

  /**
   * CAS 返回错误
   */
  if (
    typeof error === 'string'
    && error
  ) {
    ElMessage.error(error)

    await router.replace('/login')
    return
  }

  /**
   * 没拿到 JWT
   */
  if (
    typeof token !== 'string'
    || !token
  ) {
    ElMessage.error('CAS登录失败')

    await router.replace('/login')
    return
  }

  try {
    /**
     * 保存 JWT
     * 并调用：
     *
     * GET /api/auth/userinfo
     *
     * 得到：
     * {
     *   id,
     *   username,
     *   role
     * }
     */
    const userInfo =
      await userStore.completeCasLogin(token)

    const role =
      String(userInfo.role)
        .replace(/^ROLE_/, '')
        .toUpperCase()

    /**
     * 管理员
     *
     * 留在管理端 5173
     */
    if (role === 'ADMIN') {
      ElMessage.success('管理员登录成功')

      await router.replace('/dashboard')
      return
    }

    /**
     * 学生 / 教师
     *
     * 转发 JWT 到师生端 5174
     */
    if (
      role === 'STUDENT'
      || role === 'TEACHER'
    ) {
      /**
       * 管理端不应该保留
       * 学生/教师的登录状态。
       */
      userStore.reset()

      const callbackUrl =
        `${STUDENT_TEACHER_CALLBACK}`
        + `?token=${encodeURIComponent(token)}`

      window.location.replace(
        callbackUrl,
      )

      return
    }

    /**
     * 未识别角色
     */
    userStore.reset()

    ElMessage.error(
      `未知用户角色：${userInfo.role}`,
    )

    await router.replace('/login')
  } catch (error) {
    console.error(
      'CAS登录处理失败：',
      error,
    )

    userStore.reset()

    ElMessage.error(
      '无法完成统一身份认证',
    )

    await router.replace('/login')
  }
})
</script>

<template>
  <div class="cas-callback">
    <el-icon
      class="is-loading"
      size="32"
    >
      <Loading />
    </el-icon>

    <p>
      正在完成统一身份认证...
    </p>
  </div>
</template>

<style scoped>
.cas-callback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
</style>
