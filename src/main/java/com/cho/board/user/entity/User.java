package com.cho.board.user.entity;

import com.cho.board.global.common.entity.BaseEntity;
import com.cho.board.comment.entity.Comment;
import com.cho.board.post.entity.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Column
    private LocalDateTime lastLoginAt;

    @Column
    private int loginCount;

    public void updateNickname(String nickname) {
        if (nickname != null) {
            if (nickname.isBlank()) {
                throw new IllegalArgumentException("Nickname cannot be empty");
            }
            if (nickname.length() > 20) {
                throw new IllegalArgumentException("Nickname too long (limit : 20)");
            }
            this.nickname = nickname;
        }
    }

    public void updateProfileImage(String profileImage) {
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    private static boolean isValidRawPassword(String rawPassword) {
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
        return rawPassword.matches(pattern);
    }

    @OneToMany(mappedBy = "author")
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
}
