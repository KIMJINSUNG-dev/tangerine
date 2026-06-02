package com.tangerine.tangerine.domain.user;

import com.tangerine.tangerine.domain.user.dto.LoginRequest;
import com.tangerine.tangerine.domain.user.dto.LoginResponse;
import com.tangerine.tangerine.domain.user.dto.SignupRequest;
import com.tangerine.tangerine.global.security.JwtProvider;
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
    private final RefreshTokenRepository refreshTokenRepository;

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

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        LocalDateTime refreshExpiration = LocalDateTime.now()
                .plusSeconds(604800);

        refreshTokenRepository.findByEmail(user.getEmail())
                .ifPresentOrElse(
                        existing -> existing.updateToken(refreshToken, refreshExpiration),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .email(user.getEmail())
                                        .token(refreshToken)
                                        .expiresAt(refreshExpiration)
                                        .build()
                        )
                );

        return new LoginResponse(accessToken, refreshToken, user.getNickname(), user.getRole().name());
    }

    @Transactional
    public void logout(String email) {

        refreshTokenRepository.deleteByEmail(email);
    }

    @Transactional
    public String reissueAccessToken(String refreshToken) {

        if (!jwtProvider.validateToken(refreshToken)) {

            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token이 존재하지 않습니다."));

        if (!storedToken.getToken().equals(refreshToken)) {

            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return jwtProvider.generateAccessToken(email, user.getRole().name());
    }
}
