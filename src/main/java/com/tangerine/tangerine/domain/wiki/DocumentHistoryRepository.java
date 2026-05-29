package com.tangerine.tangerine.domain.wiki;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Long> {

    Page<DocumentHistory> findByDocumentIdOrderByEditedAtDesc(Long documentId, Pageable pageable);
}
