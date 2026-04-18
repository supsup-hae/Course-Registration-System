package com.liveklass.common.response;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import com.liveklass.common.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record ErrorResponse(
	HttpStatus status,
	String message,
	String method,
	String requestUri,
	List<FieldErrorDetail> errors
) {

	public static ErrorResponse of(
		@NonNull ErrorCode errorCode,
		@NonNull HttpServletRequest request
	) {
		return ErrorResponse.builder()
			.status(errorCode.getHttpStatus())
			.message(errorCode.getMessage())
			.method(request.getMethod())
			.requestUri(request.getRequestURI())
			.errors(new ArrayList<>())
			.build();
	}

	public static ErrorResponse of(
		@NonNull ErrorCode errorCode,
		@NonNull HttpServletRequest request,
		@NonNull String errorMessage
	) {
		return ErrorResponse.builder()
			.status(errorCode.getHttpStatus())
			.message(errorMessage)
			.method(request.getMethod())
			.requestUri(request.getRequestURI())
			.errors(new ArrayList<>())
			.build();
	}

	public ErrorResponse withValidationErrors(@NonNull BindingResult bindingResult) {
		List<FieldErrorDetail> details = new ArrayList<>(this.errors);
		bindingResult.getFieldErrors().forEach(fe -> details.add(
			FieldErrorDetail.builder()
				.field(fe.getField())
				.message(fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "")
				.build()
		));
		bindingResult.getGlobalErrors().forEach(oe -> details.add(
			FieldErrorDetail.builder()
				.field(oe.getObjectName())
				.message(oe.getDefaultMessage() != null ? oe.getDefaultMessage() : "")
				.build()
		));
		return ErrorResponse.builder()
			.status(this.status)
			.message(this.message)
			.method(this.method)
			.requestUri(this.requestUri)
			.errors(details)
			.build();
	}

	@Builder
	public record FieldErrorDetail(String field, String message) {
	}
}
