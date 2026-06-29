package com.tangerine.tangerine.domain.wiki.dto;

import com.tangerine.tangerine.domain.wiki.Document;
import com.tangerine.tangerine.domain.wiki.DocumentField;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class DocumentResponse {

    private Long id;
    private Long typeId;
    private String typeName;
    private String title;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, String> fields;

    public DocumentResponse(Document document, List<DocumentField> fieldList) {

        this.id = document.getId();
        this.typeId = document.getDocumentType().getId();
        this.typeName = document.getDocumentType().getName();
        this.title = document.getTitle();
        this.createdBy = document.getCreatedBy() != null
            ? document.getCreatedBy().getNickname()
            : "알 수 없음";
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        this.fields = fieldList.stream()
                .collect(Collectors.toMap(
                        DocumentField::getFieldKey,
                        DocumentField::getFieldValue
                ));
    }
}
