package com.liveklass.domain.course.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterCourseReqDto(

	@Schema(description = "강의 제목", example = "크리투스 전문코치 1:1 코칭 프로그램")
	@NotBlank(message = "강의 제목은 필수입니다.")
	@Size(min = 1, max = 100, message = "강의 제목은 1자 이상 100자 이하로 입력해주세요.")
	String title,

	@Schema(description = "강의 설명 (선택)", example = "크리에이터 성장 올인원 패키지 수강생을 위한 1:1 코칭입니다.")
	@Size(max = 2000, message = "강의 설명은 2000자 이내로 입력해주세요.")
	String description,

	@Schema(description = "강의 가격 (무료인 경우 0)", example = "50000")
	@NotNull(message = "가격은 필수입니다.")
	@Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
	BigDecimal price,

	@Schema(description = "수강 정원 (인원 제한을 지정하지 않는 경우 null)", example = "10")
	@Min(value = 1, message = "수강 정원은 1명 이상이어야 합니다.")
	Integer capacity,

	@Schema(description = "강의 시작 일시 (선택)", example = "2026-05-01T10:00:00")
	@FutureOrPresent(message = "강의 시작 일시는 현재 또는 미래 시간이어야 합니다.")
	LocalDateTime startDate,

	@Schema(description = "강의 종료 일시 (선택)", example = "2026-05-01T12:00:00")
	@FutureOrPresent(message = "강의 종료 일시는 현재 또는 미래 시간이어야 합니다.")
	LocalDateTime endDate
) {
}
