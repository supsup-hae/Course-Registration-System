package com.liveklass.domain.course.service.query;

import com.liveklass.domain.course.entity.Course;

public interface CourseQueryService {
	Course findById(Long courseId);

	Course findByIdWithCreator(Long courseId);
}
