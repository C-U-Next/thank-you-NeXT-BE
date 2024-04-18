package com.develop.thankyounext.domain.dto.base.entity;

import com.develop.thankyounext.domain.dto.base.common.AuditingDto;
import com.develop.thankyounext.domain.entity.embedded.GalleryImageList;
import lombok.Builder;

import java.util.List;

@Builder
public record GalleryDto(
        Long id,
        String title,
        List<ImageDto> imageDtoList,
        AuditingDto auditingDto
) {
}
