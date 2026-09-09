<script setup lang="ts">
import CommonTable from '@/components/CommonTable.vue'
import type { DialogField, SearchField, TableColumn } from '@/types/table'

const gradeOptions = [
  { label: '2023级', value: '2023级' },
  { label: '2024级', value: '2024级' },
  { label: '2025级', value: '2025级' },
]

const majorOptions = [
  { label: '计算机科学与技术', value: '计算机科学与技术' },
  { label: '软件工程', value: '软件工程' },
  { label: '网络工程', value: '网络工程' },
  { label: '人工智能', value: '人工智能' },
  { label: '数据科学与大数据技术', value: '数据科学与大数据技术' },
  { label: '物联网工程', value: '物联网工程' },
]

const searchFields: SearchField[] = [
  { label: '班级名称', prop: 'name', type: 'input', placeholder: '请输入班级名称' },
  { label: '年级', prop: 'grade', type: 'select', placeholder: '请选择年级', options: gradeOptions },
]

const tableColumns: TableColumn[] = [
  { label: 'ID', prop: 'id', width: 80 },
  { label: '班级名称', prop: 'name', minWidth: 140 },
  { label: '年级', prop: 'grade', width: 100 },
  { label: '专业', prop: 'major', minWidth: 180 },
  { label: '人数', prop: 'studentCount', width: 90, align: 'center' },
  { label: '辅导员', prop: 'headTeacher', width: 110 },
  { label: '入学日期', prop: 'enrollDate', width: 120 },
  { label: '创建时间', prop: 'createTime', width: 180 },
  { label: '操作', type: 'action', width: 140, fixed: 'right' },
]

const dialogFields: DialogField[] = [
  { label: '班级名称', prop: 'name', type: 'input', placeholder: '如：软件工程2401班' },
  { label: '年级', prop: 'grade', type: 'select', placeholder: '请选择年级', options: gradeOptions },
  { label: '专业', prop: 'major', type: 'select', placeholder: '请选择专业', options: majorOptions },
  {
    label: '班级人数',
    prop: 'studentCount',
    type: 'number',
    required: false,
    min: 0,
  },
  {
    label: '辅导员',
    prop: 'headTeacher',
    type: 'input',
    required: false,
    placeholder: '请输入辅导员姓名',
  },
  {
    label: '入学日期',
    prop: 'enrollDate',
    type: 'date-picker',
    required: false,
  },
]
</script>

<template>
  <CommonTable
    title="班级管理"
    :search-fields="searchFields"
    :table-columns="tableColumns"
    :dialog-fields="dialogFields"
    api-url="/api/class/list"
    add-api="/api/class/add"
    edit-api="/api/class/edit"
    delete-api="/api/class/delete"
  />
</template>
