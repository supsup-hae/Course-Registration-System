package com.liveklass.domain.enrollment.service.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;

public interface EnrollmentQueryService {

	long countActive(Long courseId);

	boolean existsActive(Long studentId, Long courseId);

	Enrollment findWithStudentById(Long enrollmentId);

	Enrollment findWithCourseAndStudentByIdForUpdate(Long enrollmentId);

	Page<Enrollment> findByStudentId(Long studentId, EnrollmentStatus status, Pageable pageable);

	Page<Enrollment> findByCourseId(Long courseId, EnrollmentStatus status, Pageable pageable);
}
