package com.liveklass.domain.enrollment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateEnrollmentReqDto(
	@Schema(description = "수강 신청할 강의 ID", example = "1")
	@NotNull(message = "강의 ID는 필수입니다.")
	@Positive(message = "강의 ID는 양수여야 합니다.")
	Long courseId
) {
}
