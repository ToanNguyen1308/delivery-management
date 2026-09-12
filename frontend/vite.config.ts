import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';

export default defineConfig({
  plugins: [react()],
  // sockjs-client la thu vien CommonJS viet cho Node nen co tham chieu bien `global`,
  // von khong ton tai tren trinh duyet. Khong khai bao thi app chet ngay khi nap module
  // va man hinh trang tron. Anh xa sang globalThis la cach xu ly chuan cho Vite.
  define: {
    global: 'globalThis',
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    // Khi chay dev, goi thang sang backend de khong phai bat CORS rieng.
    // ws: true de handshake WebSocket tai /api/v1/ws di qua duoc proxy.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        // Tach thu vien lon thanh chunk rieng de trinh duyet cache lau hon
        manualChunks: {
          react: ['react', 'react-dom', 'react-router-dom'],
          antd: ['antd', '@ant-design/icons'],
          charts: ['recharts'],
          map: ['leaflet', 'react-leaflet'],
          realtime: ['@stomp/stompjs', 'sockjs-client'],
        },
      },
    },
  },
});
