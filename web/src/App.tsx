import { RouterProvider } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { router } from '@/router'

export default function App() {
  return <ConfigProvider locale={zhCN} theme={{ token: { colorPrimary: '#c9f05b', colorText: '#f4f4ef', colorBgContainer: '#151815', borderRadius: 0, fontFamily: 'Outfit, PingFang SC, Microsoft YaHei, sans-serif' } }}><RouterProvider router={router} /></ConfigProvider>
}
