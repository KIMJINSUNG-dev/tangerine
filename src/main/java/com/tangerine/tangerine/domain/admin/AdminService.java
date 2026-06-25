package com.tangerine.tangerine.domain.admin;

import com.tangerine.tangerine.domain.board.Comment;
import com.tangerine.tangerine.domain.board.CommentRepository;
import com.tangerine.tangerine.domain.board.Post;
import com.tangerine.tangerine.domain.board.PostRepository;
import com.tangerine.tangerine.domain.user.User;
import com.tangerine.tangerine.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [설명] AdminService/AdminController는 Pebble에 통째로 없던
 * 새로운 "계층"이에요. 지금까지 본 PostService/CommentService는
 * "자기 글이거나 관리자면 허용"하는 정도였는데,
 * 이 패키지는 아예 "관리자만 쓸 수 있는 별도의 API 묶음"이에요.
 *
 * SecurityConfig에서 이렇게 막아놓은 부분과 짝을 이뤄요.
 *   .requestMatchers("/api/admin/**").hasAnyRole("MANAGER", "ADMIN")
 * 즉 USER/TRUSTED 등급은 이 컨트롤러의 어떤 엔드포인트도
 * 호출 자체가 안 돼요. (Service까지 도달하기 전에 필터에서 막혀요)
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Page<User> getUsers(Pageable pageable) {

        return userRepository.findAll(pageable);
    }

    @Transactional
    public void changeUserRole(Long userId, String role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        /**
         * [설명] User.Role.valueOf(role)
         * 문자열 "TRUSTED"를 User.Role.TRUSTED enum 값으로 변환해요.
         * 만약 존재하지 않는 값("SUPERADMIN" 같은)이 오면
         * IllegalArgumentException이 자동으로 발생해서 잘못된 등급
         * 부여를 막아줘요. (Enum이 가진 안전장치)
         */
        user.changeRole(User.Role.valueOf(role));
    }

    @Transactional
    public void deletePost(Long postId) {

        // [설명] PostService.deletePost()와 달리 작성자 일치 여부를
        // 전혀 체크하지 않아요. 이 메서드를 호출할 수 있다는 것
        // 자체가 이미 SecurityConfig를 통과한 관리자라는 뜻이라서요.
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        post.delete();
    }

    @Transactional
    public void deleteComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        comment.delete();
    }
}
