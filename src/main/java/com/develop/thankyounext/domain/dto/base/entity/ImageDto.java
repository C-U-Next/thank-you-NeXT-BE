package com.develop.thankyounext.domain.dto.base.entity;

import lombok.Builder;

@Builder
public record ImageDto(
    Long id,
    String url
) {
}
