package com.tangerine.tangerine.domain.board;

import com.tangerine.tangerine.domain.board.dto.CommentResponse;
import com.tangerine.tangerine.domain.board.dto.PostCreateRequest;
import com.tangerine.tangerine.domain.board.dto.PostResponse;
import com.tangerine.tangerine.domain.board.dto.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal String email) {

        return ResponseEntity.status(201).body(postService.createPost(request, email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {

        return ResponseEntity.ok(postService.getPost(id));
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @RequestParam String boardType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        if (keyword != null && !keyword.isBlank()) {

            return ResponseEntity.ok(postService.searchPosts(boardType, keyword, pageable));
        }
        return ResponseEntity.ok(postService.getPosts(boardType, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal String email) {

        return ResponseEntity.ok(postService.updatePost(id, request, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {

        postService.deletePost(id, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long id,
            @RequestBody String content,
            @AuthenticationPrincipal String email) {

        return ResponseEntity.status(201).body(commentService.createComment(id, content, email));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<java.util.List<CommentResponse>> getComments(@PathVariable Long id) {

        return ResponseEntity.ok(commentService.getComments(id));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody String content,
            @AuthenticationPrincipal String email) {

        return ResponseEntity.ok(commentService.updateComment(commentId, content, email));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String email) {

        commentService.deleteComment(commentId, email);
        return ResponseEntity.noContent().build();
    }
}
