<script setup lang="ts">
import {
  computed,
  onMounted,
  ref,
} from 'vue'

import {
  getMyGraduationEligibility,
} from '@/api/graduation'

import type {
  GraduationEligibility,
} from '@/api/graduation'

import {
  useUserStore,
} from '@/stores/user'

import {
  useAsyncData,
} from '@/composables/useAsyncData'

import PageHeader
  from '@/components/PageHeader.vue'

import PageState
  from '@/components/PageState.vue'

import StatBar
  from '@/components/StatBar.vue'

const userStore =
  useUserStore()

const refreshing =
  ref(false)

/**
 * businessId
 * =
 * base_student.id
 */
const studentId =
  computed(
    () =>
      userStore.businessId,
  )

const {
  data: result,
  loading,
  error,
  reload,
} =
  useAsyncData<
    GraduationEligibility | null
  >(
    () =>
      studentId.value
        ? getMyGraduationEligibility(
            studentId.value,
          )
        : Promise.resolve(
            null,
          ),
    null,
  )

/**
 * 顶部统计。
 */
const summaryItems =
  computed(
    () => [
      {
        label:
          '已录入成绩',

        value:
          result.value
            ?.total
          ?? 0,
      },

      {
        label:
          '及格',

        value:
          result.value
            ?.passed
          ?? 0,
      },

      {
        label:
          '不及格',

        value:
          result.value
            ?.failed
          ?? 0,
      },
    ],
  )

/**
 * 毕业结论。
 */
const conclusion =
  computed(() => {

    if (
      !result.value
    ) {
      return null
    }

    if (
      result.value.eligible
    ) {
      return {
        theme:
          'pass',

        title:
          '符合毕业资格',

        description:
          '当前不存在低于 60 分的课程成绩。',
      }
    }

    return {
      theme:
        'fail',

      title:
        '暂不符合毕业资格',

      description:
        `当前有 ${result.value.failed} 门课程成绩低于 60 分，请完成补考或重修。`,
    }
  })

async function onRefresh() {

  refreshing.value =
    true

  try {

    await reload()

  } finally {

    refreshing.value =
      false
  }
}

onMounted(
  reload,
)
</script>

<template>
  <div>
    <!-- 标题 -->
    <div class="st-card">
      <PageHeader
        title="毕业资格"
      />
    </div>

    <van-pull-refresh
      v-model="
        refreshing
      "
      @refresh="
        onRefresh
      "
    >
      <PageState
        :loading="
          loading
          && !refreshing
          && !!studentId
        "
        :error="
          error
        "
        :empty="
          !studentId
          || !result
        "
        :empty-text="
          studentId
            ? '暂无毕业资格数据'
            : '当前账号未绑定学生档案'
        "
        @retry="
          reload
        "
      >
        <!-- 毕业结论 -->
        <div
          v-if="
            conclusion
          "
          class="
            st-card
            graduate__result
          "
          :class="
            `graduate__result--${conclusion.theme}`
          "
        >
          <div
            class="
              graduate__result-title
            "
          >
            {{
              conclusion.title
            }}
          </div>

          <div
            class="
              graduate__result-desc
            "
          >
            {{
              conclusion.description
            }}
          </div>

          <van-tag
            :type="
              result?.eligible
                ? 'success'
                : 'danger'
            "
            size="medium"
          >
            {{
              result?.eligible
                ? '可以毕业'
                : '不可毕业'
            }}
          </van-tag>
        </div>

        <!-- 统计 -->
        <div class="st-card">
          <StatBar
            :items="
              summaryItems
            "
          />
        </div>

        <!-- 不及格课程 -->
        <template
          v-if="
            result
            && !result.eligible
          "
        >
          <div
            class="
              st-section-title
            "
          >
            不及格课程
          </div>

          <div
            v-for="
              item in
              result.failedCourses
            "
            :key="
              `${item.examId}-${item.courseId}`
            "
            class="
              st-card
              graduate__course
            "
          >
            <div
              class="
                st-row
                graduate__course-head
              "
            >
              <div>
                <div
                  class="
                    graduate__course-name
                  "
                >
                  {{
                    item.courseName
                    || '未知课程'
                  }}
                </div>

                <div
                  v-if="
                    item.courseCode
                  "
                  class="
                    st-muted
                    graduate__course-code
                  "
                >
                  {{
                    item.courseCode
                  }}
                </div>
              </div>

              <div
                class="
                  graduate__score
                "
              >
                {{
                  item.score
                }}
                分
              </div>
            </div>

            <div
              v-if="
                item.examName
              "
              class="
                st-muted
                graduate__exam
              "
            >
              {{
                item.examName
              }}

              <span
                v-if="
                  item.examDate
                "
              >
                ·
                {{
                  item.examDate
                }}
              </span>
            </div>
          </div>

          <van-notice-bar
            type="danger"
            left-icon="warning-o"
            wrapable
            :scrollable="
              false
            "
            text="存在低于 60 分的课程，请完成补考或重修；成绩达到 60 分及以上后，毕业资格将重新计算。"
          />
        </template>

        <!-- 全部通过 -->
        <div
          v-else-if="
            result?.eligible
          "
          class="
            st-card
            graduate__success
          "
        >
          <van-icon
            name="passed"
            size="64"
            color="#07c160"
          />

          <div
            class="
              graduate__success-title
            "
          >
            所有课程成绩均已及格
          </div>

          <div
            class="
              st-muted
            "
          >
            当前没有低于 60 分的课程
          </div>
        </div>
      </PageState>
    </van-pull-refresh>

    <!-- 当前系统规则 -->
    <div
      class="
        st-section-title
      "
    >
      毕业资格规则
    </div>

    <div
      class="
        st-card
        graduate__rules
        st-muted
      "
    >
      当前系统按课程成绩判断毕业资格：

      <div
        class="
          graduate__rule
        "
      >
        所有已录入成绩均 ≥ 60 分：
        可以毕业
      </div>

      <div
        class="
          graduate__rule
        "
      >
        存在任意成绩 &lt; 60 分：
        暂不可毕业
      </div>
    </div>
  </div>
</template>

<style scoped>
.graduate__result {
  border-left:
    4px solid
    var(--st-text-light);
}

.graduate__result--pass {
  background:
    var(--st-success-light);

  border-left-color:
    var(--st-success);
}

.graduate__result--fail {
  background:
    var(--st-danger-light);

  border-left-color:
    var(--st-danger);
}

.graduate__result-title {
  font-size: 20px;
  font-weight: 600;
}

.graduate__result--pass
.graduate__result-title {
  color:
    var(--st-success);
}

.graduate__result--fail
.graduate__result-title {
  color:
    var(--st-danger);
}

.graduate__result-desc {
  margin: 6px 0 10px;

  color:
    var(--st-text-light);

  font-size: 13px;

  line-height: 1.6;
}

.graduate__success {
  padding: 32px 16px;
  text-align: center;
}

.graduate__success-title {
  margin-top: 12px;
  margin-bottom: 6px;

  color: #07c160;
  font-size: 16px;
  font-weight: 600;
}

.graduate__course {
  margin-bottom: 10px;
}

.graduate__course-head {
  align-items: center;
}

.graduate__course-name {
  font-size: 15px;
  font-weight: 600;
}

.graduate__course-code {
  margin-top: 3px;
  font-size: 12px;
}

.graduate__score {
  color:
    var(--st-danger);

  font-size: 20px;
  font-weight: 700;
}

.graduate__exam {
  margin-top: 8px;
  font-size: 12px;
}

.graduate__rules {
  line-height: 1.8;
}

.graduate__rule {
  margin-top: 6px;
}
</style>
