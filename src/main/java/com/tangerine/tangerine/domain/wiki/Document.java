package com.tangerine.tangerine.domain.wiki;

import com.tangerine.tangerine.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private DocumentType documentType;

    /**
     * [설명] 여기 있는 필드(id, documentType, title, createdBy, 날짜들)는
     * "모든 문서 유형이 공통으로 가지는 것"만 모아둔 거예요.
     * Pebble의 Post Entity와 역할이 거의 같아요.
     * 다른 점은 title 외의 가변적인 내용은 전혀 여기 없다는 거예요.
     * (bpm, composer 같은 건 다음에 볼 DocumentField에 따로 있어요)
     */
    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted;

    public void update(String title) {

        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {

        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * [설명] title은 안 바뀌고 필드(DocumentField)만 바뀔 때
     * updatedAt을 갱신하기 위해 따로 만든 메서드예요.
     * (예전에 "필드만 수정했는데 updatedAt이 null로 남는" 버그를
     *  고치면서 추가했던 메서드예요)
     */
    public void touch() {

        this.updatedAt = LocalDateTime.now();
    }
}
