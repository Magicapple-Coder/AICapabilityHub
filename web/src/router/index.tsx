import { createBrowserRouter } from 'react-router-dom'
import { AppShell } from '@/components/AppShell'
import { AuthGuard } from './AuthGuard'
import { LoginPage } from '@/pages/LoginPage'
import { OverviewPage } from '@/pages/OverviewPage'
import { PlaceholderPage } from '@/pages/PlaceholderPage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <AuthGuard />,
    children: [
      {
        element: <AppShell />,
        children: [
          { index: true, element: <OverviewPage /> },
          { path: 'capabilities', element: <PlaceholderPage title="能力市场" description="浏览和调试已上架的 AI 能力" /> },
          { path: 'settings', element: <PlaceholderPage title="设置" description="管理开发者账号与访问凭证" /> },
          { path: 'usage', element: <PlaceholderPage title="调用记录" description="查看能力调用与计量信息" /> },
        ],
      },
    ],
  },
  { path: '*', element: <LoginPage /> },
])
