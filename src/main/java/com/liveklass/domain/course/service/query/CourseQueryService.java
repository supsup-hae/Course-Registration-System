package com.liveklass.domain.course.service.query;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;

import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.enums.CourseStatus;

public interface CourseQueryService {
	Course findById(Long courseId);

	Course findByIdForUpdate(Long courseId);

	Course findByIdWithCreator(Long courseId);

	Page<Course> findAllWithFilters(
		int page,
		int size,
		CourseStatus status,
		BigDecimal minPrice,
		BigDecimal maxPrice,
		Boolean hasCapacity
	);

	List<Course> findOpenCoursesWithCapacity();
}
