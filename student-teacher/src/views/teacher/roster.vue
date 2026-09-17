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
  listClasses,
  listStudentsByClass,
} from '@/api/base'

import type {
  ClassInfo,
  Student,
} from '@/api/base'

import {
  useAsyncData,
} from '@/composables/useAsyncData'

import PageHeader
  from '@/components/PageHeader.vue'

import PageState
  from '@/components/PageState.vue'

import StatBar
  from '@/components/StatBar.vue'

import {
  exportToExcel,
  timestampedFileName,
} from '@/utils/excel'

/**
 * 当前选中的班级ID。
 *
 * 使用 string | number，
 * 兼容后端BIGINT / Snowflake ID。
 */
const selectedClassId =
  ref<string | number | ''>('')

const keyword =
  ref('')

const showClassPicker =
  ref(false)

/**
 * ==============================
 * 班级列表
 * ==============================
 *
 * 真实后端：
 *
 * GET /api/classes
 */
const {
  data: classes,
  loading: classLoading,
  error: classError,
  reload: reloadClasses,
} =
  useAsyncData<ClassInfo[]>(
    () =>
      listClasses(),
    [],
  )

/**
 * ==============================
 * 班级学生
 * ==============================
 *
 * 真实后端：
 *
 * GET /api/students?classId=xxx
 */
const {
  data: students,
  loading: studentLoading,
  error: studentError,
  reload: reloadStudents,
} =
  useAsyncData<Student[]>(
    () => {
      if (
        !selectedClassId.value
      ) {
        return Promise.resolve([])
      }

      return listStudentsByClass(
        selectedClassId.value,
      )
    },
    [],
  )

/**
 * 当前班级名称。
 *
 * String比较是为了兼容：
 *
 * number ID
 * string ID
 */
const classText =
  computed(() => {
    const current =
      classes.value.find(
        (item) =>
          String(item.id)
          ===
          String(
            selectedClassId.value,
          ),
      )

    return current?.name ?? ''
  })

/**
 * Vant Picker 数据。
 *
 * ID统一转换成字符串，
 * 避免Snowflake ID被JS Number破坏。
 */
const classColumns =
  computed(() =>
    classes.value
      .filter(
        (item) =>
          item.id !== null
          && item.id !== undefined,
      )
      .map(
        (item) => ({
          text:
            item.name,

          value:
            String(
              item.id,
            ),
        }),
      ),
  )

/**
 * 学号 / 姓名搜索。
 */
const visibleStudents =
  computed(() => {
    const key =
      keyword.value
        .trim()
        .toLowerCase()

    if (!key) {
      return students.value
    }

    return students.value.filter(
      (item) =>
        item.name
          ?.toLowerCase()
          .includes(key)
        ||
        String(
          item.studentNo ?? '',
        )
          .toLowerCase()
          .includes(key),
    )
  })

/**
 * 顶部统计。
 */
const summary =
  computed(() => {
    const rows =
      students.value

    return [
      {
        label: '班级人数',
        value: rows.length,
      },
      {
        label: '在读',
        value:
          rows.filter(
            (row) =>
              row.status
              === '在读',
          ).length,
      },
      {
        label: '筛选结果',
        value:
          visibleStudents.value.length,
      },
    ]
  })

interface PickerPayload {
  selectedOptions?: Array<{
    value?:
      | string
      | number
  }>
}

/**
 * 选择班级。
 */
async function onClassConfirm(
  payload: PickerPayload,
) {
  const value =
    payload
      .selectedOptions
      ?.[0]
      ?.value

  if (
    value === undefined
    || value === null
    || value === ''
  ) {
    showClassPicker.value =
      false

    return
  }

  selectedClassId.value =
    String(value)

  keyword.value =
    ''

  showClassPicker.value =
    false

  await reloadStudents()
}

/**
 * 页面整体重新加载。
 */
async function reloadPage() {
  await reloadClasses()

  if (
    classes.value.length
    === 0
  ) {
    selectedClassId.value =
      ''

    return
  }

  /**
   * 如果原选中班级已经不存在，
   * 自动选择第一个。
   */
  const exists =
    classes.value.some(
      (item) =>
        String(item.id)
        ===
        String(
          selectedClassId.value,
        ),
    )

  if (!exists) {
    selectedClassId.value =
      String(
        classes.value[0]
          ?.id
        ?? '',
      )
  }

  if (
    selectedClassId.value
  ) {
    await reloadStudents()
  }
}

/**
 * 导出当前筛选后的花名册。
 */
function onExport() {
  if (
    visibleStudents.value.length
    === 0
  ) {
    showToast(
      '当前没有可导出的学生',
    )

    return
  }

  const rows =
    visibleStudents.value.map(
      (student) => ({
        studentNo:
          student.studentNo,

        name:
          student.name,

        gender:
          student.gender ?? '',

        className:
          classText.value,

        phone:
          student.phone ?? '',

        status:
          student.status ?? '',
      }),
    )

  exportToExcel({
    rows,

    columns: [
      {
        label: '学号',
        key: 'studentNo',
      },
      {
        label: '姓名',
        key: 'name',
      },
      {
        label: '性别',
        key: 'gender',
      },
      {
        label: '班级',
        key: 'className',
      },
      {
        label: '联系电话',
        key: 'phone',
      },
      {
        label: '状态',
        key: 'status',
      },
    ],

    fileName:
      timestampedFileName(
        `${
          classText.value
          || '班级'
        }花名册`,
      ),

    sheetName:
      '花名册',
  })

  showToast(
    '已导出 Excel',
  )
}

onMounted(
  reloadPage,
)
</script>

<template>
  <div>
    <!-- 顶部 -->
    <div class="st-card">
      <PageHeader
        title="班级花名册"
      >
        <template #actions>
          <van-button
            size="small"
            type="primary"
            :disabled="
              visibleStudents.length
              === 0
            "
            @click="onExport"
          >
            导出 Excel
          </van-button>
        </template>
      </PageHeader>

      <!-- 班级选择 -->
      <van-field
        :model-value="
          classText
        "
        label="班级"
        placeholder="请选择班级"
        readonly
        is-link
        @click="
          showClassPicker = true
        "
      />

      <!-- 统计 -->
      <StatBar
        :items="summary"
        class="roster__stat"
      />
    </div>

    <!-- 搜索 -->
    <van-search
      v-model="keyword"
      class="roster__filter"
      placeholder="搜索学号 / 姓名"
    />

    <!-- 数据状态 -->
    <PageState
      :loading="
        classLoading
        || studentLoading
      "
      :error="
        classError
        || studentError
      "
      :empty="
        visibleStudents.length
        === 0
      "
      :empty-text="
        selectedClassId
          ? '该班级暂无学生'
          : '暂无班级数据'
      "
      @retry="reloadPage"
    >
      <!-- 学生列表 -->
      <div
        v-for="
          row in visibleStudents
        "
        :key="
          String(
            row.id
            ?? row.studentNo
          )
        "
        class="
          st-card
          st-row
        "
      >
        <div>
          <div class="roster__name">
            {{ row.name }}
          </div>

          <div class="st-muted">
            {{ row.studentNo }}

            <template
              v-if="row.gender"
            >
              · {{ row.gender }}
            </template>
          </div>
        </div>

        <div class="roster__right">
          <span class="st-muted">
            {{
              row.phone
              || '—'
            }}
          </span>

          <van-tag
            plain
            :type="
              row.status === '在读'
                ? 'primary'
                : 'warning'
            "
          >
            {{
              row.status
              || '未知'
            }}
          </van-tag>
        </div>
      </div>
    </PageState>

    <!-- 班级选择器 -->
    <van-popup
      v-model:show="
        showClassPicker
      "
      position="bottom"
      round
    >
      <van-empty
        v-if="
          classColumns.length
          === 0
        "
        description="暂无班级"
      />

      <van-picker
        v-else
        :columns="
          classColumns
        "
        @confirm="
          onClassConfirm
        "
        @cancel="
          showClassPicker = false
        "
      />
    </van-popup>
  </div>
</template>

<style scoped>
.roster__filter {
  margin-bottom: 10px;
}

.roster__stat {
  margin-top: 8px;
}

.roster__name {
  font-size: 15px;
  font-weight: 600;
}

.roster__right {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>