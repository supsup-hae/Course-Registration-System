package com.liveklass.domain.enrollment.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.enrollment.EnrollmentException;
import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.repository.EnrollmentRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentQueryServiceImpl implements EnrollmentQueryService {

	private final EnrollmentRepository enrollmentRepository;

	@Override
	public long countActive(final Long courseId) {
		long count = enrollmentRepository.countActiveByCourseId(courseId);
		log.info("[Enrollment] 활성 수강신청 수 조회 : courseId = {}, count = {}", courseId, count);
		return count;
	}

	@Override
	@Transactional
	public boolean existsActive(final Long studentId, final Long courseId) {
		boolean exists = enrollmentRepository.existsActiveEnrollment(studentId, courseId);
		log.info("[Enrollment] 기존 활성 수강신청 존재 여부 조회 : studentId = {}, courseId = {}, exists = {}",
			studentId, courseId, exists);
		return exists;
	}

	@Override
	public Enrollment findWithStudentById(final Long enrollmentId) {
		Enrollment enrollment = enrollmentRepository.findWithStudentById(enrollmentId)
			.orElseThrow(() -> new EnrollmentException(ErrorCode.ENROLLMENT_NOT_FOUND));
		log.info("[Enrollment] 단건 조회 : enrollmentId = {}", enrollmentId);
		return enrollment;
	}
}
