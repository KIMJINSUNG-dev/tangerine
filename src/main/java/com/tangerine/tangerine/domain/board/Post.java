package com.tangerine.tangerine.domain.board;

import com.tangerine.tangerine.domain.user.User;
import com.tangerine.tangerine.domain.wiki.Document;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * [설명] Pebble의 Post에는 이 필드가 없었어요.
     * Pebble은 게시판이 하나뿐이라 구분이 필요 없었지만,
     * Tangerine은 자유게시판/공지/관리자게시판을 같은 posts 테이블에
     * 같이 저장하고 이 컬럼으로 구분해요.
     * 테이블을 게시판별로 따로 만들지 않은 이유는 게시글 구조
     * (제목/내용/작성자/조회수)가 어느 게시판이든 똑같기 때문이에요.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardType boardType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    /**
     * [설명] Pebble에는 없는 "위키 ↔ 게시판 연결" 기능이에요.
     * 게시글이 특정 위키 문서를 가리킬 수 있어요. nullable이라
     * 태그 없이 글을 쓸 수도 있어요. 예: "5.1.1 패턴 분석"이라는
     * 글을 쓰면서 위키의 "5.1.1" 문서를 같이 태그해서 연결해요.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagged_document_id")
    private Document taggedDocument;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    /**
     * [설명] Pebble은 deletePost()에서 postRepository.delete(post)로
     * DB에서 행을 진짜로 지우는 "하드 삭제"였어요.
     * Tangerine은 deleted 플래그만 true로 바꾸는 "소프트 삭제"예요.
     *
     * 왜 다르게 했는지가 중요해요: Post는 Comment들을 자식으로
     * 가지고 있어요. 게시글을 하드 삭제하면 cascade 설정 때문에
     * 댓글도 통째로 같이 사라져요. 만약 "삭제된 게시글이지만
     * 댓글 내용은 신고 처리를 위해 잠깐 남겨두고 싶다"같은 운영
     * 상황을 대비해 소프트 삭제를 선택했어요.
     * (Pebble은 댓글 기능 자체가 없어서 하드 삭제로도 문제없었어요)
     */
    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private int viewCount;

    /**
     * [설명] Pebble에는 없는 연관관계예요.
     * mappedBy="post": Comment 쪽의 post 필드가 이 관계의 주인이라는 뜻.
     * cascade=ALL: Post에 가해지는 변화(특히 영속화)가 Comment에도 전파.
     * orphanRemoval=true: 이 리스트에서 Comment를 빼면 DB에서도 삭제.
     *
     * 다만 실제 댓글 "소프트" 삭제는 CommentService에서
     * comment.delete()로 처리하니, orphanRemoval은 주로
     * "Post 자체가 정말로 DB에서 사라질 때"를 위한 안전장치예요.
     * (지금은 소프트 삭제만 쓰고 있어서 이 cascade가 실제로
     *  발동하는 경로는 거의 없어요. 잠재적으로 남아있는 설계예요)
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    public enum BoardType {

        FREE, NOTICE, ADMIN
    }

    public void update(String title, String content) {

        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {

        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {

        this.viewCount++;
    }
}
