package com.tangerine.tangerine.domain.user;

import com.tangerine.tangerine.domain.user.dto.LoginRequest;
import com.tangerine.tangerine.domain.user.dto.LoginResponse;
import com.tangerine.tangerine.domain.user.dto.SignupRequest;
import com.tangerine.tangerine.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * @ExtendWith(MockitoExtension.class)
 * JUnit 5에서 Mockito를 쓰기 위한 설정이에요.
 * Spring 전체 컨텍스트를 띄우지 않아서 테스트가 빠르게 실행돼요.
 * 실제 DB, 실제 Spring Bean 없이 Service 로직만 검증해요.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    /**
     * @Mock: 가짜(Mock) 객체를 만들어요.
     * 실제 Repository 대신 가짜 객체를 주입해서 DB 없이 테스트해요.
     * 메서드를 호출하면 기본적으로 null 또는 빈 값을 반환해요.
     * given()으로 원하는 반환값을 미리 설정할 수 있어요.
     */
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    /**
     * @InjectMocks: 테스트할 실제 클래스를 생성하고
     * @Mock으로 만든 가짜 객체들을 자동으로 주입해요.
     */
    @InjectMocks
    private UserService userService;

    // 테스트에서 공통으로 쓸 객체들
    private User mockUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    /**
     * @BeforeEach: 각 테스트 메서드 실행 전에 먼저 실행돼요.
     * 공통으로 필요한 객체를 미리 만들어서 중복 코드를 줄여요.
     */
    @BeforeEach
    void setUp() {

        mockUser = User.builder()
                .email("test@test.com")
                .password("암호화된비밀번호")
                .nickname("테스트유저")
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        signupRequest = new SignupRequest("test@test.com", "1234", "테스트유저");
        loginRequest = new LoginRequest("test@test.com", "1234");
    }

    // ===== signup() 테스트 =====
    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {

        // given: 이메일과 닉네임이 중복되지 않는 상황을 설정해요
        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(userRepository.existsByNickname("테스트유저")).willReturn(false);
        given(passwordEncoder.encode("1234")).willReturn("암호화된비밀번호");

        // when: 실제로 테스트할 동작을 실행해요
        userService.signup(signupRequest);

        /**
         * then: 결과를 검증해요.
         * verify(): Mock 객체의 메서드가 실제로 호출됐는지 확인해요.
         * times(1): 정확히 1번 호출됐는지 검증해요.
         * any(User.class): User 타입의 어떤 객체든 상관없다는 의미예요.
         */
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail_duplicate_email() {

        // given: 이메일이 이미 존재하는 상황
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        /**
         * assertThatThrownBy(): 예외가 발생하는지 검증해요.
         * () -> userService.signup(signupRequest): 실행할 코드
         * .isInstanceOf(): 어떤 종류의 예외인지
         * .hasMessage(): 예외 메시지가 일치하는지
         */
        assertThatThrownBy(() -> userService.signup(signupRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void signup_fail_duplicate_nickname() {

        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(userRepository.existsByNickname("테스트유저")).willReturn(true);

        assertThatThrownBy(() -> userService.signup(signupRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }

    // ===== login() 테스트 =====
    @Test
    @DisplayName("로그인 성공")
    void login_success() {

        // given: 로그인에 필요한 상황을 전부 설정해요
        given(userRepository.findByEmail("test@test.com"))
                .willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("1234", "암호화된비밀번호"))
                .willReturn(true);
        given(jwtProvider.generateAccessToken(anyString(), anyString()))
                .willReturn("fake-access-token");
        given(jwtProvider.generateRefreshToken(anyString()))
                .willReturn("fake-refresh-token");

        // refreshTokenService.save()는 void 반환이라 별도 설정 없이
        // Mockito가 자동으로 아무것도 안 하는 것처럼 처리해줘요.

        // when
        LoginResponse response = userService.login(loginRequest);

        /**
         * assertThat(): 값을 검증해요.
         * .isEqualTo(): 값이 정확히 같은지 확인해요.
         */
        assertThat(response.getAccessToken()).isEqualTo("fake-access-token");
        assertThat(response.getNickname()).isEqualTo("테스트유저");
        assertThat(response.getRole()).isEqualTo("USER");

        // RefreshToken이 저장됐는지도 검증해요
        verify(refreshTokenService, times(1)).save(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 없음")
    void login_fail_email_not_found() {

        // 이메일로 조회했을 때 빈 Optional 반환 (사용자 없음)
        given(userRepository.findByEmail("test@test.com"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrong_password() {

        given(userRepository.findByEmail("test@test.com"))
                .willReturn(Optional.of(mockUser));
        // 비밀번호 검증이 실패하는 상황
        given(passwordEncoder.matches("1234", "암호화된비밀번호"))
                .willReturn(false);

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    // ===== reissueAccessToken() 테스트 =====

    @Test
    @DisplayName("Access Token 재발급 성공")
    void reissueAccessToken_success() {

        given(jwtProvider.validateToken("valid-refresh-token")).willReturn(true);
        given(jwtProvider.getEmailFromToken("valid-refresh-token")).willReturn("test@test.com");
        given(refreshTokenService.find("test@test.com"))
                .willReturn("valid-refresh-token");
        given(userRepository.findByEmail("test@test.com"))
                .willReturn(Optional.of(mockUser));
        given(jwtProvider.generateAccessToken(anyString(), anyString()))
                .willReturn("new-access-token");

        // when
        String newToken = userService.reissueAccessToken("valid-refresh-token");

        // then
        assertThat(newToken).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("Access Token 재발급 실패 - 유효하지 않은 토큰")
    void reissueAccessToken_fail_invalid_token() {

        given(jwtProvider.validateToken("invalid-token")).willReturn(false);

        assertThatThrownBy(() -> userService.reissueAccessToken("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Refresh Token입니다.");
    }

    @Test
    @DisplayName("Access Token 재발급 실패 - DB의 토큰과 불일치")
    void reissueAccessToken_fail_token_mismatch() {

        given(jwtProvider.validateToken("some-token")).willReturn(true);
        given(jwtProvider.getEmailFromToken("some-token")).willReturn("test@test.com");
        given(refreshTokenService.find("test@test.com"))
                .willReturn("different-token-in-redis");

        // "some-token"과 DB의 "different-token-in-db"가 다르니까 예외 발생
        assertThatThrownBy(() -> userService.reissueAccessToken("some-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token이 일치하지 않습니다.");
    }
}
