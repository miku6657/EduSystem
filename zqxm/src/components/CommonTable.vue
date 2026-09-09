<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { Delete, Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import { http } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type { DialogField, FieldConfig, SearchField, TableColumn } from '@/types/table'

type Row = Record<string, any>

const props = withDefaults(
  defineProps<{
    /** 页面标题（卡片头部展示） */
    title?: string
    /** 搜索栏配置 */
    searchFields?: SearchField[]
    /** 表格列配置 */
    tableColumns?: TableColumn[]
    /** 新增 / 编辑弹窗表单配置 */
    dialogFields?: DialogField[]
    /** 列表查询接口，如 /api/course/list */
    apiUrl: string
    /** 新增接口，如 /api/course/add；未提供时点击“新增”提示“待联调” */
    addApi?: string
    /** 编辑接口，如 /api/course/edit；未提供时点击“编辑”提示“待联调” */
    editApi?: string
    /** 删除接口，如 /api/course/delete（按 id 删除） */
    deleteApi: string
    /** 每页条数，默认 10 */
    pageSize?: number
    /** 行主键，默认 id */
    rowKey?: string
  }>(),
  {
    title: '',
    addApi: '',
    editApi: '',
    searchFields: () => [],
    tableColumns: () => [],
    dialogFields: () => [],
    pageSize: 10,
    rowKey: 'id',
  },
)

const tableRef = ref<{ clearSelection: () => void }>()
const dialogFormRef = ref<FormInstance>()

const rows = ref<Row[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(props.pageSize)
const loading = ref(false)

const selectedRows = ref<Row[]>([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingRow = ref<Row | null>(null)

const searchParams = reactive<Record<string, any>>({})
const dialogForm = reactive<Record<string, any>>({})

/** 用字段默认值重置一个表单对象 */
function initForm(target: Record<string, any>, fields: FieldConfig[]) {
  for (const key of Object.keys(target)) {
    delete target[key]
  }
  for (const field of fields) {
    target[field.prop] = field.defaultValue ?? (field.type === 'number' ? undefined : '')
  }
}

initForm(searchParams, props.searchFields)

/** props 传参形如 /api/xxx，去掉前缀避免与 axios baseURL=/api 重复 */
function resolveUrl(url: string) {
  return url.startsWith('/api') ? url.slice('/api'.length) : url
}

/** 查询列表 */
const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, pageSize: pageSize.value }
    for (const key of Object.keys(searchParams)) {
      const value = searchParams[key]
      if (value !== '' && value !== null && value !== undefined) {
        params[key] = value
      }
    }
    const data = await http.get<PageResult<Row>>(resolveUrl(props.apiUrl), params)
    rows.value = data.list
    total.value = data.total
    // 删除后当前页可能为空：自动回退一页
    if (rows.value.length === 0 && page.value > 1) {
      page.value -= 1
      await loadData()
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handleReset = () => {
  initForm(searchParams, props.searchFields)
  handleSearch()
}

const handlePageChange = (current: number) => {
  page.value = current
  loadData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  page.value = 1
  loadData()
}

/** ==================== 新增 / 编辑 ==================== */

function resetDialogForm() {
  initForm(dialogForm, props.dialogFields)
  editingRow.value = null
}

function fillDialogForm(row: Row) {
  initForm(dialogForm, props.dialogFields)
  for (const field of props.dialogFields) {
    const value = row[field.prop]
    if (value !== undefined && value !== null) {
      dialogForm[field.prop] = value
    }
  }
}

const dialogTitle = computed(() => (editingRow.value ? '编辑' : '新增'))

/** 弹窗校验规则：根据 dialogFields 自动生成 */
const dialogRules = computed<FormRules>(() => {
  const rules: FormRules = {}
  for (const field of props.dialogFields) {
    if (field.required === false) {
      continue
    }
    const trigger = field.type === 'select' || field.type === 'date-picker' ? 'change' : 'blur'
    rules[field.prop] = [{ required: true, message: `${field.label}不能为空`, trigger }]
  }
  return rules
})

const openAddDialog = () => {
  if (!props.addApi) {
    ElMessage.info('待联调')
    return
  }
  resetDialogForm()
  dialogVisible.value = true
}

const openEditDialog = (row: Row) => {
  if (!props.editApi) {
    ElMessage.info('待联调')
    return
  }
  fillDialogForm(row)
  editingRow.value = row
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!dialogFormRef.value) {
    return
  }
  const valid = await dialogFormRef.value.validate().then(() => true).catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload: Record<string, unknown> = {}
    for (const field of props.dialogFields) {
      payload[field.prop] = dialogForm[field.prop]
    }

    if (editingRow.value) {
      payload[props.rowKey] = editingRow.value[props.rowKey]
      await http.post<unknown>(resolveUrl(props.editApi), payload)
      ElMessage.success('修改成功')
      dialogVisible.value = false
      await loadData()
    } else {
      await http.post<unknown>(resolveUrl(props.addApi), payload)
      ElMessage.success('新增成功')
      dialogVisible.value = false
      page.value = 1
      await loadData()
    }
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    saving.value = false
  }
}

/** ==================== 删除 ==================== */

const handleDelete = async (row: Row) => {
  const displayName = row.name ?? row[props.rowKey]
  try {
    await ElMessageBox.confirm(`确定删除「${displayName}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  await http.post<unknown>(resolveUrl(props.deleteApi), { [props.rowKey]: row[props.rowKey] })
  ElMessage.success('删除成功')
  loadData()
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先勾选需要删除的数据')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedRows.value.length} 条数据吗？`,
      '批量删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
      },
    )
  } catch {
    return
  }

  for (const row of selectedRows.value) {
    await http.post<unknown>(resolveUrl(props.deleteApi), { [props.rowKey]: row[props.rowKey] })
  }
  ElMessage.success('批量删除成功')
  tableRef.value?.clearSelection()
  loadData()
}

/** ==================== 列解析 ==================== */

/** 除操作列以外的数据列（selection 由组件统一前置渲染） */
const dataColumns = computed(() =>
  props.tableColumns.filter(
    (col) => col.type !== 'action' && col.type !== 'selection',
  ),
)

const actionColumn = computed(
  () => props.tableColumns.find((col) => col.type === 'action') ?? null,
)

const selectionColumn = computed(
  () => props.tableColumns.find((col) => col.type === 'selection') ?? null,
)

function resolveColumnWidth(col: TableColumn) {
  if (col.width) {
    return col.width
  }
  if (col.type === 'selection') {
    return 48
  }
  if (col.type === 'index') {
    return 65
  }
  return undefined
}

/** 首次渲染后加载数据 */
loadData()

defineExpose({ reload: loadData, search: handleSearch, reset: handleReset })
</script>

<template>
  <el-card shadow="never" class="common-table">
    <template #header>
      <div class="ct__header">
        <span v-if="title" class="ct__title">{{ title }}</span>
        <div class="ct__header-actions">
          <el-button type="primary" :icon="Plus" @click="openAddDialog">新增</el-button>
          <el-button
            type="danger"
            :icon="Delete"
            :disabled="selectedRows.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
      </div>
    </template>

    <!-- 搜索栏 -->
    <el-form v-if="searchFields.length" class="ct__search" inline @submit.prevent="handleSearch">
      <el-form-item v-for="field in searchFields" :key="field.prop" :label="field.label">
        <el-select
          v-if="field.type === 'select'"
          v-model="searchParams[field.prop]"
          class="ct__control"
          clearable
          :placeholder="field.placeholder || `请选择${field.label}`"
        >
          <el-option
            v-for="option in field.options ?? []"
            :key="String(option.value)"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-date-picker
          v-else-if="field.type === 'date-picker'"
          v-model="searchParams[field.prop]"
          class="ct__control"
          type="date"
          :value-format="field.valueFormat || 'YYYY-MM-DD'"
          :placeholder="field.placeholder || `请选择${field.label}`"
        />
        <el-input
          v-else
          v-model="searchParams[field.prop]"
          class="ct__control"
          clearable
          :placeholder="field.placeholder || `请输入${field.label}`"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      class="ct__table"
      :data="rows"
      :row-key="rowKey"
      border
      stripe
      @selection-change="selectedRows = $event"
    >
      <el-table-column
        v-if="selectionColumn || deleteApi"
        type="selection"
        :width="resolveColumnWidth(selectionColumn ?? { type: 'selection' })"
        align="center"
      />
      <el-table-column
        v-for="col in dataColumns"
        :key="col.prop || col.type || col.label"
        :type="col.type"
        :prop="col.prop"
        :label="col.label"
        :width="resolveColumnWidth(col)"
        :min-width="col.minWidth"
        :align="col.align ?? (col.type ? 'center' : 'left')"
        :fixed="col.fixed"
        :show-overflow-tooltip="!!col.prop && col.showOverflowTooltip !== false"
      />
      <el-table-column
        v-if="actionColumn"
        :label="actionColumn.label || '操作'"
        :width="actionColumn.width ?? 140"
        align="center"
        :fixed="actionColumn.fixed ?? 'right'"
      >
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="ct__pagination">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page="page"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetDialogForm"
    >
      <el-form
        ref="dialogFormRef"
        class="ct__dialog-form"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="96px"
      >
        <el-form-item
          v-for="field in dialogFields"
          :key="field.prop"
          :label="field.label"
          :prop="field.prop"
        >
          <el-select
            v-if="field.type === 'select'"
            v-model="dialogForm[field.prop]"
            :placeholder="field.placeholder || `请选择${field.label}`"
          >
            <el-option
              v-for="option in field.options ?? []"
              :key="String(option.value)"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          <el-date-picker
            v-else-if="field.type === 'date-picker'"
            v-model="dialogForm[field.prop]"
            type="date"
            :value-format="field.valueFormat || 'YYYY-MM-DD'"
            :placeholder="field.placeholder || `请选择${field.label}`"
          />
          <el-input-number
            v-else-if="field.type === 'number'"
            v-model="dialogForm[field.prop]"
            :min="field.min ?? 0"
            controls-position="right"
          />
          <el-input
            v-else-if="field.type === 'textarea'"
            v-model="dialogForm[field.prop]"
            type="textarea"
            :rows="3"
            :placeholder="field.placeholder || `请输入${field.label}`"
          />
          <el-input
            v-else
            v-model="dialogForm[field.prop]"
            :placeholder="field.placeholder || `请输入${field.label}`"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.ct__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ct__title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.ct__header-actions {
  display: flex;
  gap: 8px;
}

.ct__search {
  margin-bottom: 4px;
}

.ct__control {
  width: 200px;
}

.ct__table {
  width: 100%;
}

.ct__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.ct__dialog-form :deep(.el-select),
.ct__dialog-form :deep(.el-date-editor),
.ct__dialog-form :deep(.el-input-number),
.ct__dialog-form :deep(.el-input) {
  width: 100%;
}
</style>
