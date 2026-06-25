package com.tangerine.tangerine.domain.wiki.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [추가] 관리자가 템플릿 필드를 추가/수정할 때 쓰는 요청 DTO예요.
 * Long typeId: 어느 문서 유형에 속한 필드인지
 */
@Getter
@NoArgsConstructor
public class DocumentTemplateRequest {

    private Long typeId;
    private String fieldKey;
    private String fieldName;
    private String fieldType;   // "TEXT", "NUMBER" 등 문자열로 받아서 Enum으로 변환
    private boolean required;
    private int displayOrder;
}
