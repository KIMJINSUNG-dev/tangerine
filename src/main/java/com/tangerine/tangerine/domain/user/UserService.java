package com.tangerine.tangerine.domain.user;

import com.tangerine.tangerine.domain.user.dto.LoginRequest;
import com.tangerine.tangerine.domain.user.dto.LoginResponse;
import com.tangerine.tangerine.domain.user.dto.SignupRequest;
import com.tangerine.tangerine.global.security.JwtProvider;
import com.tangerine.tangerine.global.security.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitService rateLimitService;

    @Transactional
    public void signup(SignupRequest request) {

        if(userRepository.existsByEmail(request.getEmail())) {

            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if(userRepository.existsByNickname(request.getNickname())) {

            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        /**
         * [추가] Rate Limiting 체크
         * 로그인 시도 전에 먼저 차단 여부를 확인해요.
         * 이미 5번 실패한 상태면 비밀번호가 맞아도 거부해요.
         */
        if (rateLimitService.isBlocked(request.getEmail())) {

            throw new IllegalArgumentException("로그인 시도가 너무 많습니다. 5분 후에 다시 시도해주세요.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            // [추가] 비밀번호 불일치 시 실패 카운터 증가
            rateLimitService.recordFailure(request.getEmail());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // [추가] 로그인 성공 시 실패 카운터 초기화
        rateLimitService.clearFailures(request.getEmail());

        String accessToken = jwtProvider.generateAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        // Redis 저장
        refreshTokenService.save(user.getEmail(), refreshToken);

        return new LoginResponse(accessToken, refreshToken, user.getNickname(), user.getRole().name());
    }

    /**
     * [설명] Pebble에는 이 메서드 자체가 없었어요.
     * Access Token이 만료됐을 때 호출되는 재발급 로직이에요.
     */
    @Transactional
    public String reissueAccessToken(String refreshToken) {

        // 1단계: 토큰 자체의 서명/만료 검증 (JWT 구조적으로 유효한가)
        if (!jwtProvider.validateToken(refreshToken)) {

            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);

        /**
         * [설명] 2단계: DB 대조 검증 (이게 핵심 보안 장치예요)
         * JWT 자체는 유효하더라도, DB에 저장된 최신 Refresh Token과
         * 일치하지 않으면 거부해요.
         * 예: 사용자가 로그아웃해서 DB의 토큰이 삭제됐는데
         *     누군가 예전에 탈취해둔 Refresh Token으로 재발급을 시도하면
         *     → DB에 없으니 (또는 값이 다르니) 거부됨
         */
        String storedToken = refreshTokenService.find(email);

        if (storedToken == null) {

            throw new IllegalArgumentException("Refresh Token이 존재하지 않습니다.");
        }

        if (!storedToken.equals(refreshToken)) {

            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 3단계: 검증을 통과했으니 새 Access Token만 발급 (Refresh Token은 그대로 유지)
        return jwtProvider.generateAccessToken(email, user.getRole().name());
    }

    @Transactional
    public void logout(String email) {

        refreshTokenService.delete(email);
    }
}
