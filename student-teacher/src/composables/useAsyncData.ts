import { ref } from 'vue'
import type { Ref } from 'vue'

/**
 * 通用异步数据加载：统一 loading / error / reload，避免每个页面各写一遍 try-catch。
 *
 * 用法：
 *   const { data, loading, error, reload } = useAsyncData<ExamScore[]>(() => listMyScores(id), [])
 *   onMounted(reload)
 */
export function useAsyncData<T>(loader: () => Promise<T>, initial: T) {
  const data = ref(initial) as Ref<T>
  const loading = ref(false)
  const error = ref('')
  const loaded = ref(false)

  async function reload(): Promise<void> {
    loading.value = true
    error.value = ''
    try {
      data.value = await loader()
      loaded.value = true
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载失败'
    } finally {
      loading.value = false
    }
  }

  return { data, loading, error, loaded, reload }
}
