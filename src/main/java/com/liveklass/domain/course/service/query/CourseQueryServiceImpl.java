package com.liveklass.domain.course.service.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.course.dto.request.FindCoursesReqDto;
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

	@Override
	public Course findByIdWithCreator(final Long courseId) {
		Course course = courseRepository.findByIdWithCreator(courseId)
			.orElseThrow(() -> new CourseException(ErrorCode.NOT_FOUND));
		log.info("[Course] 강의 조회(fetch join) : id = {}", courseId);
		return course;
	}

	@Override
	public Page<Course> findAllWithFilters(final FindCoursesReqDto reqDto) {
		log.info("[Course] 강의 목록 필터 조회 : status={}, minPrice={}, maxPrice={}, hasCapacity={}, page={}, size={}",
			reqDto.getStatus(), reqDto.getMinPrice(), reqDto.getMaxPrice(),
			reqDto.getHasCapacity(), reqDto.getPage(), reqDto.getSize());

		PageRequest pageable = PageRequest.of(reqDto.getPage(), reqDto.getSize());
		return courseRepository.findAllWithFilters(
			reqDto.getStatus(),
			reqDto.getMinPrice(),
			reqDto.getMaxPrice(),
			reqDto.getHasCapacity(),
			pageable
		);
	}
}
