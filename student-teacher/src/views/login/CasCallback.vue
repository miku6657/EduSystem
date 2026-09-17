<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const message = ref(
  '正在完成统一身份认证...',
)

function goUnifiedLogin() {
  window.location.replace(
    'http://localhost:5173/login',
  )
}

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
    message.value =
      '统一身份认证失败'

    showToast(error)

    setTimeout(() => {
      goUnifiedLogin()
    }, 1000)

    return
  }

  /**
   * 没有拿到 JWT
   */
  if (
    typeof token !== 'string'
    || !token
  ) {
    message.value =
      '未获取到登录凭证'

    showToast(
      '统一身份认证失败，请重新登录',
    )

    setTimeout(() => {
      goUnifiedLogin()
    }, 1000)

    return
  }

  try {
    /**
     * 保存 JWT
     *
     * 然后调用：
     * GET /api/auth/userinfo
     *
     * 获取 STUDENT / TEACHER
     * 并解析对应学生/教师资料。
     */
    const userInfo =
      await userStore.completeCasLogin(
        token,
      )

    message.value =
      '登录成功，正在进入系统...'

    if (userInfo.role === 'student') {
      showToast('学生登录成功')
    } else {
      showToast('教师登录成功')
    }

    /**
     * 进入师生端首页
     */
    await router.replace('/home')
  } catch (error) {
    console.error(
      '师生端CAS登录失败：',
      error,
    )

    message.value = '登录失败'

    showToast(
      '无法完成统一身份认证',
    )

    setTimeout(() => {
      goUnifiedLogin()
    }, 1000)
  }
})
</script>

<template>
  <div class="cas-callback">
    <div class="cas-callback__card">
      <van-loading
        type="spinner"
        size="36px"
        vertical
      >
        {{ message }}
      </van-loading>
    </div>
  </div>
</template>

<style scoped>
.cas-callback {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background:
    linear-gradient(
      135deg,
      #2563eb 0%,
      #3b82f6 50%,
      #93c5fd 100%
    );
}

.cas-callback__card {
  width: 100%;
  max-width: 360px;
  padding: 48px 24px;
  text-align: center;
  background: #fff;
  border-radius: 12px;
  box-shadow:
    0 12px 32px
    rgb(0 0 0 / 15%);
}

.cas-callback__card :deep(
  .van-loading__text
) {
  margin-top: 16px;
  font-size: 14px;
}
</style>
