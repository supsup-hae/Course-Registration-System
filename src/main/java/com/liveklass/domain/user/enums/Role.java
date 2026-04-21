package com.liveklass.domain.user.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Role {
	STUDENT("클래스메이트"),
	CREATOR("크리에이터");

	private final String description;
}
