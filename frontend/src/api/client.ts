import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import { message } from 'antd';
import type { ApiResponse } from '@/types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

export const STORAGE_KEYS = {
  accessToken: 'delivery.accessToken',
  refreshToken: 'delivery.refreshToken',
};

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    'Accept-Language': 'vi',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEYS.accessToken);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (config.data instanceof FormData) {
    // Axios mặc định JSON; để trình duyệt tự gắn boundary cho multipart
    config.headers.delete('Content-Type');
  }
  return config;
});

/** Hàng đợi request 401 trong lúc refresh token, tránh gọi refresh nhiều lần. */
let isRefreshing = false;
let pendingQueue: { resolve: (token: string) => void; reject: (error: unknown) => void }[] = [];

const flushQueue = (error: unknown, token?: string) => {
  pendingQueue.forEach((item) => (token ? item.resolve(token) : item.reject(error)));
  pendingQueue = [];
};

/** Gom toast lỗi trùng trong 3 giây (một màn thường gọi nhiều API song song). */
const ERROR_DEDUPE_MS = 3000;
const recentErrors = new Map<string, number>();

const showError = (text: string) => {
  const now = Date.now();
  recentErrors.forEach((shownAt, key) => {
    if (now - shownAt > ERROR_DEDUPE_MS) {
      recentErrors.delete(key);
    }
  });
  if (recentErrors.has(text)) {
    return;
  }
  recentErrors.set(text, now);
  message.error(text);
};

const forceLogout = () => {
  localStorage.removeItem(STORAGE_KEYS.accessToken);
  localStorage.removeItem(STORAGE_KEYS.refreshToken);
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login';
  }
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retried?: boolean };
    const status = error.response?.status;
    const requestUrl = originalRequest?.url ?? '';

    if (status === 401 && requestUrl.includes('/payments/vnpay/return')) {
      return Promise.reject(error);
    }

    if (status === 401 && originalRequest && !originalRequest._retried) {
      const refreshToken = localStorage.getItem(STORAGE_KEYS.refreshToken);
      if (!refreshToken) {
        forceLogout();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({
            resolve: (token) => {
              originalRequest.headers = { ...originalRequest.headers, Authorization: `Bearer ${token}` };
              resolve(apiClient(originalRequest));
            },
            reject,
          });
        });
      }

      originalRequest._retried = true;
      isRefreshing = true;
      try {
        const { data } = await axios.post<ApiResponse<{ accessToken: string; refreshToken: string }>>(
          `${API_BASE_URL}/auth/refresh`,
          { refreshToken },
          { headers: { 'Content-Type': 'application/json' } },
        );
        const newToken = data.data.accessToken;
        localStorage.setItem(STORAGE_KEYS.accessToken, newToken);
        localStorage.setItem(STORAGE_KEYS.refreshToken, data.data.refreshToken);
        flushQueue(null, newToken);
        originalRequest.headers = { ...originalRequest.headers, Authorization: `Bearer ${newToken}` };
        return apiClient(originalRequest);
      } catch (refreshError) {
        flushQueue(refreshError);
        forceLogout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    const serverMessage = error.response?.data?.message;
    if (serverMessage) {
      showError(serverMessage);
    } else if (!error.response) {
      showError('Không kết nối được tới máy chủ');
    }
    return Promise.reject(error);
  },
);

/** Lấy `data` trong ApiResponse. */
export const unwrap = <T>(response: AxiosResponse<ApiResponse<T>>): T => response.data.data;

export default apiClient;
