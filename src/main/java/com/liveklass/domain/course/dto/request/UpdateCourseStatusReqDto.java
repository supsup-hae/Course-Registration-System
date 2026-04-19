package com.liveklass.domain.course.dto.request;

import java.time.LocalDateTime;

import com.liveklass.domain.course.enums.CourseStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateCourseStatusReqDto(
	@Schema(description = "변경할 강의 상태(OPEN, CLOSED)", example = "OPEN")
	@NotNull(message = "변경할 상태는 필수입니다.")
	CourseStatus status,

	@Schema(description = "모집 시작일 (OPEN 전환 시 필수)", example = "2026-05-01T00:00:00")
	LocalDateTime startDate,

	@Schema(description = "모집 종료일 (기한을 지정하지 않는 경우 null)", example = "2026-06-01T00:00:00")
	LocalDateTime endDate
) {
}
