import { Tag } from 'antd';
import type { EnumValue } from '@/types';

interface StatusTagProps {
  value?: EnumValue | null;
  colorMap?: Record<string, string>;
}

/** Hien thi enum tra ve tu backend kem mau sac tuong ung. */
const StatusTag = ({ value, colorMap }: StatusTagProps) => {
  if (!value) {
    return <Tag>-</Tag>;
  }
  return <Tag color={colorMap?.[value.code] ?? 'default'}>{value.description}</Tag>;
};

export default StatusTag;
