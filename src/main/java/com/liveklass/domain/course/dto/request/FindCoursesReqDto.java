package com.liveklass.domain.course.dto.request;

import java.math.BigDecimal;

import com.liveklass.domain.course.enums.CourseStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FindCoursesReqDto {

	@Schema(description = "페이지 번호 (0부터 시작)", defaultValue = "0", example = "0")
	@Min(0)
	private int page = 0;

	@Schema(description = "페이지 크기", defaultValue = "10", example = "10")
	@Min(1)
	private int size = 10;

	@Schema(description = "강의 상태 필터링 (DRAFT: 초안, OPEN: 모집 중, CLOSED: 모집 마감)", example = "OPEN", allowableValues = {"DRAFT", "OPEN", "CLOSED"})
	private CourseStatus status;

	@Schema(description = "최소 수강료 (0 이상)", example = "10000")
	@PositiveOrZero
	private BigDecimal minPrice;

	@Schema(description = "최대 수강료 (0 이상)", example = "50000")
	@PositiveOrZero
	private BigDecimal maxPrice;

	@Schema(description = "정원 제한 여부 필터링 (true: 정원 제한 있는 강의, false/null: 전체)", example = "true", allowableValues = {"true", "false"})
	private Boolean hasCapacity;
}

