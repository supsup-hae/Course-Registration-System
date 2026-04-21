package com.liveklass.domain.enrollment.service.command;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.repository.EnrollmentRepository;
import com.liveklass.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentCommandServiceImpl implements EnrollmentCommandService {

	private final EnrollmentRepository enrollmentRepository;

	@Override
	public Enrollment savePending(final User student, final Course course) {
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
		log.info("[Enrollment] 대기 상태 저장 : enrollmentId = {}, studentId = {}, courseId = {}",
			savedEnrollment.getEnrollmentId(), savedEnrollment.getStudent().getUserId(),
			savedEnrollment.getCourse().getCourseId());
		return savedEnrollment;
	}

	@Override
	public void expire(final Enrollment enrollment) {
		enrollment.expire();
		log.info("[Enrollment] 시간 초과 만료 : enrollmentId = {}", enrollment.getEnrollmentId());
	}

	@Override
	public void confirm(final Enrollment enrollment) {
		enrollment.confirm();
		log.info("[Enrollment] 확정 완료 : enrollmentId = {}", enrollment.getEnrollmentId());
	}
}
