package com.tangerine.tangerine.domain.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostUpdateRequest {

    private String title;
    private String content;

    public PostUpdateRequest(String title, String content) {

        this.title = title;
        this.content = content;
    }
}
