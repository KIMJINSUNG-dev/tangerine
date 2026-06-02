package com.tangerine.tangerine.domain.board.dto;

import com.tangerine.tangerine.domain.board.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponse {

    private Long id;
    private String boardType;
    private String title;
    private String content;
    private String author;
    private Long taggedDocumentId;
    private String taggedDocumentTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCount;
    private int commentCount;

    public PostResponse(Post post) {

        this.id = post.getId();
        this.boardType = post.getBoardType().name();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.author = post.getAuthor() != null
                ? post.getAuthor().getNickname()
                : "알 수 없음";
        this.taggedDocumentId = post.getTaggedDocument() != null
                ? post.getTaggedDocument().getId()
                : null;
        this.taggedDocumentTitle = post.getTaggedDocument() != null
                ? post.getTaggedDocument().getTitle()
                : null;
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.viewCount = post.getViewCount();
        this.commentCount = post.getComments().size();
    }
}
