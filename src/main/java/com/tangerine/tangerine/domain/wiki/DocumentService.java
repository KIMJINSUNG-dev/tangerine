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

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentFieldRepository documentFieldRepository;
    private final DocumentHistoryRepository documentHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request, String email) {

        DocumentType documentType = documentTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서 유형입니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

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

        Map<String, Map<String, String>> changeFields = new HashMap<>();

        if (request.getFields() != null) {

            request.getFields().forEach((key, newValue) -> {

                documentFieldRepository.findByDocumentIdAndFieldKey(id, key)
                        .ifPresentOrElse(
                                field -> {

                                    if (!field.getFieldValue().equals(newValue)) {

                                        Map<String, String> change = new HashMap<>();
                                        change.put("before", field.getFieldValue());
                                        change.put("after", newValue);
                                        changeFields.put(key, change);
                                        field.updateValue(newValue);
                                    }
                                },
                                () -> {

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
