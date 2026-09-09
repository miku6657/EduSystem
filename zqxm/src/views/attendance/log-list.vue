<script setup lang="ts">
import CommonTable from '@/components/CommonTable.vue'
import type { DialogField, SearchField, TableColumn } from '@/types/table'

const checkStatusOptions = [
  { label: '正常', value: '正常' },
  { label: '迟到', value: '迟到' },
  { label: '缺勤', value: '缺勤' },
]

const searchFields: SearchField[] = [
  { label: '教师姓名', prop: 'teacherName', type: 'input', placeholder: '请输入教师姓名' },
  { label: '打卡状态', prop: 'checkStatus', type: 'select', placeholder: '请选择打卡状态', options: checkStatusOptions },
]

const tableColumns: TableColumn[] = [
  { label: 'ID', prop: 'id', width: 80 },
  { label: '教师姓名', prop: 'teacherName', width: 120 },
  { label: '上课日期', prop: 'courseDate', width: 120 },
  { label: '课程名', prop: 'courseName', minWidth: 160 },
  { label: '打卡状态', prop: 'checkStatus', width: 100, align: 'center' },
  { label: '学生出勤率', prop: 'attendanceRate', width: 110, align: 'center' },
  { label: '操作', type: 'action', width: 140, fixed: 'right' },
]

/** 后端联调前仅提供列表查询与删除，新增/编辑提示“待联调” */
const dialogFields: DialogField[] = []
</script>

<template>
  <CommonTable
    title="教师考勤日志"
    :search-fields="searchFields"
    :table-columns="tableColumns"
    :dialog-fields="dialogFields"
    api-url="/api/attendance/log/list"
    delete-api="/api/attendance/log/delete"
  />
</template>
