package com.liveklass.domain.enrollment.dto.response;

import java.time.LocalDateTime;

import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.user.dto.common.UserCardInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "강의별 수강생 정보 DTO")
public record CourseEnrollmentInfo(

	@Schema(description = "수강신청 ID", example = "100")
	Long enrollmentId,

	@Schema(description = "수강생 정보")
	UserCardInfo student,

	@Schema(description = "수강신청 상태", example = "CONFIRMED")
	EnrollmentStatus status,

	@Schema(description = "수강신청 일시", example = "2026-05-01T10:00:00")
	LocalDateTime enrolledAt
) {
}
