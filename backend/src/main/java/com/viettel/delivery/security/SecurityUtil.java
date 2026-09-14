package com.viettel.delivery.security;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.constant.enums.RoleGroupCode;
import com.viettel.delivery.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Truy xuất user đăng nhập, phục vụ phân quyền dữ liệu ở tầng Specification/Service.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Optional<CustomUserDetails> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static CustomUserDetails requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED));
    }

    public static Long getCurrentUserId() {
        return requireCurrentUser().getUserId();
    }

    public static Optional<Long> getCurrentUserIdOptional() {
        return getCurrentUser().map(CustomUserDetails::getUserId);
    }

    public static String getCurrentUsername() {
        return getCurrentUser().map(CustomUserDetails::getUsername).orElse(null);
    }

    public static boolean hasAuthority(String functionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(functionCode::equals);
    }

    /** Admin và điều phối viên xem toàn bộ dữ liệu; các role khác giới hạn theo chủ sở hữu. */
    public static boolean canViewAllData() {
        Optional<CustomUserDetails> current = getCurrentUser();
        if (current.isEmpty()) {
            return false;
        }
        CustomUserDetails user = current.get();
        if (!user.getRoleGroupCodes().isEmpty()) {
            return user.hasRoleGroup(RoleGroupCode.ADMIN.name())
                    || user.hasRoleGroup(RoleGroupCode.DISPATCHER.name());
        }
        return hasAuthority(PermissionCode.USER_DELETE) || hasAuthority(PermissionCode.DISPATCH_ASSIGN);
    }
}
