<script setup lang="ts">
import CommonTable from '@/components/CommonTable.vue'
import type { DialogField, SearchField, TableColumn } from '@/types/table'

const searchFields: SearchField[] = [
  { label: '教材名称', prop: 'name', type: 'input', placeholder: '请输入教材名称' },
]

const tableColumns: TableColumn[] = [
  { label: 'ID', prop: 'id', width: 80 },
  { label: '教材名称', prop: 'name', minWidth: 200 },
  { label: 'ISBN', prop: 'isbn', width: 160 },
  { label: '出版社', prop: 'publisher', minWidth: 160 },
  { label: '入库数量', prop: 'quantity', width: 100, align: 'center' },
  { label: '入库日期', prop: 'stockDate', width: 120 },
  { label: '操作', type: 'action', width: 140, fixed: 'right' },
]

/** 后端联调前仅提供列表查询与删除，新增/编辑提示“待联调” */
const dialogFields: DialogField[] = []
</script>

<template>
  <CommonTable
    title="新书入库"
    :search-fields="searchFields"
    :table-columns="tableColumns"
    :dialog-fields="dialogFields"
    api-url="/api/textbook/stock-in/list"
    delete-api="/api/textbook/stock-in/delete"
  />
</template>
