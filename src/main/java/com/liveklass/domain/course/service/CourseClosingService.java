package com.liveklass.domain.course.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.service.command.CourseCommandService;
import com.liveklass.domain.course.service.query.CourseQueryService;
import com.liveklass.domain.enrollment.service.query.EnrollmentQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseClosingService {

	private final CourseQueryService courseQueryService;
	private final CourseCommandService courseCommandService;
	private final EnrollmentQueryService enrollmentQueryService;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void closeIfFull(final Long courseId) {
		Course lockedCourse = courseQueryService.findByIdForUpdate(courseId);
		long dbCount = enrollmentQueryService.countActive(lockedCourse.getCourseId());

		if (dbCount >= lockedCourse.getCapacity()) {
			courseCommandService.updateStatus(lockedCourse, CourseStatus.CLOSED);
			log.info("[Course] 정원 초과로 강의 자동 CLOSED : courseId = {}, capacity = {}, activeCount = {}",
				lockedCourse.getCourseId(), lockedCourse.getCapacity(), dbCount);
		}
	}
}
