package com.liveklass.common.cache.enums;

import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CacheGroup {
	LOCAL_ONLY(
		CacheName.LOCAL_ONLY,
		Duration.ofMinutes(10),
		CacheType.LOCAL
	),

	GLOBAL_ONLY(
		CacheName.GLOBAL_ONLY,
		Duration.ofMinutes(10),
		CacheType.GLOBAL
	),

	COMPOSITE_ALL(
		CacheName.COMPOSITE,
		Duration.ofMinutes(10),
		CacheType.COMPOSITE
	),

	COURSE_DETAIL(
		CacheName.COURSE_DETAIL,
		Duration.ofMinutes(10),
		CacheType.COMPOSITE
	);

	private final String cacheName;
	private final Duration expiredAfterWrite;
	private final CacheType cacheType;

	private static final Map<String, CacheGroup> CACHE_MAP = Stream.of(values())
		.collect(Collectors.toMap(CacheGroup::getCacheName, Function.identity()));

	public static boolean isCompositeType(String cacheName) {
		return get(cacheName).getCacheType() == CacheType.COMPOSITE;
	}

	private static CacheGroup get(String cacheName) {
		CacheGroup cacheGroup = CACHE_MAP.get(cacheName);
		if (cacheGroup == null) {
			throw new NoSuchElementException(cacheName + " Cache Name Not Found");
		}
		return cacheGroup;
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class CacheName {
		public static final String LOCAL_ONLY = "local";
		public static final String GLOBAL_ONLY = "global";
		public static final String COMPOSITE = "composite";
		public static final String COURSE_DETAIL = "course:detail";
	}
}
