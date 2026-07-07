package com.tangerine.tangerine.domain.wiki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangerine.tangerine.domain.user.User;
import com.tangerine.tangerine.domain.user.UserRepository;
import com.tangerine.tangerine.domain.wiki.dto.DocumentCreateRequest;
import com.tangerine.tangerine.domain.wiki.dto.DocumentResponse;
import com.tangerine.tangerine.domain.wiki.dto.DocumentUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private DocumentFieldRepository documentFieldRepository;

    @Mock
    private DocumentHistoryRepository documentHistoryRepository;

    @Mock
    private DocumentTemplateRepository documentTemplateRepository;

    @Mock
    private UserRepository userRepository;

    /**
     * ObjectMapper는 Mock 대신 실제 객체를 써요.
     * DocumentService.updateDocument()에서 변경 이력을 JSON으로
     * 변환할 때 ObjectMapper를 직접 사용해요.
     * Mock으로 만들면 변환 로직 자체를 검증할 수 없어서,
     * 실제 ObjectMapper 인스턴스를 그대로 사용해요.
     *
     * @Spy: Mock처럼 가짜 객체를 만들되, 기본 동작은 실제 코드를
     * 그대로 실행해요. 여기서는 @Spy 대신 @InjectMocks가 생성자를
     * 통해 주입하게끔, ObjectMapper를 직접 필드로 두고 @InjectMocks
     * 이 주입하도록 해요.
     */
    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private DocumentService documentService;

    private User mockUser;
    private DocumentType mockDocumentType;
    private Document mockDocument;
    private DocumentTemplate mockTemplate;

    @BeforeEach
    void setUp() {

        mockUser = User.builder()
                .email("user@test.com")
                .password("암호화된비밀번호")
                .nickname("일반유저")
                .createdAt(LocalDateTime.now())
                .build();

        mockDocumentType = DocumentType.builder()
                .id(1L)
                .name("SONG")
                .description("수록곡 문서")
                .build();

        mockDocument = Document.builder()
                .documentType(mockDocumentType)
                .title("테스트 수록곡")
                .createdBy(mockUser)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        // 허용된 fieldKey가 "composer"인 템플릿 설정
        mockTemplate = DocumentTemplate.builder()
                .documentType(mockDocumentType)
                .fieldKey("composer")
                .fieldName("작곡가")
                .fieldType(DocumentTemplate.FieldType.TEXT)
                .required(false)
                .displayOrder(1)
                .build();
    }

    // ===== createDocument() 테스트 =====

    @Test
    @DisplayName("문서 작성 성공")
    void createDocument_success() {

        // given
        DocumentCreateRequest request = new DocumentCreateRequest(
                1L, "테스트 수록곡", Map.of("composer", "Ryu☆"));

        given(documentTypeRepository.findById(1L))
                .willReturn(Optional.of(mockDocumentType));
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(mockUser));

        /**
         * "composer" fieldKey가 허용된 템플릿 목록을 반환해요.
         * DocumentService.createDocument()에서 이 목록을 조회해서
         * 요청으로 들어온 fieldKey가 허용된 것인지 검증해요.
         */
        given(documentTemplateRepository.findByDocumentTypeIdOrderByDisplayOrderAsc(1L))
                .willReturn(List.of(mockTemplate));
        given(documentRepository.save(any(Document.class)))
                .willReturn(mockDocument);
        given(documentFieldRepository.findByDocumentId(any()))
                .willReturn(List.of());

        // when
        DocumentResponse response = documentService.createDocument(
                request, "user@test.com");

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 수록곡");
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    @DisplayName("문서 작성 실패 - 정의되지 않은 필드")
    void createDocument_fail_undefined_field() {

        /**
         * 템플릿에는 "composer"만 허용되어 있는데
         * "unknown_field"를 넣으면 거부되어야 해요.
         * 이 검증이 DocumentService.createDocument() 안에서
         * 정확히 동작하는지 확인해요.
         */
        DocumentCreateRequest request = new DocumentCreateRequest(
                1L, "테스트 수록곡", Map.of("unknown_field", "값"));

        given(documentTypeRepository.findById(1L))
                .willReturn(Optional.of(mockDocumentType));
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(mockUser));
        given(documentTemplateRepository.findByDocumentTypeIdOrderByDisplayOrderAsc(1L))
                .willReturn(List.of(mockTemplate)); // "composer"만 허용

        assertThatThrownBy(() ->
                documentService.createDocument(request, "user@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정의되지 않은 필드입니다");
    }

    // ===== updateDocument() 테스트 =====

    @Test
    @DisplayName("문서 수정 성공")
    void updateDocument_success() {

        DocumentUpdateRequest request = new DocumentUpdateRequest(
                "수정된 제목", Map.of("composer", "DJ YOSHITAKA"));

        DocumentField existingField = DocumentField.builder()
                .document(mockDocument)
                .fieldKey("composer")
                .fieldValue("Ryu☆")
                .build();

        given(documentRepository.findByIdAndDeletedFalse(1L))
                .willReturn(Optional.of(mockDocument));
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(mockUser));
        given(documentTemplateRepository.findByDocumentTypeIdOrderByDisplayOrderAsc(1L))
                .willReturn(List.of(mockTemplate));
        given(documentFieldRepository.findByDocumentIdAndFieldKey(any(), eq("composer")))
                .willReturn(Optional.of(existingField));
        given(documentFieldRepository.findByDocumentId(any()))
                .willReturn(List.of(existingField));

        // when
        DocumentResponse response = documentService.updateDocument(
                1L, request, "user@test.com");

        // then
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
    }

    @Test
    @DisplayName("문서 수정 실패 - 존재하지 않는 문서")
    void updateDocument_fail_not_found() {

        DocumentUpdateRequest request = new DocumentUpdateRequest(
                "수정된 제목", Map.of());

        given(documentRepository.findByIdAndDeletedFalse(999L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                documentService.updateDocument(999L, request, "user@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 문서입니다.");
    }

    // ===== deleteDocument() 테스트 =====

    @Test
    @DisplayName("문서 삭제 성공 - 관리자")
    void deleteDocument_success_by_admin() {

        User adminUser = User.builder()
                .email("admin@test.com")
                .password("암호화된비밀번호")
                .nickname("관리자")
                .role(User.Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        given(documentRepository.findByIdAndDeletedFalse(1L))
                .willReturn(Optional.of(mockDocument));
        given(userRepository.findByEmail("admin@test.com"))
                .willReturn(Optional.of(adminUser));

        documentService.deleteDocument(1L, "admin@test.com");

        /**
         * Tangerine의 deleteDocument()는 소프트 삭제예요.
         * mockDocument.delete()가 호출되어 deleted = true가 됐는지 검증해요.
         */
        assertThat(mockDocument.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("문서 삭제 실패 - 권한 없음 (USER 등급)")
    void deleteDocument_fail_unauthorized() {

        given(documentRepository.findByIdAndDeletedFalse(1L))
                .willReturn(Optional.of(mockDocument));
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(mockUser));

        assertThatThrownBy(() ->
                documentService.deleteDocument(1L, "user@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 권한이 없습니다.");
    }
}
