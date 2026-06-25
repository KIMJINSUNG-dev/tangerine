package com.tangerine.tangerine.domain.wiki.dto;

import com.tangerine.tangerine.domain.wiki.DocumentTemplate;
import lombok.Getter;

/**
 * [추가] 템플릿 조회 응답 DTO예요.
 * 일반 사용자가 "이 유형엔 어떤 필드를 입력해야 하는지" 알기 위해
 * 문서 작성/편집 화면에서도 이 DTO를 받아서 폼을 그려요.
 */
@Getter
public class DocumentTemplateResponse {

    private Long id;
    private String fieldKey;
    private String fieldName;
    private String fieldType;
    private boolean required;
    private int displayOrder;

    public DocumentTemplateResponse(DocumentTemplate template) {

        this.id = template.getId();
        this.fieldKey = template.getFieldKey();
        this.fieldName = template.getFieldName();
        this.fieldType = template.getFieldType().name();
        this.required = template.isRequired();
        this.displayOrder = template.getDisplayOrder();
    }
}
