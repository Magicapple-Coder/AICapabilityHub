import { RouterProvider } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { router } from '@/router'

export default function App() {
  return <ConfigProvider locale={zhCN} theme={{ token: { colorPrimary: '#1769e0', borderRadius: 6, fontFamily: '"Inter", "PingFang SC", "Microsoft YaHei", sans-serif' } }}><RouterProvider router={router} /></ConfigProvider>
}
