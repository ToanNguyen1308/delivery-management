import type { DescriptionsProps } from 'antd';

/**
 * Số cột Descriptions trên trang chi tiết.
 * Breakpoint Ant Design theo bề rộng màn hình, không phải ô chứa.
 * Menu trái + cột bản đồ làm ô còn ~110px ở 1024px, chữ bị bẻ dọc — chỉ 2 cột từ 1600px.
 */
export const DETAIL_DESCRIPTIONS_COLUMN: DescriptionsProps['column'] = {
  xs: 1,
  sm: 1,
  md: 1,
  lg: 1,
  xl: 1,
  xxl: 2,
};

export const DETAIL_DESCRIPTIONS_STYLES: DescriptionsProps['styles'] = {
  label: { whiteSpace: 'nowrap', width: 170 },
};
