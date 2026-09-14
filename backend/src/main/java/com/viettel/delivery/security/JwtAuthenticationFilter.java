package com.viettel.delivery.security;

import com.viettel.delivery.constant.AppConstants;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Đọc JWT từ header Authorization rồi gắn vào SecurityContext.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token) && jwtTokenProvider.isValid(token)) {
            authenticate(token, request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            if (!AppConstants.TOKEN_TYPE_ACCESS.equals(claims.get(AppConstants.CLAIM_TOKEN_TYPE, String.class))
                    || tokenBlacklistService.isRevoked(claims.getId())) {
                return;
            }

            @SuppressWarnings("unchecked")
            List<String> authorityCodes = claims.get(AppConstants.CLAIM_AUTHORITIES, List.class);
            List<SimpleGrantedAuthority> authorities = authorityCodes == null
                    ? List.of()
                    : authorityCodes.stream().map(SimpleGrantedAuthority::new).toList();

            @SuppressWarnings("unchecked")
            List<String> roleGroupCodes = claims.get(AppConstants.CLAIM_ROLE_GROUPS, List.class);

            String fullName = claims.get(AppConstants.CLAIM_FULL_NAME, String.class);
            CustomUserDetails principal = new CustomUserDetails(
                    claims.get(AppConstants.CLAIM_USER_ID, Number.class).longValue(),
                    claims.getSubject(),
                    null,
                    StringUtils.hasText(fullName) ? fullName : claims.getSubject(),
                    true,
                    roleGroupCodes == null ? Set.of() : Set.copyOf(roleGroupCodes),
                    authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            log.debug("Khong the xac thuc token: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AppConstants.AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(AppConstants.TOKEN_PREFIX)) {
            return header.substring(AppConstants.TOKEN_PREFIX.length());
        }
        // Chỉ SockJS handshake mới đọc token từ query — tránh lộ JWT trên mọi API
        if (request.getRequestURI().contains(AppConstants.WS_ENDPOINT)) {
            String tokenParam = request.getParameter("access_token");
            return StringUtils.hasText(tokenParam) ? tokenParam : null;
        }
        return null;
    }
}
