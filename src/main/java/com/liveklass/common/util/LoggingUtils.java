package com.liveklass.common.util;

import java.util.stream.Collectors;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class LoggingUtils {

	public void logException(final String prefix, final Exception ex, final HttpServletRequest request) {
		log.error("{}: {} | 예외 발생 지점 [{} {}]",
			prefix,
			ex.getMessage(),
			request.getMethod(),
			request.getRequestURI(),
			ex);
	}

	public void logValidationException(final MethodArgumentNotValidException ex, final HttpServletRequest request) {
		String errorFields = ex.getBindingResult().getFieldErrors().stream()
			.map(LoggingUtils::formatFieldError)
			.collect(Collectors.joining(", "));

		log.error("유효성 검사 실패 | 예외 발생 지점 [{} {}] | 실패 필드: {}",
			request.getMethod(),
			request.getRequestURI(),
			errorFields,
			ex);
	}

	private String formatFieldError(final FieldError error) {
		return String.format("[field: %s, message: %s]", error.getField(), error.getDefaultMessage());
	}
}