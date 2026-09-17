<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, MagicStick, RefreshLeft } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import {
  getArrangeCourses,
  getArrangeTeachers,
  getFreeClassrooms,
  getExistingExams,
  getExistingExamRooms,
  getExistingExamMonitors,
  saveExamSchedule,

  type ClassroomItem,
  type CourseItem,
  type ExamArrangeItem,
  type ExistingExamItem,
  type ExistingExamRoomItem,
  type ExistingExamMonitorItem,
  type TeacherItem,
} from '@/api/exam'
import { getCurrentTerm, type Term } from '@/api/term'

interface ArrangeRow extends ExamArrangeItem {
  saveStatus: 'pending' | 'saving' | 'success' | 'failed'
  saveError?: string
}

const TIME_SLOTS = [
  {
    startTime: '08:30:00',
    endTime: '10:30:00',
  },
  {
    startTime: '14:00:00',
    endTime: '16:00:00',
  },
  {
    startTime: '19:00:00',
    endTime: '21:00:00',
  },
]

const currentTerm = ref<Term | null>(null)
const courses = ref<CourseItem[]>([])
const classrooms = ref<ClassroomItem[]>([])
const teachers = ref<TeacherItem[]>([])
const existingExams = ref<ExistingExamItem[]>([])
const existingExamRooms =
  ref<ExistingExamRoomItem[]>([])

const existingExamMonitors =
  ref<ExistingExamMonitorItem[]>([])

const rows = ref<ArrangeRow[]>([])

const loadingBase = ref(false)
const arranging = ref(false)
const saving = ref(false)
const exporting = ref(false)
const generatedAt = ref('')

const courseCount = computed(() => courses.value.length)
const classroomCount = computed(() => classrooms.value.length)
const teacherCount = computed(() => teachers.value.length)

const arrangedCourseIds = computed(() => {
  if (!currentTerm.value) {
    return new Set<string>()
  }

  return new Set(
    existingExams.value
      .filter((exam) => {
        return (
          String(exam.termId) === String(currentTerm.value?.id)
          && exam.status === 'ARRANGED'
        )
      })
      .map((exam) => String(exam.courseId)),
  )
})

const pendingCourses = computed(() => {
  return courses.value.filter(
    (course) =>
      !arrangedCourseIds.value.has(
        String(course.id),
      ),
  )
})

const usedRoomCount = computed(() => {
  return new Set(rows.value.map((item) => item.classroomId)).size
})

const usedTeacherCount = computed(() => {
  return new Set(rows.value.map((item) => item.teacherId)).size
})

const savedCount = computed(() => {
  return rows.value.filter((item) => item.saved).length
})

const hasSavedRows = computed(() => savedCount.value > 0)

const statCards = computed(() => [
  {
    label: '排考课程',
    value: rows.value.length,
    unit: '门',
  },
  {
    label: '使用教室',
    value: usedRoomCount.value,
    unit: '间',
  },
  {
    label: '监考教师',
    value: usedTeacherCount.value,
    unit: '人',
  },
  {
    label: '已写入数据库',
    value: savedCount.value,
    unit: '条',
  },
])

function shuffle<T>(source: T[]) {
  const result = [...source]

  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[result[i], result[j]] = [result[j], result[i]]
  }

  return result
}

function addDays(date: Date, days: number) {
  const result = new Date(date)
  result.setDate(result.getDate() + days)
  return result
}

function parseDate(value: string) {
  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day)
}

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

function formatDateTime(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

function getExamBaseDate(term: Term) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const tomorrow = addDays(today, 1)
  const termEnd = parseDate(term.endDate)
  const preferredDate = addDays(termEnd, -14)

  if (preferredDate < tomorrow) {
    return tomorrow <= termEnd ? tomorrow : termEnd
  }

  return preferredDate
}

async function loadBaseData() {
  loadingBase.value = true

  try {
    const [
      termData,
      coursePage,
      classroomData,
      teacherPage,
      examPage,
      examRoomData,
      examMonitorData,
    ] =
      await Promise.all([
        getCurrentTerm(),

        getArrangeCourses(),

        getFreeClassrooms(),

        getArrangeTeachers(),

        /**
         * 已有考试
         */
        getExistingExams(),

        /**
         * 已有考试使用的教室
         */
        getExistingExamRooms(),

        /**
         * 已有考试监考教师
         */
        getExistingExamMonitors(),
      ])

    if (!termData) {
      ElMessage.warning('当前没有可用学期')
      return
    }

    currentTerm.value = termData

    courses.value = coursePage.records

    classrooms.value = classroomData
    teachers.value = teacherPage.records
    existingExams.value = examPage.records

    existingExamRooms.value =
      examRoomData

    existingExamMonitors.value =
      examMonitorData

    rows.value = []
    generatedAt.value = ''

    if (courses.value.length === 0) {
      ElMessage.warning('当前学期暂时没有课程')
      return
    }

    ElMessage.success(
      `已识别 ${courses.value.length} 门课程、${classrooms.value.length} 间空闲教室、${teachers.value.length} 名教师`,
    )
  } catch {
    // 请求错误已经由 request.ts 统一提示
  } finally {
    loadingBase.value = false
  }
}

/**
 * 两个时间段是否重叠。
 *
 * A开始 < B结束
 * &&
 * A结束 > B开始
 */
function timeOverlap(
  start1: string,
  end1: string,
  start2: string,
  end2: string,
) {

  return (
    start1 < end2
    &&
    end1 > start2
  )
}

/**
 * 查询数据库已有考试中，
 * 与目标日期、时间冲突的考试。
 */
function getOverlappedExistingExams(
  examDate: string,
  startTime: string,
  endTime: string,
) {

  return existingExams.value.filter(
    exam =>
      exam.examDate === examDate
      &&
      timeOverlap(
        exam.startTime,
        exam.endTime,
        startTime,
        endTime,
      ),
  )
}

/**
 * 教室是否已经被数据库中的考试占用。
 */
function isExistingRoomBusy(
  classroomId: string,
  examDate: string,
  startTime: string,
  endTime: string,
) {

  const overlapIds =
    new Set(
      getOverlappedExistingExams(
        examDate,
        startTime,
        endTime,
      ).map(
        exam =>
          String(exam.id),
      ),
    )

  return existingExamRooms.value.some(
    room =>
      overlapIds.has(
        String(room.examId),
      )
      &&
      String(
        room.classroomId,
      )
      ===
      String(
        classroomId,
      ),
  )
}

/**
 * 教师是否已经在数据库已有考试中监考。
 */
function isExistingTeacherBusy(
  teacherId: string,
  examDate: string,
  startTime: string,
  endTime: string,
) {

  const overlapIds =
    new Set(
      getOverlappedExistingExams(
        examDate,
        startTime,
        endTime,
      ).map(
        exam =>
          String(exam.id),
      ),
    )

  return existingExamMonitors.value.some(
    monitor =>
      overlapIds.has(
        String(
          monitor.examId,
        ),
      )
      &&
      String(
        monitor.teacherId,
      )
      ===
      String(
        teacherId,
      ),
  )
}

async function handleArrange() {
  if (!currentTerm.value) {
    ElMessage.warning('没有识别到当前学期')
    return
  }

  if (courses.value.length === 0) {
    ElMessage.warning('当前学期没有课程，无法排考')
    return
  }

  if (pendingCourses.value.length === 0) {
    ElMessage.info('当前学期所有课程均已完成排考')
    rows.value = []
    return
  }

  if (classrooms.value.length === 0) {
    ElMessage.error('系统没有可用教室，无法排考')
    return
  }

  if (teachers.value.length === 0) {
    ElMessage.error('系统没有教师数据，无法安排监考')
    return
  }

  arranging.value = true

  try {
    /**
     * 从今天开始向后寻找可用考试时间。
     *
     * 不再限制120天，
     * 也不因为学期结束就直接放弃。
     */
    const today =
      parseDate(
        formatDate(
          new Date(),
        ),
      )

    const baseDate =
      getExamBaseDate(
        currentTerm.value,
      )

    /**
     * 防止排到过去。
     */
    const searchStartDate =
      baseDate < today
        ? today
        : baseDate

    const generated:
      ArrangeRow[] = []

    for (
      const course
      of pendingCourses.value
    ) {

      let arranged =
        false

      /**
       * 当前正在尝试的日期。
       */
      let currentDate =
        new Date(
          searchStartDate,
        )

      /**
       * 只要还有教室和教师，
       * 就持续向后寻找。
       */
      while (
        !arranged
      ) {

        const examDate =
          formatDate(
            currentDate,
          )

        /**
         * 可以随机时间段，
         * 也可以直接按早->晚排列。
         */
        for (
          const timeSlot
          of TIME_SLOTS
        ) {

          /**
           * 1.
           * 找当前时间段可用教室。
           */
          const classroom =
            classrooms.value.find(
              room => {

                const roomId =
                  String(
                    room.id,
                  )

                /**
                 * 数据库已有考试
                 * 是否占用了这个教室。
                 */
                if (
                  isExistingRoomBusy(
                    roomId,
                    examDate,
                    timeSlot.startTime,
                    timeSlot.endTime,
                  )
                ) {
                  return false
                }

                /**
                 * 本轮一键排考刚生成的数据
                 * 是否已经占用了这个教室。
                 */
                const generatedConflict =
                  generated.some(
                    item =>
                      item.classroomId
                        === roomId
                      &&
                      item.examDate
                        === examDate
                      &&
                      timeOverlap(
                        item.startTime,
                        item.endTime,
                        timeSlot.startTime,
                        timeSlot.endTime,
                      ),
                  )

                return !generatedConflict
              },
            )

          /**
           * 这个时间没有空教室。
           *
           * 不报错，
           * 继续尝试下一个时间段。
           */
          if (!classroom) {
            continue
          }

          /**
           * 2.
           * 找当前时间没有监考任务的教师。
           */
          const teacher =
            teachers.value.find(
              teacher => {

                const teacherId =
                  String(
                    teacher.id,
                  )

                /**
                 * 数据库已有监考安排冲突。
                 */
                if (
                  isExistingTeacherBusy(
                    teacherId,
                    examDate,
                    timeSlot.startTime,
                    timeSlot.endTime,
                  )
                ) {
                  return false
                }

                /**
                 * 当前批次刚生成的监考安排冲突。
                 */
                const generatedConflict =
                  generated.some(
                    item =>
                      item.teacherId
                        === teacherId
                      &&
                      item.examDate
                        === examDate
                      &&
                      timeOverlap(
                        item.startTime,
                        item.endTime,
                        timeSlot.startTime,
                        timeSlot.endTime,
                      ),
                  )

                return !generatedConflict
              },
            )

          /**
           * 有教室但没有老师。
           *
           * 继续尝试下一个时间段。
           */
          if (!teacher) {
            continue
          }

          /**
           * 3.
           * 教室和教师同时可用。
           *
           * 终于找到排考时间。
           */
          generated.push({
            courseId:
              String(
                course.id,
              ),

            courseName:
              course.name,

            classroomId:
              String(
                classroom.id,
              ),

            classroomName:
              classroom.roomNo,

            teacherId:
              String(
                teacher.id,
              ),

            teacherName:
              teacher.name,

            examDate,

            startTime:
              timeSlot.startTime,

            endTime:
              timeSlot.endTime,

            saved:
              false,

            saveStatus:
              'pending',

            saveError:
              '',
          })

          arranged =
            true

          break
        }

        /**
         * 今天所有时间段都没找到。
         *
         * 不报失败。
         *
         * 日期 + 1，
         * 明天继续找。
         */
        if (
          !arranged
        ) {
          currentDate =
            addDays(
              currentDate,
              1,
            )
        }
      }
    }

    rows.value =
      generated

    generatedAt.value =
      formatDateTime(
        new Date(),
      )

    ElMessage.success(
      `已跳过 ${arrangedCourseIds.value.size} 门已排课程，生成 ${rows.value.length} 条无冲突排考结果`,
    )
  } finally {
    arranging.value = false
  }
}

async function handleSaveAll() {
  if (!currentTerm.value) {
    ElMessage.warning('当前学期不存在')
    return
  }

  const pendingRows =
    rows.value.filter((item) => !item.saved)

  if (pendingRows.length === 0) {
    ElMessage.info('当前排考结果已经全部写入数据库')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定将 ${pendingRows.length} 条排考结果写入数据库吗？`,
      '确认排考',
      {
        confirmButtonText: '确认写入',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  saving.value = true

  let successCount = 0
  let failedCount = 0

  for (const row of pendingRows) {
    row.saveStatus = 'saving'
    row.saveError = ''

    try {
      await saveExamSchedule({
        examInfo: {
          name: `${row.courseName}期末考试`,
          courseId: row.courseId,
          termId: String(currentTerm.value.id),
          examType: '期末考试',
          examDate: row.examDate,
          startTime: row.startTime,
          endTime: row.endTime,
        },
        classroomIds: [
          row.classroomId,
        ],
        monitorTeacherIds: [
          row.teacherId,
        ],
      })

      row.saved = true
      row.saveStatus = 'success'
      successCount++
    } catch (error) {
      row.saved = false
      row.saveStatus = 'failed'
      row.saveError =
        error instanceof Error
          ? error.message
          : '保存失败'

      failedCount++
    }
  }

  saving.value = false

  if (failedCount === 0) {
    ElMessage.success(
      `排考完成，${successCount} 条记录已写入数据库`,
    )
    return
  }

  ElMessage.warning(
    `写入完成：成功 ${successCount} 条，失败 ${failedCount} 条`,
  )
}

function handleRegenerate() {
  if (hasSavedRows.value) {
    ElMessage.warning(
      '已有排考结果写入数据库，不能直接重新生成',
    )
    return
  }

  handleArrange()
}

function exportExcel() {
  if (rows.value.length === 0) {
    ElMessage.info('暂无排考结果可导出')
    return
  }

  exporting.value = true

  try {
    const sheetRows =
      rows.value.map((row, index) => ({
        序号: index + 1,
        课程名称: row.courseName,
        教室: row.classroomName,
        考试日期: row.examDate,
        开始时间: row.startTime,
        结束时间: row.endTime,
        监考教师: row.teacherName,
        入库状态:
          row.saveStatus === 'success'
            ? '已入库'
            : row.saveStatus === 'failed'
              ? '保存失败'
              : '未入库',
      }))

    const worksheet =
      XLSX.utils.json_to_sheet(sheetRows)

    worksheet['!cols'] = [
      { wch: 6 },
      { wch: 24 },
      { wch: 16 },
      { wch: 14 },
      { wch: 12 },
      { wch: 12 },
      { wch: 16 },
      { wch: 12 },
    ]

    const workbook =
      XLSX.utils.book_new()

    XLSX.utils.book_append_sheet(
      workbook,
      worksheet,
      '排考结果',
    )

    const termName =
      currentTerm.value?.name
        ?.replace(/\s+/g, '')
        ?? '当前学期'

    XLSX.writeFile(
      workbook,
      `排考结果_${termName}.xlsx`,
    )

    ElMessage.success('排考结果已导出')
  } catch (error) {
    ElMessage.error(
      '导出失败：'
      + (
        error instanceof Error
          ? error.message
          : String(error)
      ),
    )
  } finally {
    exporting.value = false
  }
}

function getSaveStatusType(row: ArrangeRow) {
  if (row.saveStatus === 'success') {
    return 'success'
  }

  if (row.saveStatus === 'failed') {
    return 'danger'
  }

  if (row.saveStatus === 'saving') {
    return 'warning'
  }

  return 'info'
}

function getSaveStatusText(row: ArrangeRow) {
  if (row.saveStatus === 'success') {
    return '已入库'
  }

  if (row.saveStatus === 'failed') {
    return '失败'
  }

  if (row.saveStatus === 'saving') {
    return '保存中'
  }

  return '待保存'
}

onMounted(() => {
  loadBaseData()
})
</script>

<template>
  <div class="exam-arrange-page">
    <el-card
      shadow="never"
      v-loading="loadingBase"
    >
      <template #header>
        <div class="base-header">
          <span class="base-header__title">
            排考基础数据
          </span>

          <el-button
            :icon="RefreshLeft"
            :loading="loadingBase"
            @click="loadBaseData"
          >
            刷新数据
          </el-button>
        </div>
      </template>

      <el-descriptions
        :column="4"
        border
      >
        <el-descriptions-item label="当前学期">
          {{
            currentTerm?.name
              || '未识别'
          }}
        </el-descriptions-item>

        <el-descriptions-item label="当前课程">
          {{ courseCount }} 门
        </el-descriptions-item>

        <el-descriptions-item label="空闲教室">
          {{ classroomCount }} 间
        </el-descriptions-item>

        <el-descriptions-item label="教师">
          {{ teacherCount }} 人
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card
      shadow="never"
      class="arrange-action-card"
    >
      <div class="arrange-action">
        <div class="arrange-action__title">
          系统将根据当前学期课程、空闲教室和教师自动生成考试安排
        </div>

        <el-button
          type="primary"
          size="large"
          class="arrange-action__btn"
          :icon="MagicStick"
          :loading="arranging"
          :disabled="
            loadingBase
            || courseCount === 0
            || classroomCount === 0
            || teacherCount === 0
          "
          @click="handleArrange"
        >
          一键生成排考
        </el-button>

        <div class="arrange-action__tip">
          生成阶段只在前端产生预览结果，不会立即写入数据库
        </div>
      </div>
    </el-card>

    <template v-if="rows.length > 0">
      <el-row
        :gutter="14"
        class="stat-row"
      >
        <el-col
          v-for="card in statCards"
          :key="card.label"
          :span="6"
        >
          <el-card
            shadow="never"
            class="stat-card"
          >
            <div class="stat-card__value">
              {{ card.value }}
              <span class="stat-card__unit">
                {{ card.unit }}
              </span>
            </div>

            <div class="stat-card__label">
              {{ card.label }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never">
        <template #header>
          <div class="result-head">
            <div>
              <div class="result-head__title">
                排考结果
              </div>

              <div class="result-head__time">
                生成时间：{{ generatedAt }}
              </div>
            </div>

            <div class="result-head__actions">
              <el-button
                :icon="RefreshLeft"
                :disabled="saving || hasSavedRows"
                @click="handleRegenerate"
              >
                重新生成
              </el-button>

              <el-button
                type="success"
                :icon="Download"
                :loading="exporting"
                @click="exportExcel"
              >
                导出 Excel
              </el-button>

              <el-button
                type="primary"
                :loading="saving"
                :disabled="savedCount === rows.length"
                @click="handleSaveAll"
              >
                确认写入数据库
              </el-button>
            </div>
          </div>
        </template>

        <el-table
          :data="rows"
          stripe
          style="width: 100%"
        >
          <el-table-column
            type="index"
            label="#"
            width="55"
          />

          <el-table-column
            prop="courseName"
            label="考试科目"
            min-width="200"
            show-overflow-tooltip
          />

          <el-table-column
            prop="classroomName"
            label="考试教室"
            min-width="130"
          />

          <el-table-column
            prop="examDate"
            label="考试日期"
            width="130"
          />

          <el-table-column
            label="考试时间"
            width="180"
          >
            <template #default="{ row }">
              {{ row.startTime.slice(0, 5) }}
              -
              {{ row.endTime.slice(0, 5) }}
            </template>
          </el-table-column>

          <el-table-column
            prop="teacherName"
            label="监考教师"
            min-width="140"
          >
            <template #default="{ row }">
              <el-tag
                type="info"
                effect="plain"
              >
                {{ row.teacherName }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column
            label="入库状态"
            width="120"
            align="center"
          >
            <template #default="{ row }">
              <el-tooltip
                :disabled="!row.saveError"
                :content="row.saveError"
                placement="top"
              >
                <el-tag
                  :type="getSaveStatusType(row)"
                  effect="dark"
                >
                  {{ getSaveStatusText(row) }}
                </el-tag>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <el-card
      v-else
      shadow="never"
      class="arrange-empty-card"
    >
      <el-empty
        description="基础数据加载完成后，点击“一键生成排考”生成考试安排"
      >
        <template #image>
          <div class="arrange-empty-icon">
            📋
          </div>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<style scoped>
.exam-arrange-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.base-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.base-header__title {
  font-weight: 600;
}

.arrange-action-card :deep(.el-card__body) {
  padding: 0;
}

.arrange-action {
  padding: 30px 24px;
  text-align: center;
  background: linear-gradient(
    135deg,
    #ecf5ff 0%,
    #f0f9ff 100%
  );
  border-radius: 4px;
}

.arrange-action__title {
  margin-bottom: 18px;
  font-size: 15px;
  color: var(--el-text-color-regular);
}

.arrange-action__btn {
  min-width: 260px;
  height: 46px;
  font-size: 16px;
}

.arrange-action__tip {
  margin-top: 14px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.stat-card {
  text-align: center;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--el-color-primary);
}

.stat-card__unit {
  margin-left: 4px;
  font-size: 13px;
  font-weight: 400;
  color: var(--el-text-color-secondary);
}

.stat-card__label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.result-head__title {
  font-weight: 600;
}

.result-head__time {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.result-head__actions {
  display: flex;
  gap: 8px;
}

.arrange-empty-icon {
  font-size: 64px;
  line-height: 1;
}
</style>