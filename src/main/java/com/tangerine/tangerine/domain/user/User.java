package com.tangerine.tangerine.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    /**
     * [설명] Role이 4단계인 이유
     *
     * Pebble은 USER/ADMIN 2단계였어요. 게시글 작성자 본인 또는
     * 관리자만 구분하면 충분한 단순 블로그 구조였으니까요.
     *
     * Tangerine은 "위키"라는 특성 때문에 중간 등급이 필요해요.
     * 위키는 여러 사람이 같은 문서를 같이 편집하는 구조라
     * 문서 훼손(vandalism)이나 분쟁이 생길 수 있어요.
     * 그래서 나무위키, 위키백과 같은 실제 위키 서비스들도
     * "신뢰할 수 있는 편집자"라는 중간 계층을 둬요.
     *
     * USER:    일반 가입자. 글 작성/조회만 가능
     * TRUSTED: 분쟁이 적었던 기존 사용자. (지금은 등급만 있고
     *          실제 차등 권한 로직은 미구현 상태예요.
     *          예: 문서 잠금 해제 권한 등을 나중에 추가할 수 있어요)
     * MANAGER: 운영진. 타인 글/댓글 강제 삭제, 공지 작성 가능
     * ADMIN:   최종 관리자. 사용자 등급 변경까지 가능
     *
     * 이렇게 등급을 세분화하면 "권한을 한 단계씩 위임"할 수 있어서
     * ADMIN 혼자 모든 운영 부담을 지지 않아도 돼요.
     */
    public enum Role {

        USER, TRUSTED, MANAGER, ADMIN
    }

    // [설명] @Setter를 안 쓰고 이렇게 의미 있는 이름의 메서드로
    // 변경을 제한하는 이유를 RefreshToken.updateToken()에서 이미 봤어요.
    // 같은 "도메인 모델 패턴"이 여기서도 반복돼요.
    public void changeRole(User.Role role) {

        this.role = role;
    }
}
