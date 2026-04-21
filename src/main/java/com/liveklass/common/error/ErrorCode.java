package com.liveklass.common.error;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

	/**
	 * Common Error (C-xxx)
	 */
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "C-001", "잘못된 요청입니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "C-002", "리소스를 찾을 수 없습니다."),
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "C-003", "유효하지 않은 입력값입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-004", "서버 오류가 발생했습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C-005", "인증이 필요합니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "C-006", "요청한 리소스에 접근할 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C-007", "지원하지 않는 HTTP 메서드입니다."),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "C-008", "지원하지 않는 미디어 타입입니다."),
	DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "C-009", "데이터 무결성 위반입니다."),
	OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "C-010", "동시 요청으로 인한 충돌이 발생했습니다. 다시 시도해 주세요."),
	SERIALIZE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-011", "데이터 직렬화 중 오류가 발생했습니다."),
	DESERIALIZE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-012", "데이터 역직렬화 중 오류가 발생했습니다."),
	GZIP_COMPRESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-013", "데이터 압축 중 오류가 발생했습니다."),
	GZIP_DECOMPRESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-014", "데이터 압축 해제 중 오류가 발생했습니다."),

	/**
	 * User Error (U-xxx)
	 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U-001", "사용자를 찾을 수 없습니다."),

	/**
	 * Course Error (CO-xxx)
	 */
	INVALID_COURSE_DATE_RANGE(HttpStatus.BAD_REQUEST, "CO-001", "종료일은 시작일보다 이후여야 합니다."),
	INVALID_COURSE_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "CO-002", "허용되지 않는 상태 전환입니다."),
	OPEN_REQUIRES_START_DATE(HttpStatus.BAD_REQUEST, "CO-003", "모집 시작일은 필수입니다."),

	/**
	 * Enrollment Error (E-xxx)
	 */
	ENROLLMENT_CAPACITY_FULL(HttpStatus.CONFLICT, "E-001", "강의 정원이 모두 찼습니다"),
	ENROLLMENT_INVALID_STATE(HttpStatus.CONFLICT, "E-002", "수강신청 상태가 올바르지 않습니다"),
	ENROLLMENT_DUPLICATE(HttpStatus.CONFLICT, "E-003", "이미 진행 중인 수강신청이 있습니다");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
