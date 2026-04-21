package com.liveklass.domain.course.service.facade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.course.converter.CourseConverter;
import com.liveklass.domain.course.dto.common.CourseCardInfo;
import com.liveklass.domain.course.dto.common.CourseInfoDto;
import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.dto.request.UpdateCourseStatusReqDto;
import com.liveklass.domain.course.dto.response.RegisterCourseResDto;
import com.liveklass.domain.course.dto.response.UpdateCourseStatusResDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.exception.CourseException;
import com.liveklass.domain.course.service.cache.CourseDetailCacheService;
import com.liveklass.domain.course.service.command.CourseCommandService;
import com.liveklass.domain.course.service.query.CourseQueryService;
import com.liveklass.domain.enrollment.service.query.EnrollmentQueryService;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.enums.Role;
import com.liveklass.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseFacadeService {

	private final CourseCommandService courseCommandService;
	private final CourseQueryService courseQueryService;
	private final CourseDetailCacheService courseDetailCacheService;

	private final EnrollmentQueryService enrollmentQueryService;
	private final UserQueryService userQueryService;

	@Transactional
	public RegisterCourseResDto registerCourse(final Long creatorId, final RegisterCourseReqDto reqDto) {
		validateDateRange(reqDto);
		User creator = userQueryService.findById(creatorId);
		validateRole(creator);
		Course course = Course.createDraft(creator, reqDto);
		Course savedCourse = courseCommandService.registerCourse(course);
		return CourseConverter.toRegisterResDto(savedCourse);
	}

	@Transactional
	public UpdateCourseStatusResDto updateCourseStatus(
		final Long userId, final Long courseId, final UpdateCourseStatusReqDto reqDto
	) {
		Course course = courseQueryService.findById(courseId);
		validateCourseBelongToUser(course, userId);
		validateStatusTransition(course, reqDto);

		if (reqDto.status() == CourseStatus.OPEN) {
			courseCommandService.openWith(course, reqDto.startDate(), reqDto.endDate());
		} else {
			courseCommandService.updateStatus(course, reqDto.status());
		}

		courseDetailCacheService.evict(courseId);
		return CourseConverter.toUpdateStatusResDto(course);
	}

	public CourseInfoDto findCourseDetail(final Long courseId) {
		CourseInfoDto cached = courseDetailCacheService.load(courseId);
		long currentEnrollmentCount = enrollmentQueryService.countActive(courseId);
		return CourseConverter.withEnrollmentCount(cached, currentEnrollmentCount);
	}

	public Page<CourseCardInfo> findAllCourses(
		final int page,
		final int size,
		final CourseStatus status,
		final BigDecimal minPrice,
		final BigDecimal maxPrice,
		final Boolean hasCapacity
	) {
		return courseQueryService.findAllWithFilters(page, size, status, minPrice, maxPrice, hasCapacity)
			.map(CourseConverter::toCourseCardInfo);
	}

	private void validateRole(final User user) {
		if (user.getRole() != Role.CREATOR) {
			throw new CourseException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateDateRange(final RegisterCourseReqDto reqDto) {
		if (isInvalidDateRange(reqDto.startDate(), reqDto.endDate())) {
			throw new CourseException(ErrorCode.INVALID_COURSE_DATE_RANGE);
		}
	}

	private void validateCourseBelongToUser(final Course course, final Long userId) {
		if (!course.getCreator().getUserId().equals(userId)) {
			throw new CourseException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateStatusTransition(final Course course, final UpdateCourseStatusReqDto reqDto) {
		if (!course.canTransitionTo(reqDto.status())) {
			throw new CourseException(ErrorCode.INVALID_COURSE_STATUS_TRANSITION);
		}
		if (reqDto.status() == CourseStatus.OPEN && reqDto.startDate() == null) {
			throw new CourseException(ErrorCode.OPEN_REQUIRES_START_DATE);
		}
		if (reqDto.status() == CourseStatus.OPEN && isInvalidDateRange(reqDto.startDate(), reqDto.endDate())) {
			throw new CourseException(ErrorCode.INVALID_COURSE_DATE_RANGE);
		}
	}

	private boolean isInvalidDateRange(final LocalDateTime startDate, final LocalDateTime endDate) {
		return startDate != null && endDate != null && !endDate.isAfter(startDate);
	}
}
