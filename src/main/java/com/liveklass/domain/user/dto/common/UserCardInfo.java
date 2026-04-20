package com.liveklass.domain.user.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사용자 카드 DTO")
public record UserCardInfo(
	@Schema(description = "사용자 ID", example = "1")
	Long userId,

	@Schema(description = "사용자 이름", example = "홍길동")
	String name
) {
}