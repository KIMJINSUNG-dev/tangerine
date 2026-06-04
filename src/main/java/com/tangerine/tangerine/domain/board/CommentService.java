package com.tangerine.tangerine.domain.board;

import com.tangerine.tangerine.domain.board.dto.CommentResponse;
import com.tangerine.tangerine.domain.user.User;
import com.tangerine.tangerine.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(Long postId, String content, String email) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Comment comment = Comment.builder()
                .post(post)
                .author(user)
                .content(content)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        commentRepository.save(comment);
        return new CommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {

        return commentRepository
                .findByPostIdAndDeletedFalseOrderByCreatedAtDesc(postId)
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, String content, String email) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean isAdmin = user.getRole() == User.Role.ADMIN
                || user.getRole() == User.Role.MANAGER;

        if (!isAdmin && !comment.getAuthor().getEmail().equals(email)) {

            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        comment.update(content);
        return new CommentResponse(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, String email) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean isAdmin = user.getRole() == User.Role.ADMIN
                || user.getRole() == User.Role.MANAGER;

        if (!isAdmin && !comment.getAuthor().getEmail().equals(email)) {

            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        comment.delete();
    }
}
