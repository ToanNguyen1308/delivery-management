package com.viettel.delivery.security;

import com.viettel.delivery.config.properties.JwtProperties;
import com.viettel.delivery.constant.AppConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Sinh va xac thuc JWT (access token + refresh token).
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .claim(AppConstants.CLAIM_USER_ID, userDetails.getUserId())
                .claim(AppConstants.CLAIM_AUTHORITIES, authorities)
                .claim(AppConstants.CLAIM_TOKEN_TYPE, AppConstants.TOKEN_TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.accessExpirationMs())))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .claim(AppConstants.CLAIM_USER_ID, userDetails.getUserId())
                .claim(AppConstants.CLAIM_TOKEN_TYPE, AppConstants.TOKEN_TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.refreshExpirationMs())))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("Token da het han: {}", ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token khong hop le: {}", ex.getMessage());
        }
        return false;
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        return parseClaims(token).get(AppConstants.CLAIM_USER_ID, Number.class).longValue();
    }

    public String getTokenId(String token) {
        return parseClaims(token).getId();
    }

    public boolean isRefreshToken(String token) {
        return AppConstants.TOKEN_TYPE_REFRESH
                .equals(parseClaims(token).get(AppConstants.CLAIM_TOKEN_TYPE, String.class));
    }

    public long getRemainingMillis(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return Math.max(0, expiration.getTime() - System.currentTimeMillis());
    }

    public long getAccessExpirationMs() {
        return jwtProperties.accessExpirationMs();
    }

    public long getRefreshExpirationMs() {
        return jwtProperties.refreshExpirationMs();
    }
}
