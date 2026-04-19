package com.liveklass.domain.course.dto.response;

import java.time.LocalDateTime;

import com.liveklass.domain.course.enums.CourseStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UpdateCourseStatusResDto(
	@Schema(description = "수정된 강의 ID", example = "1")
	Long courseId,

	@Schema(description = "변경 적용된 강의 상태", example = "OPEN")
	CourseStatus status,

	@Schema(description = "모집 시작일 (기한을 지정하지 않는 경우 null)", example = "2026-05-01T00:00:00")
	LocalDateTime startDate,

	@Schema(description = "모집 종료일 (기한을 지정하지 않는 경우 null)", example = "2026-06-01T00:00:00")
	LocalDateTime endDate
) {
}
