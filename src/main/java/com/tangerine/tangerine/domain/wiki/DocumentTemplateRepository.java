package com.tangerine.tangerine.domain.wiki;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    List<DocumentTemplate> findByDocumentTypeIdOrderByDisplayOrderAsc(Long typeId);
}
