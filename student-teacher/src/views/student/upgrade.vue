<script setup lang="ts">
/**
 * 学生 · 专升本报名
 * 数据源：GET /api/upgrade-apply/my/list?studentId（缺口接口，见 api/upgrade.ts 注释）
 * 提交：POST /api/upgrade-apply/apply，body 为 UpgradeApply
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import { listMyUpgradeApplies, submitUpgradeApply } from '@/api/upgrade'
import type { UpgradeApply } from '@/api/upgrade'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import { formatDateTime } from '@/utils/format'
import { AUDIT_STATUS_TEXT, AUDIT_STATUS_TYPE, dictText } from '@/constants/dict'

const userStore = useUserStore()
const refreshing = ref(false)
const showForm = ref(false)
const submitting = ref(false)

/** 报名表单：院校 / 专业必填，备注选填 */
const form = reactive({ schoolName: '', majorName: '', remark: '' })

const { data: applies, loading, error, reload } = useAsyncData<UpgradeApply[]>(
  () => (userStore.businessId ? listMyUpgradeApplies(userStore.businessId) : Promise.resolve([])),
  [],
)

/** 是否已有待审核的报名（Mock/后端都会拒绝重复报名，前端提前提示） */
const hasPending = computed(() => applies.value.some((row) => row.applyStatus === 'WAIT'))

/** 状态文案：WAIT 待审核 / PASS 已通过 / FAIL 已驳回 */
function statusTextOf(row: UpgradeApply): string {
  return dictText(AUDIT_STATUS_TEXT, row.applyStatus)
}

/** 状态标签颜色 */
function statusTypeOf(row: UpgradeApply): 'primary' | 'success' | 'danger' | 'warning' {
  return AUDIT_STATUS_TYPE[row.applyStatus ?? ''] || 'primary'
}

function openForm() {
  if (!userStore.businessId) {
    showToast('未解析到学号，请确认登录账号为学号')
    return
  }
  if (hasPending.value) {
    showToast('已有待审核的报名，请等待审核结果')
    return
  }
  showForm.value = true
}

function resetForm() {
  form.schoolName = ''
  form.majorName = ''
  form.remark = ''
}

/** 提交报名：必填校验交给 van-form rules；业务失败（如已有待审核报名）由请求层统一 toast */
async function onSubmit() {
  const studentId = userStore.businessId
  if (!studentId) {
    showToast('未解析到学号，请确认登录账号为学号')
    return
  }
  submitting.value = true
  try {
    await submitUpgradeApply({
      studentId,
      schoolName: form.schoolName.trim(),
      majorName: form.majorName.trim(),
      remark: form.remark.trim() || undefined,
    })
    showToast('报名已提交')
    showForm.value = false
    resetForm()
    await reload()
  } catch {
    // 请求层已 toast，不重复提示
  } finally {
    submitting.value = false
  }
}

async function onRefresh() {
  refreshing.value = true
  try {
    await reload()
  } finally {
    refreshing.value = false
  }
}

onMounted(reload)
</script>

<template>
  <div>
    <!-- 报名条件说明（静态） -->
    <div class="st-card">
      <div class="upgrade__title">报名条件说明</div>
      <ul class="upgrade__conditions st-muted">
        <li>在校期间无违纪处分记录，德育考核合格；</li>
        <li>已修满专业培养方案规定的学分，无未通过课程；</li>
        <li>符合接收院校及报考专业的报名要求（专业对口或相近）；</li>
        <li>同一时间只能有一条待审核的报名，审核通过后不可重复提交。</li>
      </ul>
    </div>

    <van-button class="upgrade__actions" round block type="primary" @click="openForm">
      我要报名
    </van-button>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-empty v-if="!userStore.businessId" description="未解析到学号，请确认登录账号为学号" />

      <div v-else-if="loading && !refreshing" class="st-empty">
        <van-loading vertical>加载中…</van-loading>
      </div>

      <van-empty v-else-if="error" image="error" :description="error">
        <van-button round type="primary" size="small" @click="reload">重新加载</van-button>
      </van-empty>

      <van-empty v-else-if="applies.length === 0" description="暂无专升本报名记录" />

      <template v-else>
        <div v-for="row in applies" :key="row.id ?? `${row.schoolName}-${row.majorName}`" class="st-card">
          <div class="st-row">
            <div class="upgrade__school">{{ row.schoolName || '—' }}</div>
            <van-tag :type="statusTypeOf(row)">{{ statusTextOf(row) }}</van-tag>
          </div>
          <div class="st-row upgrade__line">
            <span class="st-muted">报考专业</span>
            <span>{{ row.majorName || '—' }}</span>
          </div>
          <div class="st-row upgrade__line">
            <span class="st-muted">提交时间</span>
            <span class="st-muted">{{ formatDateTime(row.createTime) }}</span>
          </div>
          <div v-if="row.remark" class="upgrade__remark st-muted">备注：{{ row.remark }}</div>
        </div>
      </template>
    </van-pull-refresh>

    <!-- 报名表单 -->
    <van-popup v-model:show="showForm" position="bottom" round>
      <van-form @submit="onSubmit">
        <div class="upgrade__popup-title">专升本报名</div>
        <van-cell-group inset>
          <van-field
            v-model="form.schoolName"
            name="schoolName"
            label="报考院校"
            placeholder="请输入报考院校"
            clearable
            :rules="[{ required: true, message: '请填写报考院校' }]"
          />
          <van-field
            v-model="form.majorName"
            name="majorName"
            label="报考专业"
            placeholder="请输入报考专业"
            clearable
            :rules="[{ required: true, message: '请填写报考专业' }]"
          />
          <van-field
            v-model="form.remark"
            name="remark"
            label="备注"
            type="textarea"
            :rows="2"
            :maxlength="100"
            show-word-limit
            placeholder="选填，如获奖情况、技能证书等"
          />
        </van-cell-group>
        <div class="upgrade__popup-actions">
          <van-button round block type="primary" native-type="submit" :loading="submitting">提交报名</van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<style scoped>
.upgrade__title { margin-bottom: 8px; font-size: 15px; font-weight: 600; }

.upgrade__conditions { padding-left: 18px; margin: 0; line-height: 1.7; }

.upgrade__conditions li { margin-bottom: 2px; }

.upgrade__actions { margin-bottom: 12px; }

.upgrade__school { font-size: 15px; font-weight: 600; }

.upgrade__line { margin-top: 4px; }

.upgrade__remark { margin-top: 8px; line-height: 1.5; }

.upgrade__popup-title { padding: 14px 16px 6px; font-size: 16px; font-weight: 600; text-align: center; }

.upgrade__popup-actions { padding: 8px 16px 20px; }
</style>
