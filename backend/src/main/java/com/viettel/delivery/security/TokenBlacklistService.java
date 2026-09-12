package com.viettel.delivery.security;

import com.viettel.delivery.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Thu hoi access token khi dang xuat bang cach luu jti vao Redis den khi token het han.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void revoke(String tokenId, long ttlMillis) {
        if (tokenId == null || ttlMillis <= 0) {
            return;
        }
        try {
            redisTemplate.opsForValue()
                    .set(AppConstants.REDIS_BLACKLIST_PREFIX + tokenId, "revoked", Duration.ofMillis(ttlMillis));
        } catch (Exception ex) {
            log.warn("Khong ghi duoc blacklist token vao Redis: {}", ex.getMessage());
        }
    }

    public boolean isRevoked(String tokenId) {
        if (tokenId == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(AppConstants.REDIS_BLACKLIST_PREFIX + tokenId));
        } catch (Exception ex) {
            log.warn("Khong kiem tra duoc blacklist token: {}", ex.getMessage());
            return false;
        }
    }
}
