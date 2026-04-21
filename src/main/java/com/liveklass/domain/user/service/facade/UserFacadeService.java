package com.liveklass.domain.user.service.facade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liveklass.domain.enrollment.converter.EnrollmentConverter;
import com.liveklass.domain.enrollment.dto.common.EnrollmentCardInfo;
import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.enrollment.service.query.EnrollmentQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFacadeService {

	private final EnrollmentQueryService enrollmentQueryService;

	@Transactional(readOnly = true)
	public Page<EnrollmentCardInfo> findMyEnrollments(
		final Long studentId,
		final int page,
		final int size,
		final EnrollmentStatus status,
		final Sort.Direction sortOrder
	) {
		PageRequest pageRequest = PageRequest.of(
			page,
			size,
			Sort.by(sortOrder, "createdAt")
		);

		Page<Enrollment> enrollments = enrollmentQueryService.findByStudentId(
			studentId, status, pageRequest
		);
		return enrollments.map(EnrollmentConverter::toEnrollmentCardInfo);
	}
}
