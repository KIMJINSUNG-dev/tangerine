package com.tangerine.tangerine.domain.wiki;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangerine.tangerine.domain.user.User;
import com.tangerine.tangerine.domain.user.UserRepository;
import com.tangerine.tangerine.domain.wiki.dto.DocumentCreateRequest;
import com.tangerine.tangerine.domain.wiki.dto.DocumentResponse;
import com.tangerine.tangerine.domain.wiki.dto.DocumentUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentFieldRepository documentFieldRepository;
    private final DocumentHistoryRepository documentHistoryRepository;
    private final DocumentTemplateRepository documentTemplateRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request, String email) {

        DocumentType documentType = documentTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서 유형입니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        /**
         * [추가] 핵심 검증 로직
         *
         * 1. 이 문서 유형(typeId)에 등록된 템플릿 필드 키 목록을 가져와요.
         * 2. 요청으로 들어온 fields의 키들이 전부 그 목록 안에 있는지 확인해요.
         * 3. 목록에 없는 키가 하나라도 있으면 통째로 거부해요.
         *
         * .map(DocumentTemplate::getFieldKey): 각 DocumentTemplate 객체에서
         * fieldKey 문자열만 뽑아서 Set으로 모아요. (포함 여부 검사가 빨라서
         * List보다 Set을 써요)
         */
        if (request.getFields() != null) {

            Set<String> allowedKeys = documentTemplateRepository
                    .findByDocumentTypeIdOrderByDisplayOrderAsc(request.getTypeId())
                    .stream()
                    .map(DocumentTemplate::getFieldKey)
                    .collect(Collectors.toSet());

            for (String key : request.getFields().keySet()) {

                if (!allowedKeys.contains(key)) {

                    throw new IllegalArgumentException("정의되지 않은 필드입니다: " + key);
                }
            }
        }

        Document document = Document.builder()
                .documentType(documentType)
                .title(request.getTitle())
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        documentRepository.save(document);

        if (request.getFields() != null) {

            request.getFields().forEach((key, value) -> {

                DocumentField field = DocumentField.builder()
                        .document(document)
                        .fieldKey(key)
                        .fieldValue(value)
                        .build();
                documentFieldRepository.save(field);
            });
        }

        List<DocumentField> fieldList = documentFieldRepository.findByDocumentId(document.getId());
        return new DocumentResponse(document, fieldList);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long id) {

        Document document = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다."));

        List<DocumentField> fieldList = documentFieldRepository.findByDocumentId(id);
        return new DocumentResponse(document, fieldList);
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> getDocumentsByType(Long typeId, Pageable pageable) {

        return documentRepository
                .findByDocumentTypeIdAndDeletedFalse(typeId, pageable)
                .map(doc -> {

                    List<DocumentField> fields = documentFieldRepository.findByDocumentId(doc.getId());
                    return new DocumentResponse(doc, fields);
                });
    }

    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentUpdateRequest request, String email) {

        Document document = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        /**
         * [추가] 수정 시에도 같은 검증이 필요해요.
         * document.getDocumentType().getId()로 이 문서가 어느 유형인지 가져와요.
         */
        if (request.getFields() != null) {

            Set<String> allowedKeys = documentTemplateRepository
                    .findByDocumentTypeIdOrderByDisplayOrderAsc(document.getDocumentType().getId())
                    .stream()
                    .map(DocumentTemplate::getFieldKey)
                    .collect(Collectors.toSet());

            for (String key : request.getFields().keySet()) {

                if (!allowedKeys.contains(key)) {

                    throw new IllegalArgumentException("정의되지 않은 필드입니다: " + key);
                }
            }
        }

        /**
         * [설명] 변경 내역을 모아둘 임시 Map이에요.
         * 키: 필드명, 값: {"before": "...", "after": "..."} 형태의 또 다른 Map
         * Pebble의 Post.update()는 변경 여부를 따지지 않고 무조건
         * 새 값으로 덮어썼는데, 여긴 "진짜로 값이 달라졌는지"를
         * 일일이 비교해서 달라진 것만 changedFields에 모아요.
         */
        Map<String, Map<String, String>> changeFields = new HashMap<>();

        if (request.getFields() != null) {

            request.getFields().forEach((key, newValue) -> {

                documentFieldRepository.findByDocumentIdAndFieldKey(id, key)
                        .ifPresentOrElse(
                                field -> {

                                    // 기존 필드가 있는 경우: 값이 다를 때만 기록 + 갱신
                                    if (!field.getFieldValue().equals(newValue)) {

                                        Map<String, String> change = new HashMap<>();
                                        change.put("before", field.getFieldValue());
                                        change.put("after", newValue);
                                        changeFields.put(key, change);
                                        field.updateValue(newValue);
                                        // ↑ [설명] save() 호출이 없어요!
                                        // @Transactional 안에서 Entity의 값을 바꾸면
                                        // JPA가 "더티 체킹"으로 변경을 감지해서
                                        // 트랜잭션이 끝날 때 자동으로 UPDATE 쿼리를 날려요.
                                    }
                                },
                                () -> {

                                    // 기존에 없던 새 필드인 경우: 새로 INSERT
                                    DocumentField newField = DocumentField.builder()
                                            .document(document)
                                            .fieldKey(key)
                                            .fieldValue(newValue)
                                            .build();
                                    documentFieldRepository.save(newField);
                                    Map<String, String> change = new HashMap<>();
                                    change.put("before", null);
                                    change.put("after", newValue);
                                    changeFields.put(key, change);
                                }
                        );
            });
        }

        if (request.getTitle() != null && !request.getTitle().equals(document.getTitle())) {

            Map<String, String> titleChange = new HashMap<>();
            titleChange.put("before", document.getTitle());
            titleChange.put("after", request.getTitle());
            changeFields.put("title", titleChange);
            document.update(request.getTitle());
        }

        /**
         * [설명] 실제로 뭔가 바뀐 게 있을 때만 이력을 저장해요.
         * (아무것도 안 바뀌었는데 빈 이력이 쌓이면 의미 없으니까요)
         *
         * objectMapper.writeValueAsString(changedFields)
         * → Java의 Map 객체를 JSON 문자열로 직접 변환해요.
         *   Controller의 @RequestBody/@ResponseBody는 Spring이
         *   자동으로 이 변환을 대신 해주는데, 여긴 DB 컬럼에
         *   문자열로 저장해야 하니까 ObjectMapper를 직접 호출해서 변환해요.
         *   (JacksonConfig에서 Bean으로 등록해둔 그 ObjectMapper예요)
         */
        if (!changeFields.isEmpty()) {

            document.touch();
            try {

                String changeFieldsJson = objectMapper.writeValueAsString(changeFields);
                DocumentHistory history = DocumentHistory.builder()
                        .document(document)
                        .editedBy(user)
                        .editedAt(LocalDateTime.now())
                        .changedFields(changeFieldsJson)
                        .build();
                documentHistoryRepository.save(history);
            } catch (JsonProcessingException e) {

                throw new RuntimeException("편집 이력 저장 중 오류가 발생했습니다.", e);
            }
        }

        List<DocumentField> fieldList = documentFieldRepository.findByDocumentId(id);
        return new DocumentResponse(document, fieldList);
    }

    @Transactional
    public void deleteDocument(Long id, String email) {

        Document document = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다."));

        document.delete();
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> searchDocuments(String keyword, Pageable pageable) {

        return documentRepository
                .findByTitleContainingIgnoreCaseAndDeletedFalse(keyword, pageable)
                .map(doc -> {

                    List<DocumentField> fields = documentFieldRepository.findByDocumentId(doc.getId());
                    return new DocumentResponse(doc, fields);
                });
    }
}
