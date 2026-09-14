import { ReactElement } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

interface RequirePermissionProps {
  permission: string;
  /**
   * Thêm ràng buộc nhóm quyền cho màn hình cá nhân. Admin có đủ function_code
   * nhưng không có hồ sơ shipper nên vẫn bị chặn.
   */
  roleGroup?: string;
  children: ReactElement;
}

const RequirePermission = ({ permission, roleGroup, children }: RequirePermissionProps) => {
  const hasPermission = useAuthStore((state) => state.permissions.includes(permission));
  const hasRoleGroup = useAuthStore((state) => !roleGroup || state.roleGroups.includes(roleGroup));
  return hasPermission && hasRoleGroup ? children : <Navigate to="/403" replace />;
};

export default RequirePermission;
