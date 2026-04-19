package com.liveklass.domain.course.service.query;

import org.springframework.stereotype.Service;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.exception.CourseException;
import com.liveklass.domain.course.repository.CourseRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseQueryServiceImpl implements CourseQueryService {

	private final CourseRepository courseRepository;

	@Override
	public Course findById(final Long courseId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new CourseException(ErrorCode.NOT_FOUND));
		log.info("[Course] 강의 조회 : id = {}", courseId);
		return course;
	}

	public Course findByIdWithCreator(final Long courseId) {
		Course course = courseRepository.findByIdWithCreator(courseId)
			.orElseThrow(() -> new CourseException(ErrorCode.NOT_FOUND));
		log.info("[Course] 강의 조회(fetch join) : id = {}", courseId);
		return course;
	}
}
