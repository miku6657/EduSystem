<script setup lang="ts">
import {
  computed,
  onMounted,
  ref,
} from 'vue'

import {
  showToast,
} from 'vant'

import {
  checkIn,
  listMyRecentAttendance,
  statByDate,
} from '@/api/teacherAttendance'

import type {
  TeacherAttendanceRecord,
  TeacherAttendanceStat,
} from '@/api/teacherAttendance'

import {
  useUserStore,
} from '@/stores/user'

import PageHeader
  from '@/components/PageHeader.vue'

import StatBar
  from '@/components/StatBar.vue'

import {
  ATTENDANCE_STATUS_TYPE,
} from '@/constants/dict'

import {
  addDays,
  todayStr,
} from '@/utils/format'

const userStore =
  useUserStore()

/**
 * 真正的base_teacher.id
 */
const teacherId =
  computed(
    () =>
      userStore.businessId,
  )

const today =
  todayStr()

const loading =
  ref(false)

const submitting =
  ref(false)

const errorMessage =
  ref('')

const records =
  ref<
    TeacherAttendanceRecord[]
  >([])

const stat =
  ref<
    TeacherAttendanceStat
  >({})

/**
 * 最近7天。
 */
const recentDates =
  computed(() =>
    Array.from(
      {
        length: 7,
      },
      (_, index) =>
        addDays(
          today,
          -index,
        ),
    ),
  )

/**
 * 今日本人记录。
 */
const myToday =
  computed(() =>
    records.value.find(
      (item) =>
        item.attendanceDate
        === today,
    ),
  )

const recent =
  computed(() => {
    const map =
      new Map(
        records.value.map(
          (item) => [
            item.attendanceDate,
            item,
          ],
        ),
      )

    return recentDates.value.map(
      (date) => {
        const record =
          map.get(date)

        return {
          date,

          status:
            record?.status
            ?? '',

          checkTime:
            record?.checkTime
            ?? '',
        }
      },
    )
  })

const summaryItems =
  computed(() => [
    {
      label:
        '教师总数',

      value:
        Number(
          stat.value.total
          ?? 0,
        ),
    },

    {
      label:
        '已签到',

      value:
        Number(
          stat.value.checked
          ?? 0,
        ),
    },

    {
      label:
        '未签到',

      value:
        Number(
          stat.value.absent
          ?? 0,
        ),
    },
  ])

const checkButtonText =
  computed(() =>
    myToday.value
      ? '今日已签到'
      : '立即签到',
  )

function timeText(
  value?: string,
) {
  if (!value) {
    return ''
  }

  return value
    .replace(
      'T',
      ' ',
    )
    .slice(
      11,
      16,
    )
}

async function loadData() {
  if (
    !teacherId.value
  ) {
    errorMessage.value =
      '当前教师身份未绑定'

    return
  }

  loading.value =
    true

  errorMessage.value =
    ''

  try {
    const [
      myRecords,
      todayStat,
    ] =
      await Promise.all([
        listMyRecentAttendance(
          teacherId.value,
          recentDates.value,
        ),

        statByDate(
          today,
        ),
      ])

    records.value =
      myRecords

    stat.value =
      todayStat
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : '考勤数据加载失败'
  } finally {
    loading.value =
      false
  }
}

async function onCheckIn() {
  if (
    !teacherId.value
  ) {
    showToast(
      '当前教师身份未绑定',
    )

    return
  }

  submitting.value =
    true

  try {
    await checkIn(
      teacherId.value,
    )

    showToast(
      '签到成功',
    )

    await loadData()
  } catch {
    // request.ts统一处理
  } finally {
    submitting.value =
      false
  }
}

onMounted(
  loadData,
)
</script>

<template>
  <div>
    <div class="st-card">
      <PageHeader
        title="我的签到"
      >
        <template #actions>
          <van-button
            size="small"
            type="primary"
            :loading="submitting"
            :disabled="
              Boolean(myToday)
            "
            @click="onCheckIn"
          >
            {{
              checkButtonText
            }}
          </van-button>
        </template>
      </PageHeader>

      <div class="check__today">
        <div class="check__date">
          {{ today }}
        </div>

        <div class="check__status">
          <van-tag
            v-if="myToday"
            size="large"
            :type="
              ATTENDANCE_STATUS_TYPE[
                myToday.status
              ]
              || 'success'
            "
          >
            {{
              myToday.status
            }}
          </van-tag>

          <van-tag
            v-else
            size="large"
            type="warning"
          >
            未签到
          </van-tag>
        </div>

        <div class="st-muted">
          <template v-if="myToday">
            签到时间
            {{
              timeText(
                myToday.checkTime,
              )
              || '—'
            }}
          </template>

          <template v-else>
            今日暂无签到记录
          </template>
        </div>
      </div>

      <StatBar
        :items="summaryItems"
      />
    </div>

    <van-loading
      v-if="loading"
      vertical
      class="check__loading"
    >
      正在加载考勤数据
    </van-loading>

    <van-empty
      v-else-if="
        errorMessage
      "
      :description="
        errorMessage
      "
    >
      <van-button
        round
        size="small"
        type="primary"
        @click="loadData"
      >
        重新加载
      </van-button>
    </van-empty>

    <template v-else>
      <div class="st-section-title">
        我最近 7 天的记录
      </div>

      <div
        v-for="row in recent"
        :key="row.date"
        class="st-card st-row"
      >
        <span>
          {{ row.date }}
        </span>

        <span class="check__recent-right">
          <van-tag
            v-if="row.status"
            plain
            :type="
              ATTENDANCE_STATUS_TYPE[
                row.status
              ]
              || 'primary'
            "
          >
            {{ row.status }}
          </van-tag>

          <van-tag
            v-else
            plain
          >
            未签到
          </van-tag>

          <span class="st-muted">
            {{
              timeText(
                row.checkTime,
              )
            }}
          </span>
        </span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.check__today {
  margin-bottom: 12px;
  text-align: center;
}

.check__date {
  font-size: 14px;
  font-weight: 600;
}

.check__status {
  margin: 10px 0 6px;
}

.check__recent-right {
  display: flex;
  gap: 6px;
  align-items: center;
}

.check__loading {
  padding: 40px 0;
}
</style>
