package com.liveklass.common.cache.manager;

import org.springframework.cache.Cache;

public interface UpdatableCacheManager {
	void putIfAbsent(Cache cache, Object key, Object value);
}
