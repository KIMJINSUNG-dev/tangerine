package com.tangerine.tangerine.domain.board;

import com.tangerine.tangerine.domain.board.dto.PostCreateRequest;
import com.tangerine.tangerine.domain.board.dto.PostResponse;
import com.tangerine.tangerine.domain.board.dto.PostUpdateRequest;
import com.tangerine.tangerine.domain.user.User;
import com.tangerine.tangerine.domain.user.UserRepository;
import com.tangerine.tangerine.domain.wiki.Document;
import com.tangerine.tangerine.domain.wiki.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    public PostResponse createPost(PostCreateRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Document taggedDocument = null;
        if (request.getTaggedDocumentId() != null) {

            taggedDocument = documentRepository.findByIdAndDeletedFalse(
                    request.getTaggedDocumentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다."));
        }

        Post post = Post.builder()
                .boardType(Post.BoardType.valueOf(request.getBoardType()))
                .title(request.getTitle())
                .content(request.getContent())
                .author(user)
                .taggedDocument(taggedDocument)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .viewCount(0)
                .build();

        postRepository.save(post);
        return new PostResponse(post);
    }

    @Transactional
    public PostResponse getPost(Long id) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (post.isDeleted()) {

            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }

        post.increaseViewCount();
        return new PostResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(String boardType, Pageable pageable) {

        return postRepository
                .findByBoardTypeAndDeletedFalse(
                        Post.BoardType.valueOf(boardType), pageable)
                .map(PostResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String boardType, String keyword, Pageable pageable) {

        return postRepository
                .findByBoardTypeAndDeletedFalseAndTitleContaining(
                        Post.BoardType.valueOf(boardType), keyword, pageable)
                .map(PostResponse::new);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostUpdateRequest request, String email) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (!post.getAuthor().getEmail().equals(email)) {

            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        post.update(request.getTitle(), request.getContent());
        return new PostResponse(post);
    }

    @Transactional
    public void deletePost(Long id, String email) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (!post.getAuthor().getEmail().equals(email)) {

            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        post.delete();
    }
}
