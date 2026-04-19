package com.liveklass.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisProperties(
	@NotBlank(message = "Redis Host 정보는 필수입니다.")
	String host,

	@NotNull(message = "Redis Port 정보는 필수입니다.")
	Integer port,

	@NotBlank(message = "Redis Password 정보는 필수입니다.")
	String password
) {
}