# Web 前端

基于 React 19、Vite、TypeScript、Ant Design 5、React Router 7、Zustand 和 Axios 的前端骨架。

## 启动

```powershell
pnpm install
pnpm dev
```

默认访问 <http://localhost:5173>。复制 `.env.example` 为 `.env` 后可配置网关地址；开发阶段 `VITE_USE_MOCK_LOGIN=true` 会使用占位登录，不调用后端。

## 目录

- `src/api`：Axios 客户端与接口封装
- `src/pages`：页面入口（当前仅登录和占位页）
- `src/components`：布局及可复用组件
- `src/stores`：Zustand 状态
- `src/router`：路由及登录守卫
- `src/utils`：环境配置工具
