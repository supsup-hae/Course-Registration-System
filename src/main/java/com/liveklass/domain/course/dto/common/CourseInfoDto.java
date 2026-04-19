package com.liveklass.domain.course.dto.common;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.user.dto.common.UserInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "강의 상세 정보 DTO")
public record CourseInfoDto(
	@Schema(description = "등록된 강의 ID", example = "100")
	Long courseId,

	@Schema(description = "강의 생성자(크리에이터) 정보")
	UserInfo creator,

	@Schema(description = "강의 제목", example = "크리투스 전문코치 1:1 코칭 프로그램")
	String title,

	@Schema(description = "강의 설명", example = "크리에이터 성장 올인원 패키지 수강생을 위한 1:1 코칭입니다.")
	String description,

	@Schema(description = "강의 가격", example = "50000")
	BigDecimal price,

	@Schema(description = "수강 정원", example = "10")
	Integer capacity,

	@Schema(description = "강의 상태", example = "DRAFT")
	CourseStatus status,

	@Schema(description = "강의 시작 일시", example = "2026-05-01T10:00:00")
	LocalDateTime startDate,

	@Schema(description = "강의 종료 일시", example = "2026-05-01T12:00:00")
	LocalDateTime endDate,

	@Schema(description = "강의 생성 일시", example = "2026-04-19T10:00:00")
	LocalDateTime createdAt
) {
}

