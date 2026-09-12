package com.viettel.delivery.security;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.RoleGroupCode;
import com.viettel.delivery.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Truy xuat nguoi dung dang dang nhap, phuc vu phan quyen du lieu o tang Specification/Service.
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

    /**
     * Admin va dieu phoi vien duoc xem toan bo du lieu, cac role con lai bi gioi han theo chu so huu.
     */
    public static boolean canViewAllData() {
        return hasAuthorityOfRole(RoleGroupCode.ADMIN) || hasAuthorityOfRole(RoleGroupCode.DISPATCHER);
    }

    private static boolean hasAuthorityOfRole(RoleGroupCode roleGroupCode) {
        return switch (roleGroupCode) {
            case ADMIN -> hasAuthority(com.viettel.delivery.constant.PermissionCode.USER_DELETE);
            case DISPATCHER -> hasAuthority(com.viettel.delivery.constant.PermissionCode.DISPATCH_ASSIGN);
            default -> false;
        };
    }
}
