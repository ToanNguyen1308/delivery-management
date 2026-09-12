package com.viettel.delivery.security;

import com.viettel.delivery.entity.RoleGroup;
import com.viettel.delivery.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Principal cua he thong: ngoai username con giu userId va danh sach function_code.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String fullName;
    private final boolean active;
    private final Set<String> roleGroupCodes;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long userId,
                             String username,
                             String password,
                             String fullName,
                             boolean active,
                             Set<String> roleGroupCodes,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.active = active;
        this.roleGroupCodes = roleGroupCodes;
        this.authorities = authorities;
    }

    public static CustomUserDetails from(User user) {
        Set<String> roleCodes = user.getRoleGroups().stream()
                .map(RoleGroup::getRoleGroupCode)
                .collect(Collectors.toSet());
        List<SimpleGrantedAuthority> grantedAuthorities = user.getRoleGroups().stream()
                .flatMap(roleGroup -> roleGroup.getFunctions().stream())
                .map(function -> new SimpleGrantedAuthority(function.getFunctionCode()))
                .distinct()
                .toList();
        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(),
                user.getFullName(), user.isActive(), roleCodes, grantedAuthorities);
    }

    public boolean hasRoleGroup(String roleGroupCode) {
        return roleGroupCodes.contains(roleGroupCode);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
