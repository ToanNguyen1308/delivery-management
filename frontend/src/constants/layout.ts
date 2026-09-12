import type { DescriptionsProps } from 'antd';

/**
 * So cot cho bang Descriptions nam trong cot hep cua trang chi tiet.
 *
 * Breakpoint cua Ant Design tinh theo be rong man hinh, khong phai be rong o chua.
 * Trang chi tiet con bi menu ben trai an mat 230px roi chia doi voi cot ban do,
 * nen o man hinh 1024px moi o chi con khoang 110px va chu bi be doc tung ky tu.
 * Vi vay chi xep hai cap nhan/gia tri tren mot dong tu 1600px tro len.
 */
export const DETAIL_DESCRIPTIONS_COLUMN: DescriptionsProps['column'] = {
  xs: 1,
  sm: 1,
  md: 1,
  lg: 1,
  xl: 1,
  xxl: 2,
};

/** Giu nhan tren mot dong va co be rong on dinh de bang khong bi xo lech. */
export const DETAIL_DESCRIPTIONS_STYLES: DescriptionsProps['styles'] = {
  label: { whiteSpace: 'nowrap', width: 170 },
};
