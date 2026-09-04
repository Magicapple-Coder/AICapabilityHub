import { useState } from 'react'
import { Alert, Button, Card, Form, Input, Typography, message } from 'antd'
import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { useLocation, useNavigate } from 'react-router-dom'
import { login } from '@/api/auth'
import type { LoginPayload } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import { BrandMark } from '@/components/BrandMark'

export function LoginPage() {
  const [form] = Form.useForm<LoginPayload>()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const location = useLocation()
  const setAuth = useAuthStore((state) => state.setAuth)

  const onFinish = async (values: LoginPayload) => {
    setLoading(true)
    setError('')
    try {
      const data = await login(values)
      setAuth(data)
      message.success('登录成功')
      const target = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname || '/'
      navigate(target, { replace: true })
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '登录失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="login-page">
      <div className="login-page__aside">
        <BrandMark />
        <div>
          <Typography.Title level={1}>连接每一种 AI 能力</Typography.Title>
          <Typography.Paragraph>统一接入、快速调试，让能力交付更简单。</Typography.Paragraph>
        </div>
        <Typography.Text type="secondary">AI Capability Hub · Developer Preview</Typography.Text>
      </div>
      <Card className="login-card" bordered={false}>
        <Typography.Title level={2}>欢迎回来</Typography.Title>
        <Typography.Paragraph type="secondary">登录开发者工作台，管理你的能力调用。</Typography.Paragraph>
        {error && <Alert className="login-card__alert" type="error" showIcon message={error} />}
        <Form form={form} layout="vertical" onFinish={onFinish} initialValues={{ username: 'developer' }} requiredMark={false}>
          <Form.Item name="username" label="账号" rules={[{ required: true, message: '请输入账号' }]}>
            <Input size="large" prefix={<UserOutlined />} placeholder="请输入账号" autoComplete="username" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password size="large" prefix={<LockOutlined />} placeholder="请输入密码" autoComplete="current-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" size="large" block loading={loading}>登录</Button>
        </Form>
      </Card>
    </main>
  )
}
