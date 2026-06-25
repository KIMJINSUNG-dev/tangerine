package com.tangerine.tangerine.domain.wiki;

import com.tangerine.tangerine.domain.wiki.dto.DocumentTemplateRequest;
import com.tangerine.tangerine.domain.wiki.dto.DocumentTemplateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [추가] 신규 Service
 *
 * DocumentService와 분리한 이유: DocumentService는 "문서 내용"을
 * 다루고, 이 Service는 "문서 구조(스키마)"를 다뤄요.
 * 책임이 다르면 클래스도 나누는 게 Tangerine 전체에서
 * 일관되게 지켜온 원칙이에요. (UserService/PostService처럼)
 */
@Service
@RequiredArgsConstructor
public class DocumentTemplateService {

    private final DocumentTemplateRepository documentTemplateRepository;
    private final DocumentTypeRepository documentTypeRepository;

    /**
     * 특정 유형의 템플릿(필드 목록) 조회
     * 관리자 화면뿐 아니라 일반 사용자의 문서 작성 화면에서도
     * "입력해야 할 필드가 뭔지" 알려주기 위해 이 메서드를 써요.
     * 그래서 이 메서드 자체는 누구나 호출 가능하게 열어둘 거예요.
     */
    @Transactional(readOnly = true)
    public List<DocumentTemplateResponse> getTemplatesByType(Long typeId) {

        return documentTemplateRepository
                .findByDocumentTypeIdOrderByDisplayOrderAsc(typeId)
                .stream()
                .map(DocumentTemplateResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 템플릿 필드 추가 (관리자 전용)
     * 이 메서드를 호출하는 Controller 엔드포인트는
     * SecurityConfig에서 /api/admin/** 으로 묶여서 보호돼요.
     */

    public DocumentTemplateResponse createTemplate(DocumentTemplateRequest request) {

        DocumentType documentType = documentTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서 유형입니다."));

        if (documentTemplateRepository.existsByDocumentTypeIdAndFieldKey(
                request.getTypeId(), request.getFieldKey())) {
            throw new IllegalArgumentException("이미 존재하는 필드입니다.");
        }

        DocumentTemplate template = DocumentTemplate.builder()
                .documentType(documentType)
                .fieldKey(request.getFieldKey())
                .fieldName(request.getFieldName())
                .fieldType(DocumentTemplate.FieldType.valueOf(request.getFieldType()))
                .required(request.isRequired())
                .displayOrder(request.getDisplayOrder())
                .build();

        documentTemplateRepository.save(template);
        return new DocumentTemplateResponse(template);
    }

    /**
     * 템플릿 필드 삭제 (관리자 전용)
     *
     * [주의할 점] 이미 문서들이 이 필드(fieldKey)로 값을 갖고 있을 수 있어요.
     * 템플릿만 지우고 DocumentField는 그대로 두면, 그 문서를 다시
     * 수정할 때 "정의되지 않은 필드인데 값이 남아있는" 상태가 돼요.
     * 지금은 단순하게 템플릿만 지우고, 기존 문서의 값은 그대로 둬요.
     * (값이 화면에는 안 보이지만 DB에는 남아있는 상태가 돼요.
     *  실무에서는 이런 경우 "필드를 완전히 지우기보다 비활성화 처리"하는
     *  방식을 더 많이 써요. 지금은 학습 목적상 단순하게 가요)
     */

    public void deleteTemplate(Long templateId) {

        DocumentTemplate template = documentTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 필드입니다."));

        documentTemplateRepository.delete(template);
    }
}
