import { Card, Col, Row, Statistic, Tag, Typography } from 'antd'
import { ApiOutlined, CloudServerOutlined, ThunderboltOutlined } from '@ant-design/icons'

export function OverviewPage() {
  return (
    <div className="page-wrap">
      <div className="page-heading">
        <div><Typography.Title level={2}>工作台</Typography.Title><Typography.Text type="secondary">查看平台接入状态与近期概览</Typography.Text></div>
        <Tag color="green">开发环境</Tag>
      </div>
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}><Card><Statistic title="可用能力" value={0} prefix={<ApiOutlined />} suffix="项" /></Card></Col>
        <Col xs={24} md={8}><Card><Statistic title="本月调用" value={0} prefix={<ThunderboltOutlined />} suffix="次" /></Card></Col>
        <Col xs={24} md={8}><Card><Statistic title="服务状态" value="正常" prefix={<CloudServerOutlined />} /></Card></Col>
      </Row>
      <Card className="empty-panel" title="开始使用">
        <Typography.Paragraph type="secondary">能力市场和调用记录将在服务接入后展示。</Typography.Paragraph>
      </Card>
    </div>
  )
}
