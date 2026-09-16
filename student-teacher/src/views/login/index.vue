<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import PageHeader from '@/components/PageHeader.vue'

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
  { label: '教师演示账号', username: 'T001', password: '123456' },
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
  <div class="login">
    <!-- 主卡片：样式对齐 admin 的登录卡（渐变背景 + 白色圆角卡片 + 同款标题字号） -->
    <div class="login-card">
      <div class="login-card__header">
        <h1 class="login-card__title">教学过程管理系统</h1>
        <p class="login-card__subtitle">师生端 · 学生 / 教师</p>
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

        <div class="login-card__actions">
          <van-button block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- 演示账号卡片 -->
    <div class="login-card login-card--demo">
      <PageHeader title="演示账号（本地 Mock）" />
      <van-cell-group>
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
        正式环境请用学校统一身份（CAS）或教务下发的账号登录； 管理员请使用后台管理端（admin 工程）。
      </p>
    </div>
  </div>
</template>

<style scoped>
.login {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px 16px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 50%, #93c5fd 100%);
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 32px 24px 20px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 12px 32px rgb(0 0 0 / 15%);
}

.login-card--demo {
  padding: 16px;
  margin-top: 16px;
}

.login-card__header {
  margin-bottom: 24px;
  text-align: center;
}

.login-card__title {
  margin: 0;
  font-size: 22px;
  color: var(--st-text);
}

.login-card__subtitle {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--st-text-light);
}

.login-card__actions {
  margin-top: 16px;
}

.login__tip {
  margin: 12px 4px 0;
  line-height: 1.6;
}
</style>
