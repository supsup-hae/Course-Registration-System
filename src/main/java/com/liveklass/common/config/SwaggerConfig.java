package com.liveklass.common.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenApi() {

		return new OpenAPI()
			.info(new Info().title("A.수강 신청 시스템 API")
				.description("신윤섭 백엔드 채용 과제 - 수강 신청 시스템 API 서버")
				.version("v1.0"));
	}

	@Bean
	@Primary
	public SwaggerUiConfigProperties swaggerUiConfigProperties(SwaggerUiConfigProperties props) {
		props.setPersistAuthorization(true);
		return props;
	}
}