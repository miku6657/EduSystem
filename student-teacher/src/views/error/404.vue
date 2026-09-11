<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="st-page st-page--plain">
    <van-empty image="error" description="页面不存在或已被移除">
      <van-button round type="primary" class="error__btn" @click="router.replace('/home')">
        返回首页
      </van-button>
    </van-empty>
  </div>
</template>

<style scoped>
.error__btn {
  width: 160px;
}
</style>
