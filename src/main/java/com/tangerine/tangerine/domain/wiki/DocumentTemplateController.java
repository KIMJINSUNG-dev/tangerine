package com.tangerine.tangerine.domain.wiki;

import com.tangerine.tangerine.domain.wiki.dto.DocumentTemplateRequest;
import com.tangerine.tangerine.domain.wiki.dto.DocumentTemplateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [추가] /api/admin/templates로 경로를 잡아서
 * SecurityConfig의 "/api/admin/**" → hasAnyRole(MANAGER, ADMIN) 규칙에
 * 자동으로 걸리도록 해요. 별도 보안 설정을 또 안 적어도 돼요.
 */
@RestController
@RequestMapping("/api/admin/templates")
@RequiredArgsConstructor
public class DocumentTemplateController {

    private final DocumentTemplateService documentTemplateService;

    @PostMapping
    public ResponseEntity<DocumentTemplateResponse> createTemplate(
            @RequestBody DocumentTemplateRequest request) {

        return ResponseEntity.status(201).body(documentTemplateService.createTemplate(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {

        documentTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
