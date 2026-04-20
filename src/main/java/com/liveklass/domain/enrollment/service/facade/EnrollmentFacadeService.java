package com.liveklass.domain.enrollment.service.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.service.query.CourseQueryService;
import com.liveklass.domain.enrollment.EnrollmentException;
import com.liveklass.domain.enrollment.converter.EnrollmentConverter;
import com.liveklass.domain.enrollment.dto.response.EnrollmentResDto;
import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.service.command.EnrollmentCommandService;
import com.liveklass.domain.enrollment.service.concurrency.EnrollmentSlotCounter;
import com.liveklass.domain.enrollment.service.query.EnrollmentQueryService;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentFacadeService {

	private final EnrollmentQueryService enrollmentQueryService;
	private final EnrollmentCommandService enrollmentCommandService;
	private final EnrollmentSlotCounter redisCounter;

	private final UserQueryService userQueryService;
	private final CourseQueryService courseQueryService;

	@Transactional
	public EnrollmentResDto createPending(final Long studentId, final Long courseId) {
		User student = userQueryService.findById(studentId);
		Course course = courseQueryService.findById(courseId);

		validateNoDuplicate(studentId, courseId);

		boolean acquired = acquireSlotIfLimited(course);
		try {
			if (!course.isUnlimitedCapacity()) {
				revalidateWithPessimisticLock(course);
			}
			Enrollment enrollment = enrollmentCommandService.savePending(student, course);
			return EnrollmentConverter.toEnrollmentResDto(enrollment);
		} catch (RuntimeException ex) {
			if (acquired) {
				log.warn("[Enrollment] PENDING 저장 실패 Redis 카운터 감소 : courseId = {}", course.getCourseId(), ex);
				redisCounter.decrement(course.getCourseId());
			}
			throw ex;
		}
	}

	private void validateNoDuplicate(final Long studentId, final Long courseId) {
		if (enrollmentQueryService.existsActive(studentId, courseId)) {
			throw new EnrollmentException(ErrorCode.ENROLLMENT_DUPLICATE);
		}
	}

	private boolean acquireSlotIfLimited(final Course course) {
		if (course.isUnlimitedCapacity()) {
			return false;
		}
		boolean ok = redisCounter.tryIncrement(course.getCourseId(), course.getCapacity());
		if (!ok) {
			throw new EnrollmentException(ErrorCode.ENROLLMENT_CAPACITY_FULL);
		}
		return true;
	}

	private void revalidateWithPessimisticLock(final Course course) {
		courseQueryService.findByIdForUpdate(course.getCourseId());
		long active = enrollmentQueryService.countActive(course.getCourseId());
		if (active >= course.getCapacity()) {
			throw new EnrollmentException(ErrorCode.ENROLLMENT_CAPACITY_FULL);
		}
	}
}
