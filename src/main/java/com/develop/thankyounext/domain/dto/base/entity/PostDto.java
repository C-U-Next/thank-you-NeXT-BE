package com.develop.thankyounext.domain.dto.base.entity;

import com.develop.thankyounext.domain.dto.base.common.AuditingDto;
import com.develop.thankyounext.domain.enums.PostEnum;
import com.develop.thankyounext.domain.enums.SolvedEnum;
import lombok.Builder;

import java.util.List;

@Builder
public record PostDto(
        Long id,
        String title,
        String content,
        PostEnum dType,
        SolvedEnum isSolved,
        List<ImageDto> imageDtoList,
        AuditingDto auditingDto
) {
}
