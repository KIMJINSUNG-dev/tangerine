package com.tangerine.tangerine.domain.wiki;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/document-types")
@RequiredArgsConstructor
public class DocumentTypeController {

    private final DocumentTypeRepository documentTypeRepository;

    @GetMapping
    public ResponseEntity<List<DocumentType>> getAll() {

        return ResponseEntity.ok(documentTypeRepository.findAll());
    }
}
