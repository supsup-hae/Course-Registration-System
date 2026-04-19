package com.liveklass.domain.enrollment.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EnrollmentStatus {
	PENDING("결제 대기"),
	CONFIRMED("수강 확정"),
	CANCELLED("취소됨"),
	WAITLISTED("대기 중");

	private final String description;
}
