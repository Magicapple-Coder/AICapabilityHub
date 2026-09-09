# Web 前端

基于 React 19、Vite、TypeScript、Tailwind CSS 4、GSAP ScrollTrigger、Ant Design 5、React Router 7、Zustand 和 Axios 的开发者工作台。

## 启动

```powershell
pnpm install
pnpm dev
```

默认访问 <http://localhost:5173>。复制 `.env.example` 为 `.env` 后可配置网关地址；开发阶段 `/api` 由 Vite 代理到 `VITE_DEV_GATEWAY_URL`（默认 `http://localhost:8080`）。`VITE_USE_MOCK_LOGIN=true` 会使用占位登录，不调用后端。

页面路由：`/login` 登录、`/` 工作台、`/capabilities` 能力市场、`/chat` 在线调试、`/usage` 调用记录、`/settings` 开发者中心。首页包含能力 Bento、滚动固定内容和逐词揭示动效；能力和调试页在后端业务接口完成前使用可重复的骨架数据与 Mock 回退。

## Nginx 生产入口

仓库根目录执行 `docker compose -f deploy/docker-compose.yml up -d --build nginx` 时，Compose 会用 Node 24 构建当前前端，并由 Nginx 托管 `dist`；浏览器访问 <http://localhost>（或 `NGINX_PORT` 配置的端口）。Nginx 将 `/api/**` 同源转发到 Spring Cloud Gateway，前端不需要直连 Python 服务。

生产构建默认使用 Mock 登录。需要切换真实登录接口时，在 `deploy/.env` 设置 `VITE_USE_MOCK_LOGIN=false` 后执行 `docker compose -f deploy/docker-compose.yml up -d --build nginx`。

## 目录

- `src/api`：Axios 客户端与接口封装
- `src/pages`：页面入口（当前仅登录和占位页）
- `src/components`：布局及可复用组件
- `src/stores`：Zustand 状态
- `src/router`：路由及登录守卫
- `src/utils`：环境配置工具
