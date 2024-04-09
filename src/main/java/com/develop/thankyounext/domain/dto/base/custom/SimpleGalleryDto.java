package com.develop.thankyounext.domain.dto.base.custom;

import com.develop.thankyounext.domain.dto.base.common.AuditingDto;
import com.develop.thankyounext.domain.dto.base.entity.ImageDto;
import com.develop.thankyounext.domain.entity.embedded.GalleryImageList;
import lombok.Builder;

import java.util.List;

@Builder
public record SimpleGalleryDto(
        Long id,
        String title,
//        GalleryImageList imageUrl,
        List<ImageDto> imageUrl,
        AuditingDto auditingDto
) {
}
