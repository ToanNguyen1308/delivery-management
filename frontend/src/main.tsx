import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider, App as AntdApp } from 'antd';
import viVN from 'antd/locale/vi_VN';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import dayjs from 'dayjs';
import 'dayjs/locale/vi';
import 'leaflet/dist/leaflet.css';
import App from './App';
import './index.css';

dayjs.locale('vi');

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, refetchOnWindowFocus: false, staleTime: 15000 },
  },
});

const theme = {
  token: {
    colorPrimary: '#d32f2f',
    borderRadius: 6,
    fontFamily: "'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
  },
};

// Cac lenh message.success / Modal.confirm goi tinh o nhieu man hinh nam ngoai cay React
// nen khong tu doc duoc theme va locale. holderRender bao cho antd biet phai boc chung
// trong dung ConfigProvider nay, nho vay hop thoai va thong bao dung mau va tieng Viet.
ConfigProvider.config({
  holderRender: (children) => (
    <ConfigProvider locale={viVN} theme={theme}>
      <AntdApp>{children}</AntdApp>
    </ConfigProvider>
  ),
});

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <ConfigProvider locale={viVN} theme={theme}>
      <AntdApp>
        <QueryClientProvider client={queryClient}>
          <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
            <App />
          </BrowserRouter>
        </QueryClientProvider>
      </AntdApp>
    </ConfigProvider>
  </React.StrictMode>,
);
