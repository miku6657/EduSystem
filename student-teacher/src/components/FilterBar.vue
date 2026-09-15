<script setup lang="ts">
import { computed } from 'vue'

/**
 * 筛选栏：对齐 admin 端 CommonTable 的搜索区（ct__search）在移动端的等价实现。
 *
 * admin 用 `searchFields: SearchField[]` 配置化描述搜索项，这里沿用同样的配置思路：
 *   - `type: 'input'`  → 顶部搜索框（对应 el-input）
 *   - `type: 'select'` → 下拉筛选（对应 el-select）
 *
 * 用法：
 *   <FilterBar v-model="query" :fields="filterFields" @change="reload" />
 */
export interface FilterField {
  label: string
  prop: string
  type?: 'input' | 'select'
  placeholder?: string
  options?: Array<{ text: string; value: string | number }>
}

const props = withDefaults(
  defineProps<{
    fields: FilterField[]
    modelValue: Record<string, string | number | undefined>
    /** 输入框占位文案 */
    searchPlaceholder?: string
  }>(),
  {
    searchPlaceholder: '请输入关键字搜索',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: Record<string, string | number | undefined>): void
  (e: 'change'): void
}>()

const inputField = computed(() =>
  props.fields.find((field) => !field.type || field.type === 'input'),
)
const selectFields = computed(() => props.fields.filter((field) => field.type === 'select'))

const keyword = computed({
  get: () => String(props.modelValue[inputField.value?.prop ?? 'keyword'] ?? ''),
  set: (value: string) => update(inputField.value?.prop ?? 'keyword', value),
})

function update(prop: string, value: string | number | undefined) {
  emit('update:modelValue', { ...props.modelValue, [prop]: value })
  emit('change')
}
</script>

<template>
  <div class="st-filter">
    <van-search
      v-if="inputField"
      v-model="keyword"
      class="st-filter__search"
      :placeholder="inputField.placeholder || `请输入${inputField.label}`"
      @search="emit('change')"
      @clear="emit('change')"
    />
    <van-dropdown-menu v-if="selectFields.length" class="st-filter__menu">
      <van-dropdown-item
        v-for="field in selectFields"
        :key="field.prop"
        :title="field.label"
        :model-value="modelValue[field.prop]"
        :options="field.options ?? []"
        @update:model-value="(value) => update(field.prop, value as string | number)"
      />
    </van-dropdown-menu>
  </div>
</template>

<style scoped>
.st-filter {
  display: block;
  margin-bottom: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--st-border);
  border-radius: var(--st-radius);
}

.st-filter__search {
  padding: 4px 8px;
}

.st-filter__menu :deep(.van-dropdown-menu__bar) {
  box-shadow: none;
}
</style>
