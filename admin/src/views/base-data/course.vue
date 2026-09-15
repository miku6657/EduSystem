<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CommonTable from '@/components/CommonTable.vue'
import { getTeachingGroupList, type TeachingGroup } from '@/api/teaching-group'
import type { DialogField, SearchField, TableColumn } from '@/types/table'

const creditOptions = [1, 2, 3, 4, 5, 6].map((value) => ({
  label: `${value} 学分`,
  value,
}))

const typeOptions = [
  { label: '专业课', value: '专业课' },
  { label: '公共课', value: '公共课' },
  { label: '实践课', value: '实践课' },
]

const teachingGroups = ref<TeachingGroup[]>([])
const teachingGroupOptions = computed(() => teachingGroups.value.map((group) => ({
  label: group.name,
  value: group.id,
})))

const searchFields = computed<SearchField[]>(() => [
  { label: '课程名称/代码', prop: 'keyword', type: 'input', placeholder: '请输入课程名称或课程代码' },
])

const tableColumns: TableColumn[] = [
  { label: 'ID', prop: 'id', width: 80 },
  { label: '课程代码', prop: 'courseCode', width: 130 },
  { label: '课程名称', prop: 'name', minWidth: 180 },
  { label: '学分', prop: 'credit', width: 90, align: 'center' },
  { label: '课程类型', prop: 'type', width: 120 },
  { label: '教研室 ID', prop: 'teachingGroupId', width: 120 },
  { label: '操作', type: 'action', width: 140, fixed: 'right' },
]

const dialogFields: DialogField[] = [
  { label: '课程代码', prop: 'courseCode', type: 'input', placeholder: '请输入课程代码' },
  { label: '课程名称', prop: 'name', type: 'input', placeholder: '请输入课程名称' },
  { label: '课程类型', prop: 'type', type: 'select', placeholder: '请选择课程类型', options: typeOptions },
  { label: '学分', prop: 'credit', type: 'select', placeholder: '请选择学分', options: creditOptions },
  {
    label: '教研室',
    prop: 'teachingGroupId',
    type: 'select',
    placeholder: '请选择教研室',
    options: teachingGroupOptions.value,
  },
]

onMounted(async () => {
  try {
    teachingGroups.value = await getTeachingGroupList()
  } catch {
    // 错误提示已由请求层统一处理
  }
})
</script>

<template>
  <CommonTable
    title="课程管理"
    :search-fields="searchFields"
    :table-columns="tableColumns"
    :dialog-fields="dialogFields"
    api-url="/api/courses"
    add-api="/api/courses"
    edit-api="/api/courses/{id}"
    delete-api="/api/courses/{id}"
    edit-method="put"
    delete-method="delete"
  />
</template>
