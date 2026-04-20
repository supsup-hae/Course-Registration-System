package com.liveklass.common.cache.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import jakarta.annotation.PostConstruct;

public class LocalCacheManager implements CacheManager, UpdatableCacheManager {

	private final List<Cache> caches;
	private Map<String, Cache> cacheMap = Collections.emptyMap();
	private volatile Set<String> cacheNames = Collections.emptySet();

	public LocalCacheManager(List<Cache> caches) {
		this.caches = caches != null ? caches : Collections.emptyList();
	}

	@PostConstruct
	public void initializeCaches() {
		Set<String> names = LinkedHashSet.newLinkedHashSet(caches.size());
		Map<String, Cache> map = new ConcurrentHashMap<>(16);
		for (Cache cache : caches) {
			String name = cache.getName();
			map.put(name, cache);
			names.add(name);
		}
		this.cacheNames = Collections.unmodifiableSet(names);
		this.cacheMap = Collections.unmodifiableMap(map);
	}

	@Override
	public @Nullable Cache getCache(String name) {
		return cacheMap.get(name);
	}

	@Override
	public Collection<String> getCacheNames() {
		return cacheNames;
	}

	@Override
	public void putIfAbsent(Cache cache, Object key, Object value) {
		Cache localCache = getCache(cache.getName());
		if (localCache != null) {
			localCache.putIfAbsent(key, value);
		}
	}
}
