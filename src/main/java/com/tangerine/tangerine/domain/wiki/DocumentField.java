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

    @Column(nullable = false, length = 50)
    private String fieldKey;

    @Column(columnDefinition = "TEXT")
    private String fieldValue;

    public void updateValue(String fieldValue) {

        this.fieldValue = fieldValue;
    }
}
