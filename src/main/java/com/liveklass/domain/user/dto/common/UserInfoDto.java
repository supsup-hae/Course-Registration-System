package com.liveklass.domain.user.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사용자 정보 공통 DTO")
public record UserInfoDto(
	@Schema(description = "사용자 ID", example = "1")
	Long userId,

	@Schema(description = "사용자 이름", example = "릴스해커")
	String name,

	@Schema(description = "이메일", example = "creator@liveklass.com")
	String email,

	@Schema(description = "역할 (STUDENT, CREATOR)", example = "CREATOR")
	String role
) {
}
