package com.liveklass.domain.enrollment.dto.common;

import com.liveklass.domain.course.dto.common.CourseCardInfo;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "내 수강신청 목록 카드 DTO")
public record EnrollmentCardInfo(
	@Schema(description = "수강신청 ID", example = "999")
	Long enrollmentId,

	@Schema(description = "강의 정보")
	CourseCardInfo course,

	@Schema(description = "수강신청 상태", example = "CONFIRMED")
	EnrollmentStatus status
) {
}
