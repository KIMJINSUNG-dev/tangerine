package com.tangerine.tangerine.domain.board;

import com.tangerine.tangerine.domain.board.dto.PostCreateRequest;
import com.tangerine.tangerine.domain.board.dto.PostResponse;
import com.tangerine.tangerine.domain.board.dto.PostUpdateRequest;
import com.tangerine.tangerine.domain.user.User;
import com.tangerine.tangerine.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    private User mockUser;
    private User mockAdmin;
    private Post mockPost;

    @BeforeEach
    void setUp() {

        mockUser = User.builder()
                .email("user@test.com")
                .password("암호화된비밀번호")
                .nickname("일반유저")
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        mockAdmin = User.builder()
                .email("admin@test.com")
                .password("암호화된비밀번호")
                .nickname("관리자")
                .role(User.Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        mockPost = Post.builder()
                .boardType(Post.BoardType.FREE)
                .title("테스트 제목")
                .content("테스트 내용")
                .author(mockUser)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .viewCount(0)
                .build();
    }

    // ===== createPost() 테스트 =====

    @Test
    @DisplayName("게시글 작성 성공")
    void createPost_success() {

        // given
        PostCreateRequest request = new PostCreateRequest(
                "FREE", "테스트 게시글", "테스트 내용", null);

        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(mockUser));
        given(postRepository.save(any(Post.class)))
                .willReturn(mockPost);

        // when
        PostResponse response = postService.createPost(request, "user@test.com");

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 게시글");
        assertThat(response.getAuthor()).isEqualTo("일반유저");
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("공지사항 작성 실패 - 권한 없음 (USER 등급)")
    void createPost_fail_notice_unauthorized() {

        /**
         * PostService.createPost()에서 boardType이 NOTICE일 때
         * USER 등급이면 예외를 던지도록 구현했어요.
         * 이 테스트는 그 검증 로직이 정확히 동작하는지 확인해요.
         */
        PostCreateRequest request = new PostCreateRequest(
                "NOTICE", "공지사항 제목", "공지사항 내용", null);

        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> postService.createPost(request, "user@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("공지사항 작성 권한이 없습니다.");
    }

    @Test
    @DisplayName("공지사항 작성 성공 - ADMIN 등급")
    void createPost_success_notice_admin() {

        PostCreateRequest request = new PostCreateRequest(
                "NOTICE", "공지사항 제목", "공지사항 내용", null);

        Post mockNotice = Post.builder()
                .boardType(Post.BoardType.NOTICE)
                .title("공지사항 제목")
                .content("공지사항 내용")
                .author(mockAdmin)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .viewCount(0)
                .build();

        given(userRepository.findByEmail("admin@test.com"))
                .willReturn(Optional.of(mockAdmin));
        given(postRepository.save(any(Post.class)))
                .willReturn(mockNotice);

        // ADMIN이면 공지사항 작성이 가능해야 해요
        PostResponse response = postService.createPost(request, "admin@test.com");

        assertThat(response.getTitle()).isEqualTo("공지사항 제목");
    }

    // ===== updatePost() 테스트 =====

    @Test
    @DisplayName("게시글 수정 성공 - 작성자 본인")
    void updatePost_success_by_author() {

        PostUpdateRequest request = new PostUpdateRequest("수정된 제목", "수정된 내용");

        given(postRepository.findById(1L))
                .willReturn(Optional.of(mockPost));
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(mockUser));

        // when
        PostResponse response = postService.updatePost(1L, request, "user@test.com");

        // then
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
    }

    @Test
    @DisplayName("게시글 수정 성공 - 관리자 (타인 글도 수정 가능)")
    void updatePost_success_by_admin() {

        PostUpdateRequest request = new PostUpdateRequest("관리자가 수정", "수정된 내용");

        given(postRepository.findById(1L))
                .willReturn(Optional.of(mockPost)); // mockPost의 작성자는 mockUser
        given(userRepository.findByEmail("admin@test.com"))
                .willReturn(Optional.of(mockAdmin)); // 수정 시도자는 관리자

        // 관리자는 타인 글도 수정 가능해야 해요
        PostResponse response = postService.updatePost(1L, request, "admin@test.com");

        assertThat(response.getTitle()).isEqualTo("관리자가 수정");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 타인 글 수정 시도 (USER 등급)")
    void updatePost_fail_unauthorized() {

        PostUpdateRequest request = new PostUpdateRequest("수정 시도", "수정 내용");

        User anotherUser = User.builder()
                .email("other@test.com")
                .password("암호화된비밀번호")
                .nickname("다른유저")
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        given(postRepository.findById(1L))
                .willReturn(Optional.of(mockPost)); // 작성자는 mockUser
        given(userRepository.findByEmail("other@test.com"))
                .willReturn(Optional.of(anotherUser)); // 수정 시도자는 다른 유저

        assertThatThrownBy(() ->
                postService.updatePost(1L, request, "other@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수정 권한이 없습니다.");
    }

    // ===== deletePost() 테스트 =====

    @Test
    @DisplayName("게시글 삭제 성공 - 작성자 본인")
    void deletePost_success_by_author() {

        given(postRepository.findById(1L))
                .willReturn(Optional.of(mockPost));
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(mockUser));

        postService.deletePost(1L, "user@test.com");

        /**
         * Tangerine은 소프트 삭제를 써요.
         * postRepository.delete()가 아니라 post.delete()를 호출한 뒤
         * JPA 더티 체킹으로 UPDATE 쿼리가 나가는 구조예요.
         * 그래서 verify로 delete() 호출을 검증하는 대신
         * mockPost의 deleted 상태가 바뀌었는지를 검증해요.
         *
         * isTrue(): boolean 값이 true인지 확인해요.
         */
        assertThat(mockPost.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void deletePost_success_by_admin() {

        User anotherUser = User.builder()
                .email("other@test.com")
                .password("암호화된비밀번호")
                .nickname("다른유저")
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        given(postRepository.findById(1L))
                .willReturn(Optional.of(mockPost));
        given(userRepository.findByEmail("other@test.com"))
                .willReturn(Optional.of(anotherUser));

        assertThatThrownBy(() ->
                postService.deletePost(1L, "other@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 권한이 없습니다.");
    }
}
