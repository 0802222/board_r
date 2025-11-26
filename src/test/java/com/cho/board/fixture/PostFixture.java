package com.cho.board.fixture;

import com.cho.board.category.entity.Category;
import com.cho.board.post.entity.Post;
import com.cho.board.user.entity.User;

public class PostFixture {

    public static Post createDefaultPost(User author, Category category) {
        return Post.builder()
            .title("테스트 제목")
            .content("테스트 내용")
            .author(author)
            .category(category)
            .build();
    }

    // 커스텀 제목과 내용으로 게시글 생성
    public static Post createPost(String title, String content, User author, Category category) {
        return Post.builder()
            .title(title)
            .content(content)
            .author(author)
            .category(category)
            .build();
    }

    // 긴 내용의 게시글 생성 (페이징 테스트 등에 유용)
    public static Post createLongPost(User author, Category category) {
        String longContent = "이것은 긴 내용입니다. ".repeat(100);
        return Post.builder()
            .title("긴 내용 게시글")
            .content(longContent)
            .author(author)
            .category(category)
            .build();
    }

    // 조회수가 높은 인기 게시글 생성
    public static Post createPopularPost(User author, Category category) {
        Post post = Post.builder()
            .title("인기 게시글")
            .content("조회수가 많은 게시글")
            .author(author)
            .category(category)
            .build();

        // 조회수 증가
        post.increaseViewCount();
        post.increaseViewCount();
        post.increaseViewCount();

        return post;
    }

    // 여러 게시글을 한번에 생성 (페이징 테스트에 유용)
    public static java.util.List<Post> createMultiplePosts(int count, User author,
        Category category) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> Post.builder()
                .title("제목 " + (i + 1))
                .content("내용 " + (i + 1))
                .author(author)
                .category(category)
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    // 특정 카테고리에 여러 게시글 생성
    public static java.util.List<Post> createPostsWithCategory(
        int count,
        User author,
        Category category,
        String titlePrefix
    ) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> Post.builder()
                .title(titlePrefix + " " + (i + 1))
                .content("내용 " + (i + 1))
                .author(author)
                .category(category)
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
}