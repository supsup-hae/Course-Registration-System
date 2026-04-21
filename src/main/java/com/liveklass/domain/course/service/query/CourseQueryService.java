package com.liveklass.domain.course.service.query;

import java.util.List;

import org.springframework.data.domain.Page;

import com.liveklass.domain.course.dto.request.FindCoursesReqDto;
import com.liveklass.domain.course.entity.Course;

public interface CourseQueryService {
	Course findById(Long courseId);

	Course findByIdForUpdate(Long courseId);

	Course findByIdWithCreator(Long courseId);

	Page<Course> findAllWithFilters(FindCoursesReqDto reqDto);

	List<Course> findOpenCoursesWithCapacity();
}
