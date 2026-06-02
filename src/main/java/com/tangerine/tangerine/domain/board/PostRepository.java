package com.tangerine.tangerine.domain.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByBoardTypeAndDeletedFalse(Post.BoardType boardType, Pageable pageable);
    Page<Post> findByBoardTypeAndDeletedFalseAndTitleContaining(Post.BoardType boardType, String keyword, Pageable pageable);
    Page<Post> findByTaggedDocumentIdAndDeletedFalse(Long documentId, Pageable pageable);
}
