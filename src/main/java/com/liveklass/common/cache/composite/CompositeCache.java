package com.liveklass.common.cache.composite;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.cache.Cache;

import com.liveklass.common.cache.manager.UpdatableCacheManager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompositeCache implements Cache {

	private final List<Cache> caches;
	private final UpdatableCacheManager updatableCacheManager;

	@Override
	public String getName() {
		return caches.getFirst().getName();
	}

	@Override
	public Object getNativeCache() {
		return caches.stream()
			.map(Cache::getNativeCache)
			.toList();
	}

	@Override
	public ValueWrapper get(Object key) {
		for (Cache cache : caches) {
			ValueWrapper valueWrapper = cache.get(key);
			if (valueWrapper != null && valueWrapper.get() != null) {
				updatableCacheManager.putIfAbsent(cache, key, valueWrapper.get());
				return valueWrapper;
			}
		}
		return null;
	}

	@Override
	public <T> T get(Object key, Class<T> type) {
		for (Cache cache : caches) {
			T value = cache.get(key, type);
			if (value != null) {
				updatableCacheManager.putIfAbsent(cache, key, value);
				return value;
			}
		}
		return null;
	}

	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		for (Cache cache : caches) {
			try {
				T value = cache.get(key, valueLoader);
				if (value != null) {
					updatableCacheManager.putIfAbsent(cache, key, value);
					return value;
				}
			} catch (ValueRetrievalException _) {
				// Continue to next cache if retrieval fails
			}
		}
		return null;
	}

	@Override
	public void put(Object key, Object value) {
		for (Cache cache : caches) {
			cache.put(key, value);
		}
	}

	@Override
	public void evict(Object key) {
		for (Cache cache : caches) {
			cache.evict(key);
		}
	}

	@Override
	public void clear() {
		for (Cache cache : caches) {
			cache.clear();
		}
	}
}

