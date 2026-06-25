package com.tangerine.tangerine.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * [설명] 이 Entity 자체가 Pebble과 가장 큰 구조적 차이예요.
 *
 * Pebble의 로그인 흐름을 다시 보면:
 *   로그인 → Access Token + Refresh Token 발급
 *          → Refresh Token은 그냥 httpOnly 쿠키로만 흘려보내고
 *            서버는 그걸 기억하지 않았어요.
 *   (사실 Pebble은 /api/users/reissue API 자체를 안 만들어서
 *    Access Token이 만료되면 그냥 재로그인해야 하는 상태였어요.
 *    axios.ts 인터셉터는 reissue를 시도하지만 백엔드에
 *    그 엔드포인트가 없어서 실패하고 로그인 페이지로 튕겼을 거예요.)
 *
 * Tangerine은 Refresh Token을 DB에 저장해요. 이렇게 하면:
 *   1. Access Token이 만료돼도 Refresh Token으로 재발급 가능
 *      (사용자가 30분마다 재로그인할 필요 없음)
 *   2. 관리자가 특정 사용자를 강제 로그아웃시키고 싶을 때
 *      DB에서 이 Refresh Token 행을 삭제하면 끝 →
 *      그 사용자는 Access Token이 만료되는 즉시
 *      재발급이 거부되고 강제로 로그인 페이지로 밀려나요.
 *
 * 즉 "서버가 토큰을 하나도 기억하지 않는 JWT 방식"의 단점
 * (즉시 무효화가 어려움)을 이 Entity 하나로 보완하는 거예요.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [설명] 한 사용자당 Refresh Token이 하나만 존재해야 하므로 unique
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * [설명] 재로그인 시 기존 행을 삭제하고 새로 만드는 대신
     * 이 메서드로 같은 행의 값만 갱신해요.
     * (UserService.login()에서 ifPresentOrElse로 호출했던 부분이에요)
     */
    public void updateToken(String token, LocalDateTime expiresAt) {

        this.token = token;
        this.expiresAt = expiresAt;
    }
}
