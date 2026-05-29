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
}
