package com.tangerine.tangerine.domain.wiki;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_fields")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /**
     * [설명] 이 두 컬럼이 EAV 패턴의 본질이에요.
     *
     * fieldKey   = "bpm"  (어떤 속성인지)
     * fieldValue = "155"  (그 속성의 값)
     *
     * Pebble식이었다면 Post Entity에
     *   private int bpm;
     * 처럼 컬럼을 직접 박아넣었을 텐데,
     * 여긴 "이 문서(document)는 bpm이라는 속성이 155라는 값을 가진다"는
     * 행 하나로 표현해요.
     *
     * 같은 문서(documentId=1)에 대해 이 테이블에 여러 행이 생겨요.
     *   (1, "composer", "Ryu☆")
     *   (1, "bpm", "155")
     *   (1, "genre", "TRANCE")
     * 이 세 행을 합쳐서 "문서 1번의 필드들"이 되는 거예요.
     */
    @Column(nullable = false, length = 50)
    private String fieldKey;

    @Column(columnDefinition = "TEXT")
    private String fieldValue;

    public void updateValue(String fieldValue) {

        this.fieldValue = fieldValue;
    }
}
