package com.cho.board.post.repository;

import static com.cho.board.category.entity.QCategory.category;
import static com.cho.board.post.entity.QPost.post;
import static com.cho.board.user.entity.QUser.user;
import static org.springframework.util.StringUtils.hasText;

import com.cho.board.post.dtos.PostSearchCondition;
import com.cho.board.post.entity.Post;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> searchPosts(PostSearchCondition condition, Pageable pageable) {
        List<Post> content = queryFactory
            .selectFrom(post)
            .leftJoin(post.author, user).fetchJoin()
            .leftJoin(post.category, category).fetchJoin()
            .where(
                titleContains(condition.getTitle()),
                contentContains(condition.getContent()),
                authorNameContains(condition.getAuthor()),
                categoryIdEq(condition.getCategoryId())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(post.createdAt.desc())
            .fetch();

        Long total = queryFactory
            .select(post.count())
            .from(post)
            .where(
                titleContains(condition.getTitle()),
                contentContains(condition.getContent()),
                authorNameContains(condition.getAuthor()),
                categoryIdEq(condition.getCategoryId())
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    // 동적 조건 메서드들
    private BooleanExpression titleContains(String title) {
        return hasText(title) ? post.title.contains(title) : null;
    }

    private BooleanExpression contentContains(String content) {
        return hasText(content) ? post.content.contains(content) : null;
    }

    private BooleanExpression authorNameContains(String author) {
        return hasText(author) ? post.author.name.contains(author) : null;
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? post.category.id.eq(categoryId) : null;
    }
}
