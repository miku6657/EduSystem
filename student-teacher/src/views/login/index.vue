<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: '',
})

const loading = ref(false)

/** 演示账号（由 mock 提供）：学生用学号登录、教师用工号登录 */
const demoAccounts = [
  { label: '学生演示账号', username: '2023005001', password: '123456' },
  { label: '教师演示账号', username: 'T1001', password: '123456' },
]

function fillDemo(account: { username: string; password: string }) {
  form.value.username = account.username
  form.value.password = account.password
}

async function onSubmit() {
  if (!form.value.username || !form.value.password) {
    showToast('请输入账号与密码')
    return
  }
  loading.value = true
  try {
    await userStore.login({
      username: form.value.username.trim(),
      password: form.value.password,
    })
    showToast('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/home'
    router.replace(redirect)
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="st-page st-page--plain login">
    <div class="login__header">
      <h1 class="login__title">教学过程管理系统</h1>
      <p class="login__subtitle">师生端 · 学生 / 教师</p>
    </div>

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="账号"
          placeholder="学号 / 工号"
          clearable
          :rules="[{ required: true, message: '请输入学号或工号' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          clearable
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="login__actions">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          登录
        </van-button>
      </div>
    </van-form>

    <div class="login__demo">
      <div class="st-section-title">演示账号（本地 Mock）</div>
      <van-cell-group inset>
        <van-cell
          v-for="account in demoAccounts"
          :key="account.username"
          :title="account.label"
          :label="`${account.username} / ${account.password}`"
          is-link
          @click="fillDemo(account)"
        />
      </van-cell-group>
      <p class="st-muted login__tip">
        正式环境请用学校统一身份（CAS）或教务下发的账号登录；
        管理员请使用后台管理端（admin 工程）。
      </p>
    </div>
  </div>
</template>

<style scoped>
.login {
  padding-top: 48px;
}

.login__header {
  margin-bottom: 24px;
  text-align: center;
}

.login__title {
  margin: 0;
  font-size: 22px;
}

.login__subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--st-text-light);
}

.login__actions {
  margin: 20px 16px 0;
}

.login__demo {
  margin-top: 32px;
}

.login__tip {
  margin: 12px 20px 0;
  line-height: 1.6;
}
</style>
