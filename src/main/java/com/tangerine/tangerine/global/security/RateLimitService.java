package com.tangerine.tangerine.global.security;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * [추가] RateLimitService
 *
 * Redis 카운터를 이용해서 로그인 시도 횟수를 제한해요.
 *
 * 동작 방식:
 * 1. 로그인 실패 시 Redis에 "login-fail:{email}" 카운터를 1 증가
 * 2. 카운터가 5를 넘으면 5분간 차단
 * 3. TTL이 지나면 Redis가 카운터를 자동 삭제 → 차단 해제
 *
 * "login-fail:" 접두사를 쓰는 이유:
 * Refresh Token을 저장하는 "refresh:" 키와 구분하기 위해서예요.
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    // 최대 허용 실패 횟수
    private static final int MAX_ATTEMPTS = 5;

    // 차단 시간 (분)
    private static final int BLOCK_DURATION_MINUTES = 5;

    private String buildKey(String email) {

        return "login-fail:" + email;
    }

    /**
     * 현재 차단 상태인지 확인해요.
     * 카운터가 MAX_ATTEMPTS를 초과했으면 true를 반환해요.
     */
    public boolean isBlocked(String email) {

        String value = redisTemplate.opsForValue().get(buildKey(email));
        if (value == null) return false;
        return Integer.parseInt(value) >= MAX_ATTEMPTS;
    }

    /**
     * 로그인 실패 시 호출해요.
     *
     * increment(): Redis 카운터를 1 증가시켜요.
     * 처음 호출이면 0에서 시작해서 1이 돼요.
     *
     * 첫 번째 실패일 때만 TTL을 설정해요.
     * (이미 카운터가 있으면 TTL을 다시 설정하면 안 돼요.
     *  그러면 계속 실패해도 시간이 리셋돼서 차단이 안 될 수 있어요)
     */
    public void recordFailure(String email) {

        String key = buildKey(email);
        Long count = redisTemplate.opsForValue().increment(key);

        // 첫 번째 실패일 때만 TTL 설정
        if (count != null && count == 1) {

            redisTemplate.expire(key, BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * 로그인 성공 시 호출해요.
     * 누적된 실패 카운터를 초기화해요.
     */
    public void clearFailures(String email) {

        redisTemplate.delete(buildKey(email));
    }
}
