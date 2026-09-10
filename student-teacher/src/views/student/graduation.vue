<script setup lang="ts">
/**
 * 学生 · 毕业资格
 * 数据源：GET /api/graduate-check/by-student/{studentId}（可能返回 null：还没有审核记录）
 */
import { computed, onMounted, ref } from 'vue'
import { getMyGraduateCheck } from '@/api/graduation'
import type { GraduateCheck } from '@/api/graduation'
import { useUserStore } from '@/stores/user'
import { useAsyncData } from '@/composables/useAsyncData'
import { formatDateTime } from '@/utils/format'
import { AUDIT_STATUS_TEXT, AUDIT_STATUS_TYPE, PASS_FAIL_TEXT, dictText } from '@/constants/dict'

const userStore = useUserStore()
const refreshing = ref(false)

const { data: check, loading, error, reload } = useAsyncData<GraduateCheck | null>(
  () => (userStore.businessId ? getMyGraduateCheck(userStore.businessId) : Promise.resolve(null)),
  null,
)

/** 结论：PASS 通过 / FAIL 未通过 / WAIT 审核中（其余情况按审核中展示） */
const conclusion = computed(() => {
  const status = check.value?.checkStatus
  if (status === 'PASS') {
    return { theme: 'pass', title: '已通过毕业资格审核', desc: '学分与课程要求均已满足，请按教务处通知办理毕业手续。' }
  }
  if (status === 'FAIL') {
    return { theme: 'fail', title: '未通过毕业资格审核', desc: '请尽快参加补考或重修，成绩合格后由教务重新审核。' }
  }
  return { theme: 'wait', title: '审核中，请耐心等待', desc: '教务正在审核你的毕业资格，结果更新后会显示在本页。' }
})

const isFail = computed(() => check.value?.checkStatus === 'FAIL')
const statusText = computed(() => dictText(AUDIT_STATUS_TEXT, check.value?.checkStatus))
const statusType = computed(() => AUDIT_STATUS_TYPE[check.value?.checkStatus ?? ''] || 'primary')
/** 未审核（null）的字段统一显示 — */
const remarkText = computed(() => check.value?.remark || '—')

/** 明细行：学分审核 / 课程审核 / 审核人 / 更新时间 */
const detailItems = computed(() => {
  const row = check.value
  if (!row) {
    return []
  }
  return [
    { label: '学分审核', value: dictText(PASS_FAIL_TEXT, row.creditStatus) },
    { label: '课程审核', value: dictText(PASS_FAIL_TEXT, row.courseStatus) },
    { label: '审核人', value: row.checker || '—' },
    { label: '更新时间', value: formatDateTime(row.updateTime) },
  ]
})

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
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-empty v-if="!userStore.businessId" description="未解析到学号，请确认登录账号为学号" />

      <div v-else-if="loading && !refreshing" class="st-empty">
        <van-loading vertical>加载中…</van-loading>
      </div>

      <van-empty v-else-if="error" image="error" :description="error">
        <van-button round type="primary" size="small" @click="reload">重新加载</van-button>
      </van-empty>

      <van-empty v-else-if="!check" description="暂无毕业资格审核记录" />

      <template v-else>
        <!-- 结论卡：绿=通过 / 红=未通过 / 橙=审核中 -->
        <div class="st-card graduate__result" :class="`graduate__result--${conclusion.theme}`">
          <div class="graduate__result-title">{{ conclusion.title }}</div>
          <div class="graduate__result-desc">{{ conclusion.desc }}</div>
          <van-tag :type="statusType">{{ statusText }}</van-tag>
        </div>

        <van-notice-bar
          v-if="isFail"
          type="danger"
          left-icon="warning-o"
          wrapable
          :scrollable="false"
          text="请尽快参加补考或重修：不及格课程全部合格后，由教务处重新审核毕业资格。"
        />

        <div class="st-section-title">审核明细</div>
        <div class="st-card">
          <div v-for="item in detailItems" :key="item.label" class="st-row graduate__detail">
            <span class="st-muted">{{ item.label }}</span>
            <span>{{ item.value }}</span>
          </div>
          <div class="graduate__remark">
            <div class="st-muted">备注</div>
            <div class="graduate__remark-text">{{ remarkText }}</div>
          </div>
        </div>
      </template>
    </van-pull-refresh>

    <!-- 静态说明：毕业条件 -->
    <div class="st-section-title">毕业条件</div>
    <div class="st-card graduate__rules st-muted">
      <p>1. 修满专业培养方案规定的全部学分（含必修、选修与集中实践环节）；</p>
      <p>2. 所有课程考核合格，无未处理的缺考、违纪记录；</p>
      <p>3. 完成毕业设计 / 毕业论文并通过答辩（或顶岗实习鉴定合格）；</p>
      <p>4. 在校期间无严重违纪处分，德育与体质测试合格；</p>
      <p>5. 以上条件由教务处审核，最终结论以本页审核明细为准。</p>
    </div>
  </div>
</template>

<style scoped>
.graduate__result { border-left: 4px solid var(--st-text-light); }

.graduate__result--pass { background: #f0fff4; border-left-color: #07c160; }

.graduate__result--fail { background: #fff5f5; border-left-color: #ee0a24; }

.graduate__result--wait { background: #fffaf5; border-left-color: #ff976a; }

.graduate__result-title { font-size: 20px; font-weight: 600; }

.graduate__result--pass .graduate__result-title { color: #07c160; }

.graduate__result--fail .graduate__result-title { color: #ee0a24; }

.graduate__result--wait .graduate__result-title { color: #ff976a; }

.graduate__result-desc { margin: 6px 0 8px; font-size: 13px; line-height: 1.6; color: var(--st-text-light); }

.graduate__detail { margin-bottom: 6px; }

.graduate__remark { margin-top: 8px; padding-top: 8px; border-top: 1px solid var(--st-border); }

.graduate__remark-text { margin-top: 4px; line-height: 1.5; }

.graduate__rules p { margin: 0 0 6px; line-height: 1.6; }

.graduate__rules p:last-child { margin-bottom: 0; }
</style>
