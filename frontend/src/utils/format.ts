import dayjs from 'dayjs';

const CURRENCY_FORMATTER = new Intl.NumberFormat('vi-VN');

export const formatMoney = (value?: number | null): string =>
  value === undefined || value === null ? '0 đ' : `${CURRENCY_FORMATTER.format(value)} đ`;

export const formatNumber = (value?: number | null): string =>
  value === undefined || value === null ? '0' : CURRENCY_FORMATTER.format(value);

export const formatDateTime = (value?: string | null): string =>
  value ? dayjs(value).format('DD/MM/YYYY HH:mm') : '-';

export const formatDate = (value?: string | null): string =>
  value ? dayjs(value).format('DD/MM/YYYY') : '-';

export const formatTime = (value?: string | null): string =>
  value ? dayjs(value).format('HH:mm:ss') : '-';

export const downloadBlob = (blob: Blob, fileName: string): void => {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};
