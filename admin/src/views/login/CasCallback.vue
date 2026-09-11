<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

onMounted(async () => {
  const token = route.query.token
  const error = route.query.error

  if (typeof error === 'string' && error) {
    ElMessage.error(error)
    await router.replace('/login')
    return
  }

  if (typeof token !== 'string' || !token) {
    ElMessage.error('CAS登录失败')
    await router.replace('/login')
    return
  }

  try {
    await userStore.completeCasLogin(token)
    ElMessage.success('CAS登录成功')
    await router.replace('/dashboard')
  } catch {
    userStore.reset()
    await router.replace('/login')
  }
})
</script>

<template>
  <div class="cas-callback">
    <el-icon class="is-loading" size="32">
      <Loading />
    </el-icon>
    <p>正在完成统一身份认证...</p>
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
