package com.liveklass.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.liveklass.common.constants.AuthConstants;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenApi() {
		SecurityScheme userIdScheme = new SecurityScheme()
			.type(SecurityScheme.Type.APIKEY)
			.in(SecurityScheme.In.HEADER)
			.name(AuthConstants.HEADER_USER_ID)
			.description("사용자 ID (예: 1)");

		SecurityScheme userRoleScheme = new SecurityScheme()
			.type(SecurityScheme.Type.APIKEY)
			.in(SecurityScheme.In.HEADER)
			.name(AuthConstants.HEADER_USER_ROLE)
			.description("사용자 역할 (예: CREATOR, STUDENT)");

		SecurityRequirement securityRequirement = new SecurityRequirement()
			.addList(AuthConstants.HEADER_USER_ID)
			.addList(AuthConstants.HEADER_USER_ROLE);

		return new OpenAPI()
			.info(new Info().title("A.수강 신청 시스템 API")
				.description("신윤섭 백엔드 채용 과제 - 수강 신청 시스템 API 서버")
				.version("v1.0"))
			.components(new Components()
				.addSecuritySchemes(AuthConstants.HEADER_USER_ID, userIdScheme)
				.addSecuritySchemes(AuthConstants.HEADER_USER_ROLE, userRoleScheme))
			.addSecurityItem(securityRequirement);
	}
}