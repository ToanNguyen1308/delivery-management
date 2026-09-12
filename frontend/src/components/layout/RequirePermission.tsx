import { ReactElement } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

interface RequirePermissionProps {
  permission: string;
  /**
   * Rang buoc them ve nhom quyen cho cac man hinh ca nhan nhu "Nhiem vu cua toi".
   * Admin co du function_code nhung khong co ho so shipper nen van bi chan.
   */
  roleGroup?: string;
  children: ReactElement;
}

/** Chan truy cap route khi tai khoan khong co function_code tuong ung. */
const RequirePermission = ({ permission, roleGroup, children }: RequirePermissionProps) => {
  const hasPermission = useAuthStore((state) => state.permissions.includes(permission));
  const hasRoleGroup = useAuthStore((state) => !roleGroup || state.roleGroups.includes(roleGroup));
  return hasPermission && hasRoleGroup ? children : <Navigate to="/403" replace />;
};

export default RequirePermission;
