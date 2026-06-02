package com.tangerine.tangerine.domain.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    private String boardType;
    private String title;
    private String content;
    private Long taggedDocumentId;
}
