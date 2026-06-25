package com.tangerine.tangerine.domain.wiki;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_templates")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * [설명] "SONG 유형은 title, composer, bpm, genre 필드를 가진다"는
     * 설계도예요. 실제 문서 데이터가 아니라 "이 유형엔 어떤 필드가
     * 있어야 하는지"에 대한 메타데이터예요.
     *
     * Pebble의 PostCreateRequest처럼 필드 구조를 Java 코드(DTO)로
     * 고정해둔 게 아니라, DB 데이터로 관리해요.
     * 그래서 새 필드(예: "난이도")를 추가하고 싶으면
     * 코드 배포 없이 이 테이블에 INSERT만 하면 돼요.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private DocumentType documentType;

    @Column(nullable = false, length = 50)
    private String fieldKey;       // 예: "bpm" (실제 데이터 식별용 키)

    @Column(nullable = false, length = 50)
    private String fieldName;      // 예: "BPM" (화면에 보여줄 한글/표시명)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FieldType fieldType;   // TEXT/NUMBER/DATE/BOOLEAN/REFERENCE

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int displayOrder;      // 화면에 필드를 보여줄 순서

    public enum FieldType {

        TEXT, NUMBER, DATE, BOOLEAN, REFERENCE
    }
}
