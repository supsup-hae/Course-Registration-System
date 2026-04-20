package com.liveklass.domain.course.dto.common;

import java.math.BigDecimal;

import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.user.dto.common.UserCardInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "강의 목록 카드 DTO")
public record CourseCardInfo(
	@Schema(description = "강의 ID", example = "100")
	Long courseId,

	@Schema(description = "크리에이터 정보")
	UserCardInfo creator,

	@Schema(description = "강의 제목", example = "크리투스 전문코치 1:1 코칭 프로그램")
	String title,

	@Schema(description = "강의 가격", example = "50000")
	BigDecimal price,

	@Schema(description = "강의 상태", example = "OPEN")
	CourseStatus status
) {
}
