package com.liveklass.domain.course.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CourseStatus {
	DRAFT("초안"),
	OPEN("모집 중"),
	CLOSED("모집 마감");

	private final String description;
}
