package com.cho.board.post.entity;

import com.cho.board.category.entity.Category;
import com.cho.board.comment.entity.Comment;
import com.cho.board.global.common.entity.BaseEntity;
import com.cho.board.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "posts",
    indexes = {
        @Index(name = "idx_post_created_at", columnList = "created_at"),
        @Index(name = "idx_post_view_count", columnList = "view_count"),
        @Index(name = "idx_post_category_id", columnList = "category_id")
    })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column
    private LocalDateTime scheduledAt;

    @Column(name = "is_scheduled")
    private boolean Scheduled;

    // FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    public void update(String title, String content, Category category) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        if (category != null) {
            this.category = category;
        }
        updateTimestamp();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public boolean isAuthor(String email) {
        return this.author.getEmail().equals(email);
    }

    // PostImage 관련 필드
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();

    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this);
    }

    public void removeImage(PostImage image) {
        images.remove(image);
        image.setPost(null);
    }

    public void clearImages() {
        images.clear();
    }
}
