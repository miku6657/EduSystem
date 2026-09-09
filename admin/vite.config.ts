import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver, VantResolver } from 'unplugin-vue-components/resolvers'
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

      // Element Plus（PC）与 Vant 4（H5）按需自动导入
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia'],
        resolvers: [ElementPlusResolver()],
        dts: 'src/auto-imports.d.ts',
      }),
      Components({
        resolvers: [ElementPlusResolver(), VantResolver()],
        dts: 'src/components.d.ts',
      }),

      // mock：拦截所有 /api 请求并返回本地模拟数据（仅开发环境）
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
      port: 5173,
      proxy: {
        // /api 代理到后端，方便后续联调（mock 关闭时生效）
        '/api': {
          target: env.VITE_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
          // 若后端接口本身不带 /api 前缀，放开下行注释
          // rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },
  }
})
