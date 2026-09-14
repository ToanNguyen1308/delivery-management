import { useMemo, useState } from 'react';
import { Avatar, Dropdown, Layout, Menu, Tag, Typography } from 'antd';
import type { MenuProps } from 'antd';
import {
  AppstoreOutlined,
  BarChartOutlined,
  CarOutlined,
  CreditCardOutlined,
  DollarOutlined,
  FileTextOutlined,
  GiftOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  ShopOutlined,
  TeamOutlined,
  UserOutlined,
  WalletOutlined,
} from '@ant-design/icons';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { PERMISSION, ROLE_GROUP } from '@/constants/permissions';
import NotificationBell from './NotificationBell';

const { Header, Sider, Content } = Layout;

interface MenuDefinition {
  key: string;
  icon: React.ReactNode;
  label: string;
  permissions: string[];
  /** Màn hình cá nhân (ví dụ nhiệm vụ shipper): admin có đủ quyền nhưng không phải shipper thì ẩn. */
  roleGroups?: string[];
}

const MENU_DEFINITIONS: MenuDefinition[] = [
  { key: '/dashboard', icon: <BarChartOutlined />, label: 'Tổng quan', permissions: [PERMISSION.DASHBOARD_VIEW] },
  { key: '/orders', icon: <FileTextOutlined />, label: 'Đơn hàng', permissions: [PERMISSION.ORDER_VIEW] },
  { key: '/orders/create', icon: <AppstoreOutlined />, label: 'Tạo đơn hàng', permissions: [PERMISSION.ORDER_CREATE] },
  { key: '/dispatch', icon: <CarOutlined />, label: 'Điều phối', permissions: [PERMISSION.DISPATCH_ASSIGN] },
  {
    key: '/my-tasks',
    icon: <CarOutlined />,
    label: 'Nhiệm vụ của tôi',
    permissions: [PERMISSION.DISPATCH_RESPOND],
    roleGroups: [ROLE_GROUP.SHIPPER],
  },
  {
    key: '/my-wallet',
    icon: <WalletOutlined />,
    label: 'Ví COD',
    permissions: [PERMISSION.COD_SUBMIT],
    roleGroups: [ROLE_GROUP.SHIPPER],
  },
  { key: '/cod-settlements', icon: <DollarOutlined />, label: 'Đối soát COD', permissions: [PERMISSION.COD_CONFIRM] },
  { key: '/payments', icon: <CreditCardOutlined />, label: 'Thanh toán', permissions: [PERMISSION.PAYMENT_VIEW] },
  { key: '/shippers', icon: <ShopOutlined />, label: 'Quản lý shipper', permissions: [PERMISSION.SHIPPER_VIEW] },
  { key: '/users', icon: <TeamOutlined />, label: 'Người dùng', permissions: [PERMISSION.USER_VIEW] },
  { key: '/roles', icon: <SafetyCertificateOutlined />, label: 'Nhóm quyền', permissions: [PERMISSION.ROLE_VIEW] },
  { key: '/pricing', icon: <SettingOutlined />, label: 'Bảng phí', permissions: [PERMISSION.PRICING_MANAGE] },
  { key: '/vouchers', icon: <GiftOutlined />, label: 'Voucher', permissions: [PERMISSION.VOUCHER_MANAGE] },
];

const MainLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, roleGroups, hasAnyPermission, logout } = useAuthStore();

  const menuItems: MenuProps['items'] = useMemo(
    () =>
      MENU_DEFINITIONS.filter(
        (item) =>
          hasAnyPermission(item.permissions) &&
          (!item.roleGroups || item.roleGroups.some((code) => roleGroups.includes(code))),
      ).map((item) => ({
        key: item.key,
        icon: item.icon,
        label: <Link to={item.key}>{item.label}</Link>,
      })),
    [hasAnyPermission, roleGroups],
  );

  // Ưu tiên mục khớp đường dẫn dài nhất (tránh /orders nuốt /orders/create)
  const selectedKey = useMemo(() => {
    const matched = MENU_DEFINITIONS.map((item) => item.key)
      .filter((key) => location.pathname === key || location.pathname.startsWith(`${key}/`))
      .sort((a, b) => b.length - a.length);
    return matched[0] ?? location.pathname;
  }, [location.pathname]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider className="app-sider" collapsible collapsed={collapsed} trigger={null} theme="light" width={230}>
        <div className="h-16 flex items-center justify-center px-3">
          <CarOutlined style={{ fontSize: 22, color: '#d32f2f' }} />
          {!collapsed && (
            <Typography.Text strong className="ml-2" style={{ fontSize: 15 }}>
              Quản lý giao hàng
            </Typography.Text>
          )}
        </div>
        <Menu mode="inline" selectedKeys={[selectedKey]} items={menuItems} />
      </Sider>

      <Layout>
        <Header className="bg-white flex items-center justify-between px-4" style={{ paddingInline: 16 }}>
          <div
            className="cursor-pointer text-lg"
            onClick={() => setCollapsed((prev) => !prev)}
            role="button"
            tabIndex={0}
            onKeyDown={() => setCollapsed((prev) => !prev)}
          >
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </div>

          <div className="flex items-center gap-3">
            <NotificationBell />
            <Dropdown
              menu={{
                items: [
                  { key: 'profile', icon: <UserOutlined />, label: 'Thông tin tài khoản' },
                  { type: 'divider' },
                  { key: 'logout', icon: <LogoutOutlined />, label: 'Đăng xuất', danger: true },
                ],
                onClick: ({ key }) => {
                  if (key === 'logout') {
                    void handleLogout();
                  } else {
                    navigate('/profile');
                  }
                },
              }}
            >
              <div className="flex items-center gap-2 cursor-pointer">
                <Avatar style={{ backgroundColor: '#d32f2f' }} icon={<UserOutlined />} />
                <div className="hidden sm:block leading-tight">
                  <div style={{ fontWeight: 600 }}>{user?.fullName}</div>
                  <Tag color="red" style={{ marginTop: 2 }}>
                    {roleGroups.join(', ')}
                  </Tag>
                </div>
              </div>
            </Dropdown>
          </div>
        </Header>

        <Content className="page-container">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
