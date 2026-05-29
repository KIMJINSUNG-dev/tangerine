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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private DocumentType documentType;

    @Column(nullable = false, length = 50)
    private String fieldKey;

    @Column(nullable = false, length = 50)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FieldType fieldType;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int displayOrder;

    public enum FieldType {

        TEXT, NUMBER, DATE, BOOLEAN, REFERENCE
    }
}
