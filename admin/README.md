# 教学过程管理系统 · 前端工程

基于 **Vue 3 + Vite + TypeScript** 的《教学过程管理系统》前端工程，PC 端使用 **Element Plus**，移动端 H5 集成 **Vant 4**，内置本地 Mock 与登录/布局骨架。

## 快速开始

```bash
npm install        # 安装依赖
npm run dev        # 启动开发服务器 http://localhost:5173
```

演示账号（由本地 mock 提供）：`admin / 123456`

## 常用命令

| 命令 | 说明 |
| --- | --- |
| `npm run dev` | 启动开发服务器（mock 默认开启） |
| `npm run type-check` | 执行 `vue-tsc` 类型检查 |
| `npm run build` | 类型检查 + 生产构建，产物输出到 `dist/` |
| `npm run preview` | 本地预览生产构建产物 |

## 目录结构

```text
src
├── api/              # 接口定义（按业务模块拆分，如 auth.ts / dashboard.ts）
├── components/       # 公共组件（CommonTable 通用 CRUD、ModulePlaceholder 等）
├── mock/             # 模拟数据（index.ts 统一拦截 /api 请求）
├── router/           # 路由（index.ts + modules/ 业务模块路由，菜单由路由自动生成）
├── stores/           # Pinia 状态（user / app）
├── styles/           # 全局样式
├── types/            # TS 类型（api / user / router meta）
├── utils/            # 工具（axios 请求封装、localStorage 封装）
├── views/            # 页面
│   ├── login/        # 登录页
│   ├── layout/       # 主布局（侧边栏 + 顶部导航）
│   ├── dashboard/    # 工作台
│   ├── mobile/       # H5（Vant）示例页
│   └── <业务模块>/   # 依据需求文档的 8 个业务模块页面
└── main.ts / App.vue
```

## 按需自动导入

`vite.config.ts` 中通过 `unplugin-auto-import` + `unplugin-vue-components` 配置了
Element Plus（PC）与 Vant 4（H5）的自动导入：

- 模板中直接使用 `<el-xxx>` / `<van-xxx>`，无需手动 `import`；
- `ElMessage`、`ElMessageBox` 等编程式组件同样可自动导入；
- 组件样式按需注入（无整包 CSS 引入）；
- 自动生成的 `src/auto-imports.d.ts`、`src/components.d.ts` 请随仓库提交，保证 `vue-tsc` 开箱可用。

## 环境变量与后端联调

```text
.env.development
  VITE_API_BASE_URL=/api              # axios 基础路径
  VITE_PROXY_TARGET=http://localhost:8080  # Vite 代理目标
  VITE_USE_MOCK=true                  # 是否启用本地 mock
```

- 本地开发默认开启 mock（`VITE_USE_MOCK=true`），所有 `/api` 请求由 `src/mock/index.ts` 拦截；
- 后端就绪后，将 `VITE_USE_MOCK` 改为 `false`，`/api` 请求即通过 Vite 代理转发到 `http://localhost:8080`；
- mock 统一返回 `{ code: 0, data: any, msg: string }`，`code !== 0` 时 `utils/request.ts` 会弹出错误提示并 `reject`。

## 路由与侧边栏菜单

侧边栏菜单由 `src/router/modules/` 下的路由配置自动生成（递归渲染），包含 8 个业务模块：

教学基础数据、考务管理、教材管理、教室申请、调课管理、毕业审核、专升本、毕业生管理。

- 路由守卫在 `src/router/index.ts`：未登录访问受保护页面时跳转 `/login`，并携带 `redirect` 参数；
- 每个模块对应 `src/router/modules/<模块>.ts` 路由文件；如“教学基础数据”下已有 课程管理、班级管理两个子页面（`/base-data/course`、`/base-data/class`）；
- 新增模块：在 `src/router/modules/` 下新增路由文件（含 `meta.title`），再在 `modules/index.ts` 聚合即可自动出现在侧边栏；
- 需要子菜单时，给路由记录添加 `children`（侧边栏组件已支持多级递归渲染）。

### 权限菜单与角色

- 侧边栏按 `useUserStore.roles` 动态渲染，角色规则见各路由 `meta.roles`：
  - `admin`（系统管理员）：全部 9 个一级菜单；
  - `manager`（教务处长）：教学基础数据、教室申请、调课管理、毕业审核。
- 当前 mock 登录固定返回 `admin`；预留了 `/api/auth/userinfo`、`/api/auth/roles` 动态获取角色的通道（见 `src/api/auth.ts`、`src/stores/user.ts`）。

## 接口文档

全量接口约定（含管理端/师生端归属标注与 mock 说明）见根目录 **`API_MANUAL.md`**，可转发给后端同学按此联调。

## 通用 CRUD 组件（CommonTable）

`src/components/CommonTable.vue` 通过配置即可渲染“搜索 + 表格 + 分页 + 新增/编辑弹窗 + 单删/批量删除”的完整页面，由以下 Props 驱动：

- `searchFields`：搜索栏配置（input / select / date-picker）；
- `tableColumns`：表格列配置，`type: 'action'` 会自动渲染“编辑/删除”操作列；
- `apiUrl` / `addApi` / `editApi` / `deleteApi`：列表与增删改接口地址（如 `/api/course/list`，自动去掉重复的 `/api` 前缀）；
- `dialogFields`：弹窗表单配置（input / select / date-picker / number / textarea），校验规则按字段自动生成（`required: false` 可取消必填）。

使用示例见 `src/views/base-data/course.vue` 与 `src/views/base-data/class.vue`；对应 mock 数据（每类 10 条，含 `id`、`name`、`createTime`）在 `src/mock/index.ts` 中维护。

## 移动端 H5

- Vant 4 已完成按需自动导入，移动端示例页为 `/mobile`（免登录，便于预览）；
- 移动端与 PC 端共用一套 `api` / `stores` / `types`，业务开发时可按入口拆分页面。
