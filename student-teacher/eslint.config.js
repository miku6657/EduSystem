import js from '@eslint/js'
import globals from 'globals'
import tseslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'
import vueParser from 'vue-eslint-parser'
import prettier from 'eslint-config-prettier'

/**
 * ESLint 9 扁平配置。
 * - 「essential」级别的 Vue 规则 + TypeScript 推荐规则 + Prettier 关闭格式类规则
 * - 自动生成的声明文件与产物目录不检查
 */
export default [
  {
    ignores: ['dist/**', 'node_modules/**', 'src/auto-imports.d.ts', 'src/components.d.ts'],
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tseslint.parser,
        ecmaVersion: 'latest',
        sourceType: 'module',
        extraFileExtensions: ['.vue'],
      },
    },
  },
  {
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node,
      },
    },
    rules: {
      // 项目里 mock 数据与动态字段较多，显式 any 是刻意为之
      '@typescript-eslint/no-explicit-any': 'off',
      // 页面组件用 index.vue / 单词名是可接受的约定
      'vue/multi-word-component-names': 'off',
      // 空 catch 用于「错误已由请求层提示」的场景，允许
      'no-empty': ['error', { allowEmptyCatch: true }],
    },
  },
  prettier,
]
