package com.liveklass.domain.enrollment.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.service.command.CourseCommandService;
import com.liveklass.domain.course.service.query.CourseQueryService;
import com.liveklass.domain.enrollment.service.concurrency.EnrollmentSlotCounter;
import com.liveklass.domain.enrollment.service.query.EnrollmentQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentReconciliationScheduler {

	private final CourseQueryService courseQueryService;
	private final CourseCommandService courseCommandService;
	private final EnrollmentQueryService enrollmentQueryService;
	private final EnrollmentSlotCounter redisCounter;

	@Scheduled(cron = "0 */5 * * * *")
	@Transactional(readOnly = true)
	public void reconcile() {
		List<Course> openCourses = courseQueryService.findOpenCoursesWithCapacity();
		for (Course course : openCourses) {
			long dbCount = enrollmentQueryService.countActive(course.getCourseId());
			long redisCount = redisCounter.get(course.getCourseId());
			if (redisCount < dbCount) {
				redisCounter.set(course.getCourseId(), dbCount);
				log.warn("[Enrollment] Redis 카운터 보정 : courseId = {}, dbCount = {}, redisCount = {} -> {}",
					course.getCourseId(), dbCount, redisCount, dbCount
				);
			}
		}
	}

	@Scheduled(cron = "0 */5 * * * *")
	@Transactional
	public void closeFullCourses() {
		List<Course> openCourses = courseQueryService.findOpenCoursesWithCapacity();
		for (Course course : openCourses) {
			long dbCount = enrollmentQueryService.countActive(course.getCourseId());
			if (dbCount >= course.getCapacity()) {
				courseCommandService.updateStatus(course, CourseStatus.CLOSED);
				log.info("[Enrollment] 정원 초과로 강의 자동 CLOSED : courseId = {}, capacity = {}, activeCount = {}",
					course.getCourseId(), course.getCapacity(), dbCount);
			}
		}
	}
}
