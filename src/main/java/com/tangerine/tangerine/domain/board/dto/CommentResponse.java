package com.tangerine.tangerine.domain.board.dto;

import com.tangerine.tangerine.domain.board.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {

    private Long id;
    private String author;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponse(Comment comment) {

        this.id = comment.getId();
        this.author = comment.getAuthor() != null
                ? comment.getAuthor().getNickname()
                : "알 수 없음";
        this.content = comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
