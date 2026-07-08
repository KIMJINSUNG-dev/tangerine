package com.tangerine.tangerine.domain.user;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * [추가] RefreshTokenService
 *
 * Redis에 Refresh Token을 저장/조회/삭제하는 Service예요.
 *
 * 기존 방식 (MySQL):
 *   refreshTokenRepository.save(refreshToken)
 *   → INSERT INTO refresh_tokens (email, token, expires_at) VALUES (...)
 *   → 만료 여부를 직접 체크하고 삭제해야 했어요
 *
 * 새 방식 (Redis):
 *   redisTemplate.opsForValue().set(key, value, 7, TimeUnit.DAYS)
 *   → Redis에 key-value 쌍으로 저장하고 7일 후 자동 삭제
 *   → 만료 관리를 Redis가 알아서 해줘요
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * key 앞에 "refresh:" 접두사를 붙이는 이유:
     * Redis는 모든 데이터가 하나의 저장소에 flat하게 들어가요.
     * 나중에 Redis에 다른 용도의 데이터(예: 캐시, 세션)도 추가하면
     * key가 뒤섞일 수 있어요. 접두사로 용도를 구분해요.
     * 예: "refresh:user@test.com", "cache:documents:1" 등
     */
    private String buildKey(String email) {

        return "refresh:" + email;
    }

    /**
     * Refresh Token 저장
     *
     * opsForValue(): Redis의 단순 key-value 연산을 제공해요.
     * set(key, value, timeout, timeUnit): TTL(만료 시간)을 함께 설정해요.
     * 7일이 지나면 Redis가 자동으로 이 key를 삭제해요.
     */
    public void save(String email, String refreshToken) {

        redisTemplate.opsForValue().set(
                buildKey(email),
                refreshToken,
                7,
                TimeUnit.DAYS
        );
    }

    /**
     * Refresh Token 조회
     * 없거나 만료됐으면 null을 반환해요.
     */
    public String find(String email) {

        return redisTemplate.opsForValue().get(buildKey(email));
    }

    /**
     * Refresh Token 삭제 (로그아웃 시)
     * delete()는 해당 key를 즉시 삭제해요.
     */
    public void delete(String email) {

        redisTemplate.delete(buildKey(email));
    }
}
