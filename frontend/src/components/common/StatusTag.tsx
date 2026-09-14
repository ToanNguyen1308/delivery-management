import { Tag } from 'antd';
import type { EnumValue } from '@/types';

interface StatusTagProps {
  value?: EnumValue | null;
  colorMap?: Record<string, string>;
}

const StatusTag = ({ value, colorMap }: StatusTagProps) => {
  if (!value) {
    return <Tag>-</Tag>;
  }
  return <Tag color={colorMap?.[value.code] ?? 'default'}>{value.description}</Tag>;
};

export default StatusTag;
