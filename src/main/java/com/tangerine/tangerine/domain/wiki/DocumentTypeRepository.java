package com.tangerine.tangerine.domain.wiki;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {

    Optional<DocumentType> findByName(String name);
    boolean existsByName(String name);
}
