package com.liveklass.domain.enrollment.service.concurrency;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EnrollmentRedisCounter implements EnrollmentSlotCounter {

	private static final String KEY_PREFIX = "enrollment:course:";
	private static final String KEY_SUFFIX = ":active_count";

	private static final String INCR_LUA = """
		local current = tonumber(redis.call('GET', KEYS[1]) or '0')
		local capacity = tonumber(ARGV[1])
		if current >= capacity then
		return 0
		end
		redis.call('INCR', KEYS[1])
		return 1
		""";

	private static final String DECR_LUA = """
		local current = tonumber(redis.call('GET', KEYS[1]) or '0')
		if current <= 0 then
		return 0
		end
		return redis.call('DECR', KEYS[1])
		""";

	private final StringRedisTemplate redisTemplate;
	private final RedisScript<Long> incrScript;
	private final RedisScript<Long> decrScript;

	public EnrollmentRedisCounter(final StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.incrScript = new DefaultRedisScript<>(INCR_LUA, Long.class);
		this.decrScript = new DefaultRedisScript<>(DECR_LUA, Long.class);
	}

	@Override
	public boolean tryIncrement(@NotNull final Long courseId, final int capacity) {
		Long result = redisTemplate.execute(incrScript, List.of(key(courseId)), String.valueOf(capacity));
		return result != null && result == 1L;
	}

	@Override
	public void decrement(@NotNull final Long courseId) {
		try {
			redisTemplate.execute(decrScript, List.of(key(courseId)));
		} catch (Exception e) {
			log.error("[Enrollment] Redis 카운터 감소 실패, 스케줄러 보정 필요 : courseId = {}", courseId, e);
		}
	}

	@Override
	public long get(@NotNull final Long courseId) {
		String value = redisTemplate.opsForValue().get(key(courseId));
		if (value == null) {
			return 0L;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			log.warn("[Enrollment] Redis 카운터 값 파싱 실패 : courseId = {}, value = {}", courseId, value, e);
			return 0L;
		}
	}

	@Override
	public void set(@NotNull final Long courseId, final long value) {
		redisTemplate.opsForValue().set(key(courseId), String.valueOf(value));
	}

	private String key(final Long courseId) {
		return KEY_PREFIX + courseId + KEY_SUFFIX;
	}
}
