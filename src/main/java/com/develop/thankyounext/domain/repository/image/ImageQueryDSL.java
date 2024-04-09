package com.develop.thankyounext.domain.repository.image;

public interface ImageQueryDSL {
    Long deleteAllByPostId(Long postId);

    Long deleteAllByGalleryId(Long galleryId);
}
