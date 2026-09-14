import { useEffect } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { Spin } from 'antd';
import MainLayout from '@/components/layout/MainLayout';
import RequirePermission from '@/components/layout/RequirePermission';
import { useAuthStore } from '@/store/authStore';
import { PERMISSION, ROLE_GROUP } from '@/constants/permissions';
import LoginPage from '@/pages/LoginPage';
import RegisterPage from '@/pages/RegisterPage';
import PublicTrackingPage from '@/pages/PublicTrackingPage';
import DashboardPage from '@/pages/DashboardPage';
import OrderListPage from '@/pages/orders/OrderListPage';
import OrderCreatePage from '@/pages/orders/OrderCreatePage';
import OrderDetailPage from '@/pages/orders/OrderDetailPage';
import DispatchPage from '@/pages/dispatch/DispatchPage';
import MyTasksPage from '@/pages/shipper/MyTasksPage';
import MyWalletPage from '@/pages/shipper/MyWalletPage';
import CodSettlementPage from '@/pages/admin/CodSettlementPage';
import PaymentHistoryPage from '@/pages/payment/PaymentHistoryPage';
import PaymentResultPage from '@/pages/payment/PaymentResultPage';
import ShipperPage from '@/pages/admin/ShipperPage';
import UserPage from '@/pages/admin/UserPage';
import RoleGroupPage from '@/pages/admin/RoleGroupPage';
import PricingPage from '@/pages/admin/PricingPage';
import VoucherPage from '@/pages/admin/VoucherPage';
import NotificationPage from '@/pages/NotificationPage';
import ProfilePage from '@/pages/ProfilePage';
import ForbiddenPage from '@/pages/ForbiddenPage';
import NotFoundPage from '@/pages/NotFoundPage';

const App = () => {
  const { user, initialized, restoreSession } = useAuthStore();

  useEffect(() => {
    void restoreSession();
  }, [restoreSession]);

  if (!initialized) {
    return (
      <div className="h-screen flex flex-col items-center justify-center gap-4">
        <Spin size="large" />
        <span className="text-gray-500">Đang tải hệ thống...</span>
      </div>
    );
  }

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/dashboard" replace /> : <LoginPage />} />
      <Route path="/register" element={user ? <Navigate to="/dashboard" replace /> : <RegisterPage />} />
      <Route path="/tracking" element={<PublicTrackingPage />} />
      <Route path="/payment/result" element={<PaymentResultPage />} />

      <Route element={user ? <MainLayout /> : <Navigate to="/login" replace />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={
          <RequirePermission permission={PERMISSION.DASHBOARD_VIEW}>
            <DashboardPage />
          </RequirePermission>
        } />
        <Route path="/notifications" element={<NotificationPage />} />
        <Route path="/profile" element={<ProfilePage />} />

        <Route
          path="/orders"
          element={
            <RequirePermission permission={PERMISSION.ORDER_VIEW}>
              <OrderListPage />
            </RequirePermission>
          }
        />
        <Route
          path="/orders/create"
          element={
            <RequirePermission permission={PERMISSION.ORDER_CREATE}>
              <OrderCreatePage />
            </RequirePermission>
          }
        />
        <Route
          path="/orders/:id"
          element={
            <RequirePermission permission={PERMISSION.ORDER_VIEW}>
              <OrderDetailPage />
            </RequirePermission>
          }
        />
        <Route
          path="/dispatch"
          element={
            <RequirePermission permission={PERMISSION.DISPATCH_ASSIGN}>
              <DispatchPage />
            </RequirePermission>
          }
        />
        <Route
          path="/my-tasks"
          element={
            <RequirePermission permission={PERMISSION.DISPATCH_RESPOND} roleGroup={ROLE_GROUP.SHIPPER}>
              <MyTasksPage />
            </RequirePermission>
          }
        />
        <Route
          path="/my-wallet"
          element={
            <RequirePermission permission={PERMISSION.COD_SUBMIT} roleGroup={ROLE_GROUP.SHIPPER}>
              <MyWalletPage />
            </RequirePermission>
          }
        />
        <Route
          path="/cod-settlements"
          element={
            <RequirePermission permission={PERMISSION.COD_CONFIRM}>
              <CodSettlementPage />
            </RequirePermission>
          }
        />
        <Route
          path="/payments"
          element={
            <RequirePermission permission={PERMISSION.PAYMENT_VIEW}>
              <PaymentHistoryPage />
            </RequirePermission>
          }
        />
        <Route
          path="/shippers"
          element={
            <RequirePermission permission={PERMISSION.SHIPPER_VIEW}>
              <ShipperPage />
            </RequirePermission>
          }
        />
        <Route
          path="/users"
          element={
            <RequirePermission permission={PERMISSION.USER_VIEW}>
              <UserPage />
            </RequirePermission>
          }
        />
        <Route
          path="/roles"
          element={
            <RequirePermission permission={PERMISSION.ROLE_VIEW}>
              <RoleGroupPage />
            </RequirePermission>
          }
        />
        <Route
          path="/pricing"
          element={
            <RequirePermission permission={PERMISSION.PRICING_MANAGE}>
              <PricingPage />
            </RequirePermission>
          }
        />
        <Route
          path="/vouchers"
          element={
            <RequirePermission permission={PERMISSION.VOUCHER_MANAGE}>
              <VoucherPage />
            </RequirePermission>
          }
        />
        <Route path="/403" element={<ForbiddenPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
};

export default App;
