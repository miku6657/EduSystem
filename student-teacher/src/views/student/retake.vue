<script setup lang="ts">
import {
  computed,
  onMounted,
  ref,
} from 'vue'

import {
  showConfirmDialog,
  showToast,
} from 'vant'

import {
  applyRetake,
  listFailedCourses,
  listMyRetakes,
} from '@/api/retake'

import type {
  ExamRetake,
  FailedCourse,
  RetakeStatus,
} from '@/api/retake'

import {
  useUserStore,
} from '@/stores/user'

import PageHeader
  from '@/components/PageHeader.vue'

import StatBar
  from '@/components/StatBar.vue'

const userStore =
  useUserStore()

const failedCourses =
  ref<FailedCourse[]>([])

const retakes =
  ref<ExamRetake[]>([])

const loading =
  ref(false)

const refreshing =
  ref(false)

const submittingCourseId =
  ref('')

const activeTab =
  ref(0)

const studentId =
  computed(
    () =>
      userStore.businessId
      ?? '',
  )

/**
 * 正在处理中的申请。
 *
 * REJECTED / COMPLETED
 * 不阻止再次申请。
 */
const activeRetakeMap =
  computed(() => {

    const map =
      new Map<
        string,
        ExamRetake
      >()

    for (
      const row
      of retakes.value
    ) {

      if (
        row.status === 'WAIT'
        ||
        row.status === 'APPROVED'
        ||
        row.status === 'ARRANGED'
      ) {

        map.set(
          row.courseId,
          row,
        )
      }
    }

    return map
  })

const summaryItems =
  computed(
    () => [
      {
        label:
          '挂科课程',

        value:
          failedCourses.value
            .length,
      },

      {
        label:
          '待审批',

        value:
          retakes.value.filter(
            row =>
              row.status
              === 'WAIT',
          ).length,
      },

      {
        label:
          '待考试',

        value:
          retakes.value.filter(
            row =>
              row.status
              === 'ARRANGED',
          ).length,
      },
    ],
  )

async function loadData() {

  if (!studentId.value) {
    return
  }

  loading.value =
    true

  try {

    const [
      failed,
      records,
    ] =
      await Promise.all([
        listFailedCourses(
          studentId.value,
        ),

        listMyRetakes(
          studentId.value,
        ),
      ])

    failedCourses.value =
      failed

    retakes.value =
      records

  } catch {
    // request.ts统一提示
  } finally {

    loading.value =
      false
  }
}

function statusText(
  status: RetakeStatus,
) {

  const map:
    Record<
      RetakeStatus,
      string
    > = {
      WAIT:
        '待审批',

      APPROVED:
        '已批准，待安排',

      REJECTED:
        '已驳回',

      ARRANGED:
        '已安排考试',

      COMPLETED:
        '重修完成',
    }

  return map[status]
}

function statusType(
  status: RetakeStatus,
):
  | 'primary'
  | 'success'
  | 'warning'
  | 'danger'
  | 'default' {

  if (
    status === 'COMPLETED'
  ) {
    return 'success'
  }

  if (
    status === 'REJECTED'
  ) {
    return 'danger'
  }

  if (
    status === 'WAIT'
    ||
    status === 'APPROVED'
  ) {
    return 'warning'
  }

  return 'primary'
}

async function onApply(
  course: FailedCourse,
) {

  if (!studentId.value) {
    showToast(
      '当前账号未绑定学生档案',
    )

    return
  }

  /**
   * 页面上也阻止重复提交。
   * 后端还有第二层校验。
   */
  const active =
    activeRetakeMap.value.get(
      course.courseId,
    )

  if (active) {

    showToast(
      statusText(
        active.status,
      ),
    )

    return
  }

  try {

    await showConfirmDialog({
      title:
        '申请重修',

      message:
        `「${course.courseName}」当前成绩 ${course.score} 分，确定申请重修吗？`,
    })

  } catch {
    return
  }

  submittingCourseId.value =
    course.courseId

  try {

    await applyRetake(
      studentId.value,
      course.courseId,
    )

    showToast(
      '重修申请已提交',
    )

    await loadData()

    activeTab.value =
      1

  } catch {
    // 后端重复申请、
    // 成绩已及格等异常统一提示
  } finally {

    submittingCourseId.value =
      ''
  }
}

async function onRefresh() {

  refreshing.value =
    true

  try {

    await loadData()

  } finally {

    refreshing.value =
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
        title="补考重修"
      />

      <StatBar
        :items="summaryItems"
      />
    </div>

    <van-tabs
      v-model:active="
        activeTab
      "
    >
      <van-tab
        title="挂科课程"
      />

      <van-tab
        title="申请记录"
      />
    </van-tabs>

    <van-pull-refresh
      v-model="
        refreshing
      "
      @refresh="
        onRefresh
      "
    >
      <div
        v-if="loading"
        class="st-card retake__loading"
      >
        <van-loading
          vertical
        >
          加载中
        </van-loading>
      </div>

      <!-- 挂科课程 -->
      <template
        v-else-if="
          activeTab === 0
        "
      >
        <van-empty
          v-if="
            failedCourses.length
            === 0
          "
          description="当前没有挂科课程"
        />

        <div
          v-for="
            course
            in failedCourses
          "
          :key="
            course.courseId
          "
          class="st-card"
        >
          <div class="st-row">
            <div>
              <div
                class="
                  retake__course
                "
              >
                {{
                  course.courseName
                }}
              </div>

              <div
                class="
                  st-muted
                  retake__code
                "
              >
                {{
                  course.courseCode
                  || '—'
                }}

                <span
                  v-if="
                    course.credit
                    !== undefined
                  "
                >
                  ·
                  {{
                    course.credit
                  }}
                  学分
                </span>
              </div>
            </div>

            <div
              class="
                retake__score
              "
            >
              {{
                course.score
              }}
              分
            </div>
          </div>

          <div
            class="
              retake__actions
            "
          >
            <template
              v-if="
                activeRetakeMap
                  .get(
                    course.courseId,
                  )
              "
            >
              <van-tag
                type="warning"
              >
                {{
                  statusText(
                    activeRetakeMap
                      .get(
                        course.courseId,
                      )!
                      .status
                  )
                }}
              </van-tag>
            </template>

            <van-button
              v-else
              size="small"
              type="primary"
              :loading="
                submittingCourseId
                ===
                course.courseId
              "
              @click="
                onApply(course)
              "
            >
              申请重修
            </van-button>
          </div>
        </div>
      </template>

      <!-- 申请记录 -->
      <template
        v-else
      >
        <van-empty
          v-if="
            retakes.length === 0
          "
          description="暂无重修申请记录"
        />

        <div
          v-for="
            row in retakes
          "
          :key="row.id"
          class="st-card"
        >
          <div class="st-row">
            <div>
              <div
                class="
                  retake__course
                "
              >
                {{
                  row.courseName
                  || '未知课程'
                }}
              </div>

              <div
                class="
                  st-muted
                  retake__code
                "
              >
                {{
                  row.courseCode
                  || '—'
                }}
              </div>
            </div>

            <van-tag
              :type="
                statusType(
                  row.status,
                )
              "
            >
              {{
                statusText(
                  row.status,
                )
              }}
            </van-tag>
          </div>

          <div
            v-if="
              row.examName
            "
            class="
              retake__exam
              st-muted
            "
          >
            考试：
            {{
              row.examName
            }}

            <span
              v-if="
                row.examDate
              "
            >
              ·
              {{
                row.examDate
              }}
            </span>

            <span
              v-if="
                row.startTime
                && row.endTime
              "
            >
              ·
              {{
                row.startTime
              }}
              -
              {{
                row.endTime
              }}
            </span>
          </div>

          <div
            v-else
            class="
              retake__exam
              st-muted
            "
          >
            {{
              row.status === 'WAIT'
                ? '等待管理员审批'
                : row.status === 'APPROVED'
                  ? '审批已通过，等待安排考试'
                  : row.status === 'REJECTED'
                    ? '申请未通过'
                    : '暂无考试信息'
            }}
          </div>
        </div>
      </template>
    </van-pull-refresh>
  </div>
</template>

<style scoped>
.retake__loading {
  padding: 35px 0;
}

.retake__course {
  font-size: 15px;
  font-weight: 600;
}

.retake__code {
  margin-top: 4px;
  font-size: 12px;
}

.retake__score {
  color: var(--st-danger);
  font-size: 20px;
  font-weight: 700;
}

.retake__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.retake__exam {
  margin-top: 10px;
  line-height: 1.6;
}
</style>