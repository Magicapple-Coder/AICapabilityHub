import { Layout, Menu, Space, Typography, Button } from 'antd'
import { AppstoreOutlined, ApiOutlined, LogoutOutlined, SettingOutlined } from '@ant-design/icons'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'
import { BrandMark } from './BrandMark'

const { Header, Sider, Content } = Layout

export function AppShell() {
  const location = useLocation()
  const navigate = useNavigate()
  const { userName, clearAuth } = useAuthStore()
  const selected = location.pathname.startsWith('/capabilities') ? 'capabilities' : 'overview'

  return (
    <Layout className="app-shell">
      <Sider width={236} className="app-sider" breakpoint="lg" collapsedWidth={0}>
        <div className="app-sider__brand"><BrandMark /></div>
        <Menu
          theme="light"
          mode="inline"
          selectedKeys={[selected]}
          items={[
            { key: 'overview', icon: <AppstoreOutlined />, label: '工作台', onClick: () => navigate('/') },
            { key: 'capabilities', icon: <ApiOutlined />, label: '能力市场', onClick: () => navigate('/capabilities') },
            { key: 'settings', icon: <SettingOutlined />, label: '设置', onClick: () => navigate('/settings') },
          ]}
        />
      </Sider>
      <Layout>
        <Header className="app-header">
          <Typography.Text className="app-header__title">开发者工作台</Typography.Text>
          <Space size="middle">
            <Typography.Text type="secondary">{userName || '开发者'}</Typography.Text>
            <Button type="text" icon={<LogoutOutlined />} aria-label="退出登录" onClick={() => { clearAuth(); navigate('/login') }} />
          </Space>
        </Header>
        <Content className="app-content"><Outlet /></Content>
      </Layout>
    </Layout>
  )
}
