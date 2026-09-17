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
  listScoreStudents,
  listUnscoredCourses,
  saveScores,
} from '@/api/score'

import type {
  ExamScore,
  ScoreStudent,
  UnscoredCourse,
} from '@/api/score'

import {
  listRetakeStudents,
  saveRetakeScores,
} from '@/api/retake'

import {
  http,
} from '@/utils/request'

import PageHeader
  from '@/components/PageHeader.vue'

import StatBar
  from '@/components/StatBar.vue'

interface ScoreRow {
  studentId: string

  studentNo: string

  name: string

  score: string

  status:
    | 'NORMAL'
    | 'ABSENT'

  /**
   * 重修考试时，对应 exam_retake.id。
   */
  retakeId?: string
}

const courses =
  ref<
    UnscoredCourse[]
  >([])

const rows =
  ref<
    ScoreRow[]
  >([])

const selectedExamId =
  ref('')

const loading =
  ref(false)

const saving =
  ref(false)

const loadingCourses =
  ref(false)

const showCoursePicker =
  ref(false)

/**
 * 当前选择的课程。
 */
const selectedCourse =
  computed(
    () =>
      courses.value.find(
        item =>
          item.examId
          ===
          selectedExamId.value,
      )
      ?? null,
  )

/**
 * 重修考试走独立录入流程。
 */
const isRetakeExam =
  computed(
    () =>
      selectedCourse.value
        ?.examType
      === '重修考试',
  )

const courseText =
  computed(() => {

    const item =
      selectedCourse.value

    if (!item) {
      return ''
    }

    const code =
      item.courseCode
        ? `（${item.courseCode}）`
        : ''

    return (
      `${item.courseName}${code}`
    )
  })

/**
 * 只显示“没有成绩”的课程。
 */
const courseColumns =
  computed(() =>
    courses.value.map(
      item => ({
        text:
          `${item.courseName}`
          +
          (
            item.courseCode
              ? `（${item.courseCode}）`
              : ''
          )
          +
          (
            item.examType
              ? ` · ${item.examType}`
              : ''
          ),

        value:
          item.examId,
      }),
    ),
  )

const summaryItems =
  computed(() => {

    const total =
      rows.value.length

    const entered =
      rows.value.filter(
        row =>
          row.status === 'ABSENT'
          ||
          row.score.trim() !== '',
      ).length

    const absent =
      rows.value.filter(
        row =>
          row.status
          === 'ABSENT',
      ).length

    return [
      {
        label: '学生人数',
        value: total,
      },

      {
        label: '已填写',
        value: entered,
      },

      {
        label: '缺考',
        value: absent,
      },
    ]
  })

/**
 * 加载还没有成绩的课程。
 */
async function loadCourses() {

  loadingCourses.value =
    true

  try {

    courses.value =
      await listUnscoredCourses()

  } catch {
    // request.ts统一处理
  } finally {

    loadingCourses.value =
      false
  }
}

/**
 * 选择课程后，
 * 加载学生生成成绩录入表。
 */
async function loadStudents() {

  if (
    !selectedExamId.value
  ) {
    return
  }

  loading.value =
    true

  try {

    if (
      isRetakeExam.value
    ) {
      rows.value =
        await loadRetakeStudents(
          selectedExamId.value,
        )
    } else {

      const students =
        await listScoreStudents()

      rows.value =
        students.map(
          (
            student:
              ScoreStudent,
          ) => ({
            studentId:
              String(
                student.id,
              ),

            studentNo:
              student.studentNo,

            name:
              student.name,

            score:
              '',

            status:
              'NORMAL',
          }),
        )
    }

  } catch {
    rows.value =
      []
  } finally {

    loading.value =
      false
  }
}

/**
 * 重修考试：只加载真正报名的学生。
 *
 * GET
 * /api/exam-retakes/exams/{examId}
 * ↓
 * 再根据 studentId 逐个取学生资料。
 */
async function loadRetakeStudents(
  examId: string,
): Promise<ScoreRow[]> {

  const retakes =
    await listRetakeStudents(
      examId,
    )

  return Promise.all(
    retakes.map(
      async item => {

        const student =
          await http.get<any>(
            `/students/${item.studentId}`,
          )

        return {
          studentId:
            item.studentId,

          studentNo:
            student.studentNo,

          name:
            student.name,

          score:
            '',

          status:
            'NORMAL',

          retakeId:
            item.id,
        } satisfies ScoreRow
      },
    ),
  )
}

function openCoursePicker() {

  if (
    courses.value.length === 0
  ) {
    showToast(
      '当前没有待录入成绩的课程',
    )

    return
  }

  showCoursePicker.value =
    true
}

async function onCourseConfirm(
  payload: {
    selectedOptions?: Array<{
      value?:
        | string
        | number
    }>
  },
) {

  const value =
    payload
      .selectedOptions
      ?.[0]
      ?.value

  showCoursePicker.value =
    false

  if (
    value === null
    || value === undefined
  ) {
    return
  }

  selectedExamId.value =
    String(value)

  await loadStudents()
}

/**
 * 选择缺考后，
 * 分数自动清空。
 */
function onStatusChange(
  row: ScoreRow,
) {

  if (
    row.status === 'ABSENT'
  ) {
    row.score =
      ''
  }
}

/**
 * 成绩必须：
 *
 * 0 ~ 100
 * 最多1位小数。
 */
function validScore(
  value: string,
) {

  const text =
    value.trim()

  if (
    !/^\d{1,3}(\.\d)?$/
      .test(text)
  ) {
    return false
  }

  const score =
    Number(text)

  return (
    score >= 0
    && score <= 100
  )
}

/**
 * 保存整个学生名单。
 *
 * 保存完成之后，
 * 这门课程因为已经存在exam_score，
 * 会自动从“待录入课程”中消失。
 */
async function onSave() {

  if (
    !selectedExamId.value
  ) {
    showToast(
      '请先选择课程',
    )

    return
  }

  /**
   * 重修考试走独立录入流程。
   */
  if (
    isRetakeExam.value
  ) {
    await saveCurrentRetakeScores()

    return
  }

  if (
    rows.value.length === 0
  ) {
    showToast(
      '没有学生数据',
    )

    return
  }

  /**
   * 要求每个学生都有结果：
   *
   * 正常 -> 必须输入分数
   * 缺考 -> 分数为空
   */
  for (
    const row
    of rows.value
  ) {

    if (
      row.status === 'ABSENT'
    ) {
      continue
    }

    if (
      !row.score.trim()
    ) {
      showToast(
        `请录入 ${row.name} 的成绩`,
      )

      return
    }

    if (
      !validScore(
        row.score,
      )
    ) {
      showToast(
        `${row.name} 的成绩必须为0~100`,
      )

      return
    }
  }

  try {

    await showConfirmDialog({
      title:
        '确认提交成绩',

      message:
        `确定提交「${selectedCourse.value?.courseName ?? ''}」共 ${rows.value.length} 名学生的成绩吗？`,
    })

  } catch {
    return
  }

  const payload:
    ExamScore[] =
    rows.value.map(
      row => ({
        studentId:
          row.studentId,

        status:
          row.status,

        score:
          row.status
          === 'ABSENT'
            ? null
            : Number(
                row.score,
              ),
      }),
    )

  saving.value =
    true

  try {

    await saveScores(
      selectedExamId.value,
      payload,
    )

    showToast(
      `成功录入 ${payload.length} 条成绩`,
    )

    /**
     * 清空当前页。
     */
    selectedExamId.value =
      ''

    rows.value =
      []

    /**
     * 重新查询。
     *
     * 刚录入的课程已经有成绩，
     * 因而不会再出现在下拉框。
     */
    await loadCourses()

  } catch {
    // request统一提示
  } finally {

    saving.value =
      false
  }
}

/**
 * 重修考试：只提交已填写成绩的学生。
 *
 * POST
 * /api/exam-retakes/exams/{examId}/scores
 */
async function saveCurrentRetakeScores() {

  if (
    !selectedExamId.value
  ) {
    return
  }

  /**
   * 先校验已填写的成绩。
   */
  for (
    const row
    of rows.value
  ) {

    if (
      !row.score.trim()
    ) {
      continue
    }

    if (
      !validScore(
        row.score,
      )
    ) {
      showToast(
        `${row.name} 的成绩必须为0~100`,
      )

      return
    }
  }

  const payload =
    rows.value
      .filter(
        row =>
          row.score.trim() !== '',
      )
      .map(
        row => ({
          studentId:
            row.studentId,

          score:
            Number(
              row.score,
            ),

          status:
            'NORMAL',
        }),
      )

  if (
    payload.length === 0
  ) {
    showToast(
      '请至少录入一名学生成绩',
    )

    return
  }

  await saveRetakeScores(
    selectedExamId.value,
    payload,
  )

  showToast(
    '重修成绩保存成功',
  )
}

onMounted(
  loadCourses,
)
</script>

<template>
  <div>
    <div class="st-card">
      <PageHeader
        title="成绩录入"
      >
        <template #actions>
          <van-button
            size="small"
            type="primary"
            :disabled="
              !selectedExamId
              || rows.length === 0
            "
            :loading="
              saving
            "
            @click="
              onSave
            "
          >
            保存成绩
          </van-button>
        </template>
      </PageHeader>

      <!--
        只显示还没有成绩的课程
      -->
      <van-field
        readonly
        is-link
        label="待录入课程"
        placeholder="请选择还没有成绩的课程"
        :model-value="
          courseText
        "
        @click="
          openCoursePicker
        "
      />

      <div
        v-if="
          selectedCourse
        "
        class="
          score__exam
          st-muted
        "
      >
        考试：
        {{
          selectedCourse.examName
        }}

        <span
          v-if="
            selectedCourse.examDate
          "
        >
          ·
          {{
            selectedCourse.examDate
          }}
        </span>
      </div>

      <StatBar
        :items="
          summaryItems
        "
      />
    </div>

    <!-- 无待录入课程 -->
    <van-empty
      v-if="
        !loadingCourses
        && courses.length === 0
      "
      description="当前所有考试课程均已录入成绩"
    />

    <!-- 还未选择 -->
    <van-empty
      v-else-if="
        !selectedExamId
      "
      description="请选择待录入成绩的课程"
    />

    <!-- 加载学生 -->
    <div
      v-else-if="
        loading
      "
      class="st-card"
    >
      <van-loading
        vertical
      >
        正在加载学生名单
      </van-loading>
    </div>

    <!-- 学生列表 -->
    <template
      v-else
    >
      <van-empty
        v-if="
          rows.length === 0
        "
        description="系统暂无学生数据"
      />

      <div
        v-for="
          row in rows
        "
        :key="
          row.studentId
        "
        class="st-card"
      >
        <div
          class="
            st-row
            score__row
          "
        >
          <div>
            <div
              class="
                score__name
              "
            >
              {{
                row.name
              }}
            </div>

            <div
              class="
                st-muted
                score__no
              "
            >
              {{
                row.studentNo
              }}
            </div>
          </div>

          <van-field
            v-model="
              row.score
            "
            type="number"
            placeholder="0-100"
            input-align="right"
            class="
              score__input
            "
            :disabled="
              row.status
              === 'ABSENT'
            "
          />
        </div>

        <van-radio-group
          v-if="
            !isRetakeExam
          "
          v-model="
            row.status
          "
          direction="horizontal"
          class="
            score__status
          "
          @change="
            onStatusChange(
              row,
            )
          "
        >
          <van-radio
            name="NORMAL"
          >
            正常
          </van-radio>

          <van-radio
            name="ABSENT"
          >
            缺考
          </van-radio>
        </van-radio-group>
      </div>

      <div
        v-if="
          rows.length > 0
        "
        class="
          st-card
          score__bottom
        "
      >
        <van-button
          block
          type="primary"
          :loading="
            saving
          "
          @click="
            onSave
          "
        >
          保存全部成绩
        </van-button>
      </div>
    </template>

    <!-- 课程Picker -->
    <van-popup
      v-model:show="
        showCoursePicker
      "
      position="bottom"
      round
    >
      <van-picker
        title="选择待录入课程"
        :columns="
          courseColumns
        "
        @confirm="
          onCourseConfirm
        "
        @cancel="
          showCoursePicker =
            false
        "
      />
    </van-popup>
  </div>
</template>

<style scoped>
.score__exam {
  padding:
    8px 16px
    2px;
  font-size: 12px;
}

.score__row {
  align-items: center;
}

.score__name {
  min-width: 100px;
  font-size: 15px;
  font-weight: 600;
}

.score__no {
  margin-top: 3px;
  font-size: 12px;
}

.score__input {
  flex: 1;
  padding: 4px 0;
}

.score__status {
  margin-top: 10px;
}

.score__bottom {
  margin-bottom: 20px;
}
</style>
