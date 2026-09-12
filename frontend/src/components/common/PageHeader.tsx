import { ReactNode } from 'react';
import { Space, Typography } from 'antd';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  extra?: ReactNode;
}

const PageHeader = ({ title, subtitle, extra }: PageHeaderProps) => (
  <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
    <div>
      <Typography.Title level={4} style={{ margin: 0 }}>
        {title}
      </Typography.Title>
      {subtitle && <Typography.Text type="secondary">{subtitle}</Typography.Text>}
    </div>
    {extra && <Space wrap>{extra}</Space>}
  </div>
);

export default PageHeader;
