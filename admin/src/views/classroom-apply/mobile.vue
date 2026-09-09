<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  cancelClassroomApply,
  getMyClassroomApplies,
  type ClassroomApplyRecord,
} from '@/api/classroom'

const router = useRouter()

/** 提交后 N 毫秒内不可取消 */
const CANCEL_LOCK_MS = 30 * 60 * 1000

const statusTabs = [
  { name: 'all', label: '全部' },
  { name: '待审核', label: '待审核' },
  { name: '已通过', label: '已通过' },
  { name: '已驳回', label: '已驳回' },
  { name: '已取消', label: '已取消' },
] as const

type StatusName = (typeof statusTabs)[number]['name']

/* ==================== 列表状态 ==================== */

const activeTab = ref<StatusName>('all')
const list = ref<ClassroomApplyRecord[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const errorMsg = ref('')

/** 用于驱动“剩余锁定时间”文本刷新 */
const tick = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

/* ==================== 取消确认弹窗 ==================== */

const cancelTarget = ref<ClassroomApplyRecord | null>(null)
const cancelVisible = ref(false)
const cancelling = ref(false)

const tagTypeByStatus: Record<ClassroomApplyRecord['status'], 'primary' | 'success' | 'danger' | 'warning' | 'default'> = {
  待审核: 'warning',
  已通过: 'success',
  已驳回: 'danger',
  已取消: 'default',
}

/** 是否处于“30 分钟内不可取消”锁定 */
function isLocked(record: ClassroomApplyRecord) {
  const created = new Date(record.applyTime.replace(/-/g, '/')).getTime()
  return Date.now() - created < CANCEL_LOCK_MS
}

/** 取消锁定剩余时间（mm:ss） */
function lockRemainText(record: ClassroomApplyRecord) {
  const created = new Date(record.applyTime.replace(/-/g, '/')).getTime()
  const remain = Math.max(0, Math.ceil((CANCEL_LOCK_MS - (Date.now() - created)) / 1000))
  const mm = String(Math.floor(remain / 60)).padStart(2, '0')
  const ss = String(remain % 60).padStart(2, '0')
  return `${mm}:${ss}`
}

/** 该记录是否展示“取消申请”按钮（仅待审核可取消） */
function canCancel(record: ClassroomApplyRecord) {
  return record.status === '待审核'
}

function askCancel(record: ClassroomApplyRecord) {
  if (isLocked(record)) return
  cancelTarget.value = record
  cancelVisible.value = true
}

async function confirmCancel() {
  const record = cancelTarget.value
  if (!record) return
  cancelling.value = true
  try {
    await cancelClassroomApply(record.id)
    // 乐观更新：本地状态置为“已取消”
    record.status = '已取消'
    reload()
  } catch {
    // 失败时刷新列表，还原服务端真实状态
    reload()
  } finally {
    cancelVisible.value = false
    cancelTarget.value = null
    cancelling.value = false
  }
}

/* ==================== 列表加载（van-list 无限滚动） ==================== */

async function loadPage() {
  if (loading.value) return
  loading.value = true
  try {
    const result = await getMyClassroomApplies({
      page: page.value,
      pageSize: 10,
      status: activeTab.value === 'all' ? undefined : activeTab.value,
    })
    list.value = page.value === 1 ? result.list : [...list.value, ...result.list]
    total.value = result.total
    finished.value = list.value.length >= result.total
    page.value += 1
    errorMsg.value = ''
  } catch {
    errorMsg.value = '加载失败，请下拉重试'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/** 切换页签 / 取消成功后：重置并重新加载 */
function reload() {
  page.value = 1
  list.value = []
  finished.value = false
  loadPage()
}

function onLoad() {
  loadPage()
}

function onRefresh() {
  refreshing.value = true
  reload()
}

function onTabChange() {
  reload()
}

onMounted(() => {
  loadPage()
  // 每秒刷新一次剩余锁定时间
  timer = setInterval(() => {
    tick.value += 1
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="cr-mobile">
    <van-nav-bar
      title="教室申请"
      left-text="返回"
      fixed
      placeholder
      safe-area-inset-top
      @click-left="router.back()"
    />

    <van-tabs v-model:active="activeTab" sticky offset-top="46px" @change="onTabChange">
      <van-tab v-for="tab in statusTabs" :key="tab.name" :name="tab.name" :title="tab.label" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        :error="Boolean(errorMsg)"
        error-text="请求失败，点击重新加载"
        @load="onLoad"
        @error="onLoad"
      >
        <div v-for="record in list" :key="record.id" class="cr-card">
          <div class="cr-card__head">
            <div class="cr-card__room">
              <van-icon name="location-o" />
              {{ record.roomName }}
            </div>
            <van-tag :type="tagTypeByStatus[record.status]" round>{{ record.status }}</van-tag>
          </div>

          <div class="cr-card__row">
            <span class="cr-card__label">使用时间</span>
            <span>{{ record.date }} {{ record.timeSlot }}</span>
          </div>
          <div class="cr-card__row">
            <span class="cr-card__label">用途</span>
            <span>{{ record.purpose }}</span>
          </div>
          <div class="cr-card__row cr-card__row--reason">
            <span class="cr-card__label">事由</span>
            <span>{{ record.reason }}</span>
          </div>

          <div class="cr-card__foot">
            <span class="cr-card__time">
              提交于 {{ record.applyTime }}
              <van-tag v-if="canCancel(record) && isLocked(record)" type="warning" plain>
                30分钟锁定中 {{ lockRemainText(record) }}
              </van-tag>
            </span>
            <van-button
              v-if="canCancel(record)"
              size="small"
              plain
              type="danger"
              :disabled="isLocked(record)"
              :loading="cancelling && cancelTarget?.id === record.id"
              @click="askCancel(record)"
            >
              {{ isLocked(record) ? '暂不可取消' : '取消申请' }}
            </van-button>
          </div>
        </div>

        <van-empty v-if="!loading && list.length === 0" description="暂无申请记录" />
      </van-list>
    </van-pull-refresh>

    <div class="cr-mobile__tip">
      规则提示：教室申请提交后 <b>30 分钟</b> 内不可取消，超时后可在“待审核”列表中取消。
    </div>

    <!-- 取消确认 -->
    <van-dialog
      v-model:show="cancelVisible"
      title="取消教室申请"
      show-cancel-button
      @confirm="confirmCancel"
    >
      <div class="cr-dialog-body">
        <p>确定取消以下申请吗？</p>
        <p v-if="cancelTarget" class="cr-dialog-body__detail">
          {{ cancelTarget.roomName }} · {{ cancelTarget.date }} {{ cancelTarget.timeSlot }}
        </p>
        <p class="cr-dialog-body__tip">取消后如需使用该教室，请重新提交申请。</p>
      </div>
    </van-dialog>
  </div>
</template>

<style scoped>
.cr-mobile {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 24px;
}

.cr-card {
  margin: 10px 12px 0;
  padding: 12px 14px;
  background: #fff;
  border-radius: 10px;
}

.cr-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid #f2f3f5;
}

.cr-card__room {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 15px;
  font-weight: 600;
  color: #323233;
}

.cr-card__row {
  display: flex;
  align-items: flex-start;
  font-size: 13px;
  color: #646566;
  margin-top: 7px;
  line-height: 1.5;
}

.cr-card__row--reason {
  align-items: flex-start;
}

.cr-card__label {
  flex: 0 0 58px;
  color: #969799;
}

.cr-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid #f2f3f5;
}

.cr-card__time {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #969799;
}

.cr-mobile__tip {
  margin: 16px 12px 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fef7e6;
  color: #8a6d1f;
  font-size: 12px;
  line-height: 1.6;
}

.cr-dialog-body {
  padding: 4px 4px 8px;
  font-size: 14px;
  color: #323233;
  line-height: 1.7;
}

.cr-dialog-body__detail {
  color: #576b95;
}

.cr-dialog-body__tip {
  color: #969799;
  font-size: 12px;
}
</style>
