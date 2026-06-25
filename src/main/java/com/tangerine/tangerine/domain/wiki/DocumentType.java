package com.tangerine.tangerine.domain.wiki;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_types")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * [설명] "SONG", "COMPOSER", "GAME" 같은 문서 유형 이름이 들어가요.
     * 이 테이블에 행을 하나 추가하는 것만으로 새 유형이 생겨요.
     * (Pebble식이었다면 새 유형 추가 = 새 Entity 클래스 + 새 테이블이
     *  필요했을 텐데, 여기선 INSERT 한 줄로 끝나요)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;
}
