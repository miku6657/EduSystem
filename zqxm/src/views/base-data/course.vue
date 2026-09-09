<script setup lang="ts">
import CommonTable from '@/components/CommonTable.vue'
import type { DialogField, SearchField, TableColumn } from '@/types/table'

const termOptions = [
  { label: '2026-2027学年第一学期', value: '2026-2027学年第一学期' },
  { label: '2026-2027学年第二学期', value: '2026-2027学年第二学期' },
]

const creditOptions = [1, 2, 3, 4, 5, 6].map((value) => ({
  label: `${value} 学分`,
  value,
}))

const searchFields: SearchField[] = [
  { label: '课程名称', prop: 'name', type: 'input', placeholder: '请输入课程名称' },
  { label: '学期', prop: 'term', type: 'select', placeholder: '请选择学期', options: termOptions },
]

const tableColumns: TableColumn[] = [
  { label: 'ID', prop: 'id', width: 80 },
  { label: '课程名称', prop: 'name', minWidth: 180 },
  { label: '学期', prop: 'term', minWidth: 200 },
  { label: '授课教师', prop: 'teacher', width: 120 },
  { label: '学分', prop: 'credit', width: 90, align: 'center' },
  { label: '开课日期', prop: 'startDate', width: 120 },
  { label: '创建时间', prop: 'createTime', width: 180 },
  { label: '操作', type: 'action', width: 140, fixed: 'right' },
]

const dialogFields: DialogField[] = [
  { label: '课程名称', prop: 'name', type: 'input', placeholder: '请输入课程名称' },
  { label: '学期', prop: 'term', type: 'select', placeholder: '请选择学期', options: termOptions },
  { label: '授课教师', prop: 'teacher', type: 'input', placeholder: '请输入授课教师' },
  { label: '学分', prop: 'credit', type: 'select', placeholder: '请选择学分', options: creditOptions },
  { label: '开课日期', prop: 'startDate', type: 'date-picker', required: false },
]
</script>

<template>
  <CommonTable
    title="课程管理"
    :search-fields="searchFields"
    :table-columns="tableColumns"
    :dialog-fields="dialogFields"
    api-url="/api/course/list"
    add-api="/api/course/add"
    edit-api="/api/course/edit"
    delete-api="/api/course/delete"
  />
</template>
