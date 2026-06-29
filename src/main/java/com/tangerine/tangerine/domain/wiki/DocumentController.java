package com.tangerine.tangerine.domain.wiki;

import com.tangerine.tangerine.domain.wiki.dto.DocumentCreateRequest;
import com.tangerine.tangerine.domain.wiki.dto.DocumentResponse;
import com.tangerine.tangerine.domain.wiki.dto.DocumentTemplateResponse;
import com.tangerine.tangerine.domain.wiki.dto.DocumentUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentTemplateService documentTemplateService;

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @RequestBody DocumentCreateRequest request,
            @AuthenticationPrincipal String email) {

        DocumentResponse response = documentService.createDocument(request, email);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {

        DocumentResponse response = documentService.getDocument(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{typeId}")
    public ResponseEntity<Page<DocumentResponse>> getDocumentByType(
            @PathVariable Long typeId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<DocumentResponse> response = documentService.getDocumentsByType(typeId, keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentUpdateRequest request,
            @AuthenticationPrincipal String email) {

        DocumentResponse response = documentService.updateDocument(id, request, email);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {

        documentService.deleteDocument(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DocumentResponse>> searchDocuments(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<DocumentResponse> response = documentService.searchDocuments(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가] 누구나 호출 가능 (GET이라 SecurityConfig의
     * .requestMatchers(HttpMethod.GET, "/api/documents/**").permitAll()
     * 규칙에 이미 포함돼요. 별도 설정 불필요)
     */
    @GetMapping("/type/{typeId}/template")
    public ResponseEntity<List<DocumentTemplateResponse>> getTemplate(@PathVariable Long typeId) {

        return ResponseEntity.ok(documentTemplateService.getTemplatesByType(typeId));
    }
}
