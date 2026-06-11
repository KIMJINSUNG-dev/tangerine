package com.tangerine.tangerine.domain.wiki;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByIdAndDeletedFalse(Long id);
    Page<Document> findByDocumentTypeIdAndDeletedFalse(Long id, Pageable pageable);
    Page<Document> findByTitleContainingAndDeletedFalse(String keyword, Pageable pageable);
    Page<Document> findByTitleContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);
}
