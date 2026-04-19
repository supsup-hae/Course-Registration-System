package com.liveklass.common.cache.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liveklass.common.cache.enums.CacheGroup;
import com.liveklass.common.cache.enums.CacheType;
import com.liveklass.common.cache.manager.CompositeCacheManager;
import com.liveklass.common.cache.manager.LocalCacheManager;

@Configuration
@EnableCaching
public class CacheConfiguration {

	private final RedisConnectionFactory redisConnectionFactory;

	public CacheConfiguration(RedisConnectionFactory redisConnectionFactory) {
		this.redisConnectionFactory = redisConnectionFactory;
	}

	@Bean
	public LocalCacheManager localCacheManager() {
		List<org.springframework.cache.Cache> caches = Arrays.stream(CacheGroup.values())
			.filter(it -> it.getCacheType() == CacheType.LOCAL || it.getCacheType() == CacheType.COMPOSITE)
			.map(this::toCaffeineCache)
			.collect(Collectors.toList());

		return new LocalCacheManager(caches);
	}

	private CaffeineCache toCaffeineCache(CacheGroup cacheGroup) {
		return new CaffeineCache(
			cacheGroup.getCacheName(),
			Caffeine.newBuilder()
				.expireAfterWrite(cacheGroup.getExpiredAfterWrite().getSeconds(), TimeUnit.SECONDS)
				.recordStats()
				.build()
		);
	}

	@Bean
	public RedisSerializer<Object> redisSerializer() {
		ObjectMapper objectMapper = JsonMapper.builder()
			.addModule(new JavaTimeModule()) // KotlinModule 등은 자바 환경이므로 생략하였습니다
			.activateDefaultTyping(
				BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
				ObjectMapper.DefaultTyping.EVERYTHING
			)
			.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.build();

		return new GenericJackson2JsonRedisSerializer(objectMapper);
	}

	@Bean
	public CacheManager redisCacheManager(RedisSerializer<Object> redisSerializer) {
		Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = Arrays.stream(CacheGroup.values())
			.filter(it -> it.getCacheType() == CacheType.GLOBAL || it.getCacheType() == CacheType.COMPOSITE)
			.collect(Collectors.toMap(
				CacheGroup::getCacheName,
				it -> RedisCacheConfiguration.defaultCacheConfig()
					.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
					.entryTtl(it.getExpiredAfterWrite())
			));

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheWriter(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
			.withInitialCacheConfigurations(redisCacheConfigurationMap)
			.enableStatistics()
			.build();
	}

	@Bean
	@Primary
	public CacheManager cacheManager(RedisSerializer<Object> redisSerializer) {
		LocalCacheManager localCacheManager = localCacheManager();
		localCacheManager.initializeCaches();

		return new CompositeCacheManager(
			Arrays.asList(localCacheManager, redisCacheManager(redisSerializer)),
			localCacheManager
		);
	}
}
