package com.tangerine.tangerine.domain.wiki.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class DocumentCreateRequest {

    private Long typeId;
    private String title;
    private Map<String, String> fields;

    public DocumentCreateRequest(Long typeId, String title, Map<String, String> fields) {

        this.typeId = typeId;
        this.title = title;
        this.fields = fields;
    }
}
