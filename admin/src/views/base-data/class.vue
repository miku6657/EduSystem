<script setup lang="ts">
import CommonTable from '@/components/CommonTable.vue'
import type { DialogField, SearchField, TableColumn } from '@/types/table'
import { http } from '@/utils/request'

const majorOptions = ref<{ label: string; value: number }[]>([])
const campusOptions = ref<{ label: string; value: number }[]>([])

http.get<{ id: number; name: string }[]>('/majors').then((items) => {
  majorOptions.value.push(...items.map((item) => ({ label: item.name, value: item.id })))
})

http.get<{ id: number; name: string }[]>('/campuses').then((items) => {
  campusOptions.value.push(...items.map((item) => ({ label: item.name, value: item.id })))
})

const gradeOptions = [
  { label: '2020', value: '2020' },
  { label: '2021', value: '2021' },
  { label: '2022', value: '2022' },
  { label: '2023', value: '2023' },
  { label: '2024', value: '2024' },
  { label: '2025', value: '2025' },
  { label: '2026', value: '2026' },
  { label: '2027', value: '2027' },
  { label: '2028', value: '2028' },
]

const formatGrade = (value: unknown) => String(value ?? '').replace(/级$/, '')

const formatOption = (options: { label: string; value: number }[], value: unknown) =>
  options.find((option) => String(option.value) === String(value))?.label ?? ''

const searchFields: SearchField[] = [
  { label: '班级名称', prop: 'name', type: 'input', placeholder: '请输入班级名称' },
  { label: '年级', prop: 'grade', type: 'select', placeholder: '请选择年级', options: gradeOptions },
]

const tableColumns: TableColumn[] = [
  { label: 'ID', prop: 'id', width: 80 },
  { label: '班级名称', prop: 'name', minWidth: 140 },
  { label: '年级', prop: 'grade', width: 100, formatter: (_row, _column, value) => formatGrade(value) },
  { label: '专业', prop: 'majorId', minWidth: 180, formatter: (_row, _column, value) => formatOption(majorOptions.value, value) },
  { label: '校区', prop: 'campusId', minWidth: 140, formatter: (_row, _column, value) => formatOption(campusOptions.value, value) },
  { label: '人数', prop: 'studentCount', width: 90, align: 'center' },
  { label: '辅导员', prop: 'counselor', width: 110 },
  { label: '创建时间', prop: 'createTime', width: 180 },
  { label: '操作', type: 'action', width: 140, fixed: 'right' },
]

const dialogFields: DialogField[] = [
  { label: '班级名称', prop: 'name', type: 'input', placeholder: '如：软件工程2401班' },
  { label: '年级', prop: 'grade', type: 'select', placeholder: '请选择年级', options: gradeOptions },
  { label: '专业', prop: 'majorId', type: 'select', placeholder: '请选择专业', options: majorOptions.value },
  { label: '校区', prop: 'campusId', type: 'select', placeholder: '请选择校区', options: campusOptions.value, required: false },
  {
    label: '班级人数',
    prop: 'studentCount',
    type: 'number',
    required: false,
    min: 0,
  },
  {
    label: '辅导员',
    prop: 'counselor',
    type: 'input',
    required: false,
    placeholder: '请输入辅导员姓名',
  },
]
</script>

<template>
  <CommonTable
    title="班级管理"
    :search-fields="searchFields"
    :table-columns="tableColumns"
    :dialog-fields="dialogFields"
    api-url="/api/classes"
    add-api="/api/classes"
    edit-api="/api/classes/{id}"
    delete-api="/api/classes/{id}"
    edit-method="put"
    delete-method="delete"
  />
</template>
