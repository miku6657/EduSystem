<script setup lang="ts">
import CommonTable from '@/components/CommonTable.vue'
import type { DialogField, SearchField, TableColumn } from '@/types/table'

const statusOptions = [
  { label: '待审核', value: '待审核' },
  { label: '已通过', value: '已通过' },
  { label: '已驳回', value: '已驳回' },
]

const searchFields: SearchField[] = [
  { label: '课程名', prop: 'courseName', type: 'input', placeholder: '请输入课程名' },
  { label: '审批状态', prop: 'status', type: 'select', placeholder: '请选择审批状态', options: statusOptions },
]

const tableColumns: TableColumn[] = [
  { label: 'ID', prop: 'id', width: 80 },
  { label: '课程名', prop: 'courseName', minWidth: 150 },
  { label: '调课原因', prop: 'reason', minWidth: 240 },
  { label: '原时间', prop: 'originalTime', width: 170 },
  { label: '新时间', prop: 'newTime', width: 170 },
  { label: '审批状态', prop: 'status', width: 100, align: 'center' },
  { label: '操作', type: 'action', width: 140, fixed: 'right' },
]

/** 后端联调前仅提供列表查询与删除，新增/编辑提示“待联调” */
const dialogFields: DialogField[] = []
</script>

<template>
  <CommonTable
    title="我的调课"
    :search-fields="searchFields"
    :table-columns="tableColumns"
    :dialog-fields="dialogFields"
    api-url="/api/course-adjust/my/list"
    delete-api="/api/course-adjust/my/delete"
  />
</template>
