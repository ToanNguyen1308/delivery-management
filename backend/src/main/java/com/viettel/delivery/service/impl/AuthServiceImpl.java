package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.RoleGroupCode;
import com.viettel.delivery.constant.enums.UserStatus;
import com.viettel.delivery.dto.request.ChangePasswordRequest;
import com.viettel.delivery.dto.request.LoginRequest;
import com.viettel.delivery.dto.request.RefreshTokenRequest;
import com.viettel.delivery.dto.request.RegisterRequest;
import com.viettel.delivery.dto.response.LoginResponse;
import com.viettel.delivery.dto.response.UserResponse;
import com.viettel.delivery.entity.RefreshToken;
import com.viettel.delivery.entity.RoleGroup;
import com.viettel.delivery.entity.User;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.UserMapper;
import com.viettel.delivery.repository.RefreshTokenRepository;
import com.viettel.delivery.repository.RoleGroupRepository;
import com.viettel.delivery.repository.UserRepository;
import com.viettel.delivery.security.CustomUserDetails;
import com.viettel.delivery.security.JwtTokenProvider;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.security.TokenBlacklistService;
import com.viettel.delivery.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleGroupRepository roleGroupRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameWithAuthorities(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_BAD_CREDENTIALS, HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Dang nhap that bai cho tai khoan {}", request.getUsername());
            throw new BusinessException(ErrorCode.USER_BAD_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        }
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE, HttpStatus.FORBIDDEN);
        }

        user.setLastLoginAt(LocalDateTime.now());
        CustomUserDetails userDetails = CustomUserDetails.from(user);
        return buildLoginResponse(user, userDetails);
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateUniqueFields(request);

        RoleGroup customerRole = roleGroupRepository
                .findByRoleGroupCodeAndIsDeletedFalse(RoleGroupCode.CUSTOMER.name())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_GROUP_NOT_FOUND));

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoleGroups(Set.of(customerRole));

        User saved = userRepository.save(user);
        log.info("Tai khoan khach hang moi da duoc tao: {}", saved.getUsername());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.isValid(token) || !jwtTokenProvider.isRefreshToken(token)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
        }

        RefreshToken stored = refreshTokenRepository.findByTokenWithUser(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID, HttpStatus.UNAUTHORIZED));
        if (!stored.isUsable()) {
            throw new BusinessException(ErrorCode.TOKEN_REVOKED, HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByIdWithAuthorities(stored.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE, HttpStatus.FORBIDDEN);
        }

        // Xoay vong refresh token: token cu bi thu hoi ngay khi cap token moi
        stored.setRevoked(Boolean.TRUE);
        return buildLoginResponse(user, CustomUserDetails.from(user));
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        if (!StringUtils.hasText(accessToken) || !jwtTokenProvider.isValid(accessToken)) {
            return;
        }
        Claims claims = jwtTokenProvider.parseClaims(accessToken);
        tokenBlacklistService.revoke(claims.getId(), jwtTokenProvider.getRemainingMillis(accessToken));
        SecurityUtil.getCurrentUserIdOptional().ifPresent(refreshTokenRepository::revokeAllByUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentProfile() {
        User user = userRepository.findByIdWithAuthorities(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_OLD_PASSWORD_INVALID);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenRepository.revokeAllByUserId(user.getId());
        log.info("Nguoi dung {} da doi mat khau", user.getUsername());
    }

    private LoginResponse buildLoginResponse(User user, CustomUserDetails userDetails) {
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(userDetails);

        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(toLocalDateTime(jwtTokenProvider.getRefreshExpirationMs()))
                .revoked(Boolean.FALSE)
                .build());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .expiresIn(jwtTokenProvider.getAccessExpirationMs())
                .user(userMapper.toResponse(user))
                .permissions(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .sorted()
                        .toList())
                .roleGroups(userDetails.getRoleGroupCodes().stream().sorted().toList())
                .build();
    }

    private LocalDateTime toLocalDateTime(long millisFromNow) {
        return LocalDateTime.ofInstant(Instant.now().plusMillis(millisFromNow), ZoneId.systemDefault());
    }

    private void validateUniqueFields(RegisterRequest request) {
        if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
            throw new BusinessException(ErrorCode.USER_USERNAME_EXISTED);
        }
        if (StringUtils.hasText(request.getEmail())
                && userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_EXISTED);
        }
        if (StringUtils.hasText(request.getPhoneNumber())
                && userRepository.existsByPhoneNumberAndIsDeletedFalse(request.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.USER_PHONE_EXISTED);
        }
    }
}
