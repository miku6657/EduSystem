import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'
import { viteMockServe } from 'vite-plugin-mock'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // 联调真实后端时：.env.development 中把 VITE_USE_MOCK 改为 false 即可关闭 mock
  const enableMock = env.VITE_USE_MOCK !== 'false'

  return {
    plugins: [
      vue(),

      // 自动导入 vue / vue-router / pinia 的 API（ref、computed、onMounted 等无需手写 import）
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia'],
        dts: 'src/auto-imports.d.ts',
      }),

      // Vant 4 组件按需自动导入；样式统一在 main.ts 引入 vant/lib/index.css，故此处关闭 importStyle
      Components({
        resolvers: [VantResolver({ importStyle: false })],
        dts: 'src/components.d.ts',
      }),

      // mock：拦截 /api 请求返回本地模拟数据（仅开发环境）
      viteMockServe({
        mockPath: 'src/mock',
        enable: command === 'serve' && enableMock,
        watchFiles: true,
        logger: true,
      }),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      host: '0.0.0.0',
      port: 5174,
      proxy: {
        // /api 代理到后端（mock 关闭时生效）
        '/api': {
          target: env.VITE_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
  }
})
