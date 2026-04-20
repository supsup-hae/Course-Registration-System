package com.liveklass.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveklass.common.cache.serializer.GzipRedisSerializer;
import com.liveklass.common.config.properties.RedisProperties;
import com.liveklass.domain.course.entity.Course;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@RequiredArgsConstructor
public class RedisConfig {

	private final RedisProperties redisProperties;
	private final ObjectMapper objectMapper;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
			redisProperties.host(),
			redisProperties.port()
		);
		config.setPassword(redisProperties.password());

		return new LettuceConnectionFactory(config);
	}

	@Bean
	public RedisTemplate<String, String> stringRedisTemplate() {
		return createGzipJsonRedisTemplate(redisConnectionFactory(), objectMapper, new TypeReference<>() {
		});
	}

	@Bean
	public RedisTemplate<String, Course> courseRedisTemplate() {
		return createGzipJsonRedisTemplate(redisConnectionFactory(), objectMapper, new TypeReference<>() {
		});
	}

	private <V> RedisTemplate<String, V> createGzipJsonRedisTemplate(
		RedisConnectionFactory connectionFactory,
		ObjectMapper objectMapper,
		TypeReference<V> typeRef
	) {
		RedisTemplate<String, V> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GzipRedisSerializer<>(objectMapper, typeRef));
		return redisTemplate;
	}
}