package com.tangerine.tangerine.domain.board;

import com.tangerine.tangerine.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * [설명] Pebble은 댓글 기능 자체가 없었어요.
 * 구조는 Post와 거의 같은 패턴이에요. (Entity → 도메인 메서드)
 * CommentResponse에서 isDeleted()인 댓글은 "삭제된 댓글입니다."로
 * 내용을 가려서 보여주되, 댓글이 달렸던 흔적(대화의 맥락)은 유지해요.
 * 이것도 소프트 삭제를 쓰는 이유예요. 누군가의 답글이 "삭제된 댓글에
 * 대한 답글"인데 원댓글이 통째로 사라지면 대화 흐름이 끊겨 보여요.
 */
@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted;

    public void update(String content) {

        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {

        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }
}
