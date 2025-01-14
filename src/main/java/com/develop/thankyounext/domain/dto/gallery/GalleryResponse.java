package com.develop.thankyounext.domain.dto.gallery;

import com.develop.thankyounext.domain.dto.base.entity.CommentDto;
import com.develop.thankyounext.domain.dto.base.custom.SimpleGalleryDto;
import com.develop.thankyounext.domain.dto.base.common.PageDto;
import com.develop.thankyounext.domain.dto.base.entity.GalleryDto;
import lombok.Builder;

import java.util.List;

public class GalleryResponse {

    @Builder
    public record GetGalleryList(
            List<SimpleGalleryDto> simpleGalleryDtoList,
            PageDto pageDto
    ) {
    }

    @Builder
    public record GetGallery(
            GalleryDto galleryDto,
            List<CommentDto> commentDtoList
    ) {
    }
}
