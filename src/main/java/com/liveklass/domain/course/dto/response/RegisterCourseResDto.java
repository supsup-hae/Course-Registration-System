package com.liveklass.domain.course.dto.response;

import com.liveklass.domain.course.enums.CourseStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "강의 등록 응답 DTO")
public record RegisterCourseResDto(
	@Schema(description = "등록된 강의 ID", example = "100")
	Long courseId,

	@Schema(description = "강의 상태", example = "DRAFT")
	CourseStatus status
) {
}
