package com.liveklass.common.cache.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.liveklass.common.cache.composite.CompositeCache;
import com.liveklass.common.cache.enums.CacheGroup;

public class CompositeCacheManager implements CacheManager {

	private final List<CacheManager> cacheManagers;
	private final UpdatableCacheManager updatableCacheManager;
	private final List<String> cacheNames;


	public CompositeCacheManager(List<CacheManager> cacheManagers, UpdatableCacheManager updatableCacheManager) {
		this.cacheManagers = cacheManagers;
		this.updatableCacheManager = updatableCacheManager;
		this.cacheNames = new ArrayList<>();
		for (CacheManager manager : cacheManagers) {
			this.cacheNames.addAll(manager.getCacheNames());
		}
	}

	@Override
	public @Nullable Cache getCache(final String name) {
		if (CacheGroup.isCompositeType(name)) {
			List<Cache> caches = cacheManagers.stream()
				.map(manager -> manager.getCache(name))
				.filter(Objects::nonNull)
				.toList();
			return new CompositeCache(caches, updatableCacheManager);
		}

		return cacheManagers.stream()
			.map(manager -> manager.getCache(name))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	@Override
	public Collection<String> getCacheNames() {
		return new ArrayList<>(cacheNames);
	}

	@Override
	public void resetCaches() {
		CacheManager.super.resetCaches();
	}
}
