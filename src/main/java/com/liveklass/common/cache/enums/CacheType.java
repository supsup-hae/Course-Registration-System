package com.liveklass.common.cache.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CacheType {
	LOCAL("로컬 캐시만 적용"),
	GLOBAL("분산 캐시만 적용"),
	COMPOSITE("로컬 캐시와 분산 캐시를 모두 적용");

	private final String description;

}
