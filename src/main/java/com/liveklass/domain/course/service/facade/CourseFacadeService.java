package com.liveklass.domain.course.service.facade;

import org.springframework.stereotype.Service;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.course.converter.CourseConverter;
import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.dto.response.RegisterCourseResDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.exception.CourseException;
import com.liveklass.domain.course.service.command.CourseCommandService;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseFacadeService {

	private final CourseCommandService courseCommandService;
	private final UserQueryService userQueryService;

	public RegisterCourseResDto registerCourse(final Long creatorId, final RegisterCourseReqDto reqDto) {
		validateDateRange(reqDto);
		User creator = userQueryService.findById(creatorId);
		Course course = Course.createDraft(creator, reqDto);
		Course savedCourse = courseCommandService.registerCourse(course);
		return CourseConverter.toRegisterResDto(savedCourse);
	}

	private void validateDateRange(final RegisterCourseReqDto reqDto) {
		if (reqDto.startDate() != null && reqDto.endDate() != null && !reqDto.endDate().isAfter(reqDto.startDate())) {
			throw new CourseException(ErrorCode.INVALID_COURSE_DATE_RANGE);
		}
	}

}
