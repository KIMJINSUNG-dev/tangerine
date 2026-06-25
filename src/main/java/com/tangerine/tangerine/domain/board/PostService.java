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

        /**
         * [설명] Pebble의 createPost()에는 이런 분기가 없었어요.
         * (게시판이 하나뿐이라 "어떤 게시판인지에 따른 제약"이 없었으니까요)
         * 여기서는 NOTICE 게시판일 때만 등급을 추가로 확인해요.
         * "로그인했는지"는 SecurityConfig가 이미 걸러줬지만,
         * "그 중에서도 특정 등급만"은 Controller/Security 설정만으로는
         * 표현하기 까다로워서 Service 레벨에서 직접 체크해요.
         */
        if (request.getBoardType().equals("NOTICE")) {

            boolean isAdmin = user.getRole() == User.Role.ADMIN
                    || user.getRole() == User.Role.MANAGER;
            if (!isAdmin) {

                throw new IllegalArgumentException("공지사항 작성 권한이 없습니다.");
            }
        }

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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        /**
         * [설명] Pebble의 updatePost()는
         *   post.getAuthor().getEmail().equals(email) 이거 하나만 체크했어요.
         *   ("작성자 본인인가?" 만 확인)
         *
         * Tangerine은 여기에 OR로 관리자 조건을 추가했어요.
         *   "작성자 본인이 아니어도, 관리자/매니저면 통과시켜라"
         * 이게 권한 체계(4단계 Role)가 실제로 코드에 반영되는 지점이에요.
         * Role enum을 4단계로 늘려놓기만 했지 여기서 활용을 안 했다면
         * 그 등급 구분이 아무 의미가 없었을 거예요.
         */
        boolean isAdmin = user.getRole() == User.Role.ADMIN
                || user.getRole() == User.Role.MANAGER;

        if (!isAdmin && !post.getAuthor().getEmail().equals(email)) {

            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        post.update(request.getTitle(), request.getContent());
        return new PostResponse(post);
    }

    @Transactional
    public void deletePost(Long id, String email) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean isAdmin = user.getRole() == User.Role.ADMIN
                || user.getRole() == User.Role.MANAGER;

        if (!isAdmin && !post.getAuthor().getEmail().equals(email)) {

            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        post.delete();
    }
}
