<script setup lang="ts">
import { onMounted } from 'vue'

const UNIFIED_LOGIN_URL =
  'http://localhost:5173/login'

onMounted(() => {
  window.location.replace(
    UNIFIED_LOGIN_URL,
  )
})
</script>

<template>
  <div class="login-redirect">
    <van-loading
      type="spinner"
      size="32px"
      vertical
    >
      正在前往统一身份认证...
    </van-loading>
  </div>
</template>

<style scoped>
.login-redirect {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
}
</style>
