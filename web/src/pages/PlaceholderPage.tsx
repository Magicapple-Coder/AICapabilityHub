import { Empty, Typography } from 'antd'

interface PlaceholderPageProps { title: string; description: string }

export function PlaceholderPage({ title, description }: PlaceholderPageProps) {
  return <div className="page-wrap"><div className="page-heading"><div><Typography.Title level={2}>{title}</Typography.Title><Typography.Text type="secondary">{description}</Typography.Text></div></div><div className="empty-panel"><Empty description="页面准备中" /></div></div>
}
