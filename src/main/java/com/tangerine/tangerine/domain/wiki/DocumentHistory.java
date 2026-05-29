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

    @Column(columnDefinition = "TEXT")
    private String changedFields;
}
