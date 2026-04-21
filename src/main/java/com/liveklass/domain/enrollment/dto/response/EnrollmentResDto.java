package com.liveklass.domain.enrollment.dto.response;

import java.time.LocalDateTime;

import com.liveklass.domain.enrollment.enums.EnrollmentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "수강신청 응답 DTO")
public record EnrollmentResDto(

	@Schema(description = "수강신청 ID", example = "100")
	Long enrollmentId,

	@Schema(description = "신청한 강의 ID", example = "1")
	Long courseId,

	@Schema(description = "수강생 ID", example = "10")
	Long studentId,

	@Schema(description = "수강신청 상태 (초기 PENDING)", example = "PENDING")
	EnrollmentStatus status,

	@Schema(description = "PENDING 상태 만료 일시 (기본 15분)", example = "2026-05-01T10:30:00")
	LocalDateTime expiresAt
) {
}
