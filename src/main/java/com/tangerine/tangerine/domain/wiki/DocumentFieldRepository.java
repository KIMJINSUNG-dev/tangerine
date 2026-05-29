package com.tangerine.tangerine.domain.wiki;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentFieldRepository extends JpaRepository<DocumentField, Long> {

    List<DocumentField> findByDocumentId(Long documentId);
    Optional<DocumentField> findByDocumentIdAndFieldKey(Long documentId, String fieldKey);
    void deleteByDocumentId(Long documentId);
}
