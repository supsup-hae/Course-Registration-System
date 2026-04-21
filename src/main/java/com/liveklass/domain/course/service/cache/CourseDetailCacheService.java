package com.liveklass.domain.course.service.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.liveklass.domain.course.converter.CourseConverter;
import com.liveklass.domain.course.dto.common.CourseInfoDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.service.query.CourseQueryService;
import com.liveklass.domain.user.converter.UserConverter;
import com.liveklass.domain.user.dto.common.UserInfoDto;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseDetailCacheService {

	private final CourseQueryService courseQueryService;

	@Cacheable(cacheNames = "course:detail", key = "#courseId")
	public CourseInfoDto load(final Long courseId) {
		Course course = courseQueryService.findByIdWithCreator(courseId);
		UserInfoDto creatorInfo = UserConverter.toUserInfo(course.getCreator());
		return CourseConverter.toCourseInfoDto(course, creatorInfo);
	}

	@CacheEvict(cacheNames = "course:detail", key = "#courseId")
	public void evict(final Long courseId) {
	}
}
