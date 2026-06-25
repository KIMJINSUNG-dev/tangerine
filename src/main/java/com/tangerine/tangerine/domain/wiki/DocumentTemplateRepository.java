package com.tangerine.tangerine.domain.wiki;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    List<DocumentTemplate> findByDocumentTypeIdOrderByDisplayOrderAsc(Long typeId);

    /**
     * [추가] "이 유형에 이 fieldKey가 이미 등록되어 있는가?"를 확인해요.
     * 관리자가 같은 필드를 중복으로 추가하는 걸 막을 때 써요.
     */
    boolean existsByDocumentTypeIdAndFieldKey(Long typeId, String fieldKey);
}
