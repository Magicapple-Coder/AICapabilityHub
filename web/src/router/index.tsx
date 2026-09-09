import { createBrowserRouter } from 'react-router-dom'
import { AppShell } from '@/components/AppShell'
import { AuthGuard } from './AuthGuard'
import { LoginPage } from '@/pages/LoginPage'
import { OverviewPage } from '@/pages/OverviewPage'
import { CapabilitiesPage } from '@/pages/CapabilitiesPage'
import { ChatPage } from '@/pages/ChatPage'
import { UsagePage } from '@/pages/UsagePage'
import { SettingsPage } from '@/pages/SettingsPage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <AuthGuard />,
    children: [
      {
        element: <AppShell />,
        children: [
          { index: true, element: <OverviewPage /> },
          { path: 'capabilities', element: <CapabilitiesPage /> },
          { path: 'chat', element: <ChatPage /> },
          { path: 'settings', element: <SettingsPage /> },
          { path: 'usage', element: <UsagePage /> },
        ],
      },
    ],
  },
  { path: '*', element: <LoginPage /> },
])
