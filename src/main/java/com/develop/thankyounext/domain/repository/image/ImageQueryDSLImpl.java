package com.develop.thankyounext.domain.repository.image;

import com.develop.thankyounext.domain.entity.QImage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ImageQueryDSLImpl implements ImageQueryDSL{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Long deleteAllByPostId(Long postId) {
        QImage image = QImage.image;

        return jpaQueryFactory
                .delete(image)
                .where(image.post.id.eq(postId))
                .execute();
    }

    @Override
    public Long deleteAllByGalleryId(Long galleryId) {
        QImage image = QImage.image;

        return jpaQueryFactory
                .delete(image)
                .where(image.gallery.id.eq(galleryId))
                .execute();
    }
}
