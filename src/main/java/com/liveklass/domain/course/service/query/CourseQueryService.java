package com.liveklass.domain.course.service.query;

import org.springframework.data.domain.Page;

import com.liveklass.domain.course.dto.request.FindCoursesReqDto;
import com.liveklass.domain.course.entity.Course;

public interface CourseQueryService {
	Course findById(Long courseId);

	Course findByIdWithCreator(Long courseId);

	Page<Course> findAllWithFilters(FindCoursesReqDto reqDto);
}
