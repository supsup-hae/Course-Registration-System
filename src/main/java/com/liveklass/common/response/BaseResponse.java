package com.liveklass.common.response;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record BaseResponse<T>(
	@JsonIgnore
	HttpStatus httpStatus,
	boolean success,
	T data,
	ErrorResponse error,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN, timezone = TIMEZONE_SEOUL)
	ZonedDateTime timestamp
) {

	static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	static final String TIMEZONE_SEOUL = "Asia/Seoul";

	private static ZonedDateTime now() {
		return ZonedDateTime.now(ZoneId.of(TIMEZONE_SEOUL));
	}

	public static <T> BaseResponse<T> ok(final T data) {
		return BaseResponse.<T>builder()
			.httpStatus(HttpStatus.OK)
			.success(true)
			.data(data)
			.error(null)
			.timestamp(now())
			.build();
	}

	public static <T> BaseResponse<T> created(final T data) {
		return BaseResponse.<T>builder()
			.httpStatus(HttpStatus.CREATED)
			.success(true)
			.data(data)
			.error(null)
			.timestamp(now())
			.build();
	}

	public static <T> BaseResponse<T> accepted() {
		return BaseResponse.<T>builder()
			.httpStatus(HttpStatus.ACCEPTED)
			.success(true)
			.data(null)
			.error(null)
			.timestamp(now())
			.build();
	}

	public static <T> BaseResponse<T> fail(@NonNull ErrorResponse error) {
		return BaseResponse.<T>builder()
			.httpStatus(error.status())
			.success(false)
			.data(null)
			.error(error)
			.timestamp(now())
			.build();
	}
}
