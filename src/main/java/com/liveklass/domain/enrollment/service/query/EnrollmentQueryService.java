package com.liveklass.domain.enrollment.service.query;

import com.liveklass.domain.enrollment.entity.Enrollment;

public interface EnrollmentQueryService {

	long countActive(Long courseId);

	boolean existsActive(Long studentId, Long courseId);

	Enrollment findWithStudentById(Long enrollmentId);
}
