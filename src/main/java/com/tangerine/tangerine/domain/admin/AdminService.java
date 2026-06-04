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

        user.changeRole(User.Role.valueOf(role));
    }

    @Transactional
    public void deletePost(Long postId) {

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
