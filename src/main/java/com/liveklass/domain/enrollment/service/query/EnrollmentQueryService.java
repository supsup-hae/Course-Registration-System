package com.liveklass.domain.enrollment.service.query;

public interface EnrollmentQueryService {

	long countActive(Long courseId);

	boolean existsActive(Long studentId, Long courseId);
}
