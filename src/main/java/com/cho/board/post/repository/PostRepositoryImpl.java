package com.cho.board.post.repository;

import static com.cho.board.category.entity.QCategory.category;
import static com.cho.board.post.entity.QPost.post;
import static com.cho.board.user.entity.QUser.user;
import static org.springframework.util.StringUtils.hasText;

import com.cho.board.post.dtos.PostSearchCondition;
import com.cho.board.post.entity.Post;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


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


    @Override
    public Page<Post> searchPostsWithFilters(String keyword, Long categoryId, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword))
                    .or(post.author.name.containsIgnoreCase(keyword))
            );
        }

        if (categoryId != null) {
            builder.and(post.category.id.eq(categoryId));
        }

        List<Post> content = queryFactory
            .selectFrom(post)
            .leftJoin(post.author).fetchJoin()
            .leftJoin(post.category).fetchJoin()
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(getOrderSpecifiers(pageable.getSort()))
            .fetch();

        Long total = queryFactory
            .select(post.count())
            .from(post)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (sort.isEmpty()) {
            // 기본 정렬 : 최신순
            orders.add(post.createdAt.desc());
        } else {
            sort.forEach(order -> {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();

                switch (property) {
                    case "createdAt":
                        orders.add(new OrderSpecifier<>(direction, post.createdAt));
                        break;
                    case "viewCount":
                        orders.add(new OrderSpecifier<>(direction, post.viewCount));
                        break;
                    case "title":
                        orders.add(new OrderSpecifier<>(direction, post.title));
                        break;
                    default:
                        orders.add(post.createdAt.desc());
                }
            });
        }
        return orders.toArray(new OrderSpecifier[0]);
    }

}
