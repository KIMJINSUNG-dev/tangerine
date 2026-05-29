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

    public void touch() {

        this.updatedAt = LocalDateTime.now();
    }
}
