package com.tangerine.tangerine.domain.wiki;

import com.tangerine.tangerine.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_histories")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by")
    private User editedBy;

    @Column(nullable = false)
    private LocalDateTime editedAt;

    /**
     * [설명] Pebble엔 편집 이력 기능 자체가 없었어요.
     * 이 컬럼엔 "뭐가 어떻게 바뀌었는지"를 JSON 문자열로 저장해요.
     * 예: {"bpm": {"before": "150", "after": "155"}}
     *
     * 왜 별도 컬럼들(beforeValue, afterValue)로 안 나누고
     * 통째로 JSON 문자열 하나에 넣었는지가 중요해요.
     * 한 번의 수정으로 여러 필드(bpm도 바뀌고 genre도 바뀌고)가
     * 동시에 바뀔 수 있어요. 컬럼을 미리 정해두면 "몇 개까지 바뀔 수
     * 있는지" 제한이 생기는데, JSON으로 두면 몇 개가 바뀌든
     * 유연하게 한 행에 다 담을 수 있어요.
     */
    @Column(columnDefinition = "TEXT")
    private String changedFields;
}
