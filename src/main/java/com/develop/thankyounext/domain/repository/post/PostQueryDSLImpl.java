package com.develop.thankyounext.domain.repository.post;

import com.develop.thankyounext.domain.entity.*;
import com.develop.thankyounext.domain.entity.mapping.QPostTag;
import com.develop.thankyounext.domain.enums.PostEnum;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostQueryDSLImpl implements PostQueryDSL {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable) {
        QPost post = QPost.post;

        List<Post> contents = jpaQueryFactory
                .selectFrom(post)
                .where(post.member.id.eq(memberId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = createCountQuery(post.member.id.eq(memberId), post);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Post> findAllByConditions(PostEnum dType, List<Long> tagList, String keyword, Pageable pageable) {
        QPost post = QPost.post;
        QPostTag postTag = QPostTag.postTag;

        BooleanExpression conditions = post.dType.eq(dType);

        if (tagList != null && !tagList.isEmpty()) {
            BooleanExpression tagCondition = postTag.tag.id.in(tagList);
            conditions = conditions.and(post.id.in(
                    JPAExpressions.select(postTag.post.id)
                            .from(postTag)
                            .where(tagCondition)
            ));
        }

        if (keyword != null && !keyword.isEmpty()) {
            conditions = conditions.and(post.title.contains(keyword));
        }

        List<Post> contents = jpaQueryFactory
                .selectFrom(post)
                .where(conditions)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = createCountQuery(conditions, post);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<Post> findByIdWithCommentAndMember(Long postId) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QMember member = QMember.member;
        QImage image = QImage.image;

        Post findPost = jpaQueryFactory
                .selectFrom(post)
                .where(post.id.eq(postId))
                .leftJoin(post.commentList, comment).fetchJoin()
                .leftJoin(comment.member, member).fetchJoin()
                .fetchOne();

        return Optional.ofNullable(findPost);
    }

    @Override
    public Optional<Post> findByIdWithMember(Long postId) {
        QPost post = QPost.post;
        QMember member = QMember.member;

        Post findPost = jpaQueryFactory
                .selectFrom(post)
                .where(post.id.eq(postId))
                .leftJoin(post.member, member).fetchJoin()
                .fetchOne();

        return Optional.ofNullable(findPost);
    }

    @Override
    public Long deleteAllById(Long postId) {
        QPost post = QPost.post;

        return jpaQueryFactory
                .delete(post)
                .where(post.id.eq(postId))
                .execute();
    }

    private JPAQuery<Long> createCountQuery(BooleanExpression condition, QPost post) {
        return jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(condition);
    }
}
