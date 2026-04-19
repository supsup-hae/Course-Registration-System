package com.liveklass.domain.course.converter;

import com.liveklass.domain.course.dto.response.RegisterCourseResDto;
import com.liveklass.domain.course.dto.response.UpdateCourseStatusResDto;
import com.liveklass.domain.course.entity.Course;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CourseConverter {

	public RegisterCourseResDto toRegisterResDto(final Course course) {
		return RegisterCourseResDto.builder()
			.courseId(course.getCourseId())
			.status(course.getStatus())
			.build();
	}

	public UpdateCourseStatusResDto toUpdateStatusResDto(final Course course) {
		return UpdateCourseStatusResDto.builder()
			.courseId(course.getCourseId())
			.status(course.getStatus())
			.startDate(course.getStartDate())
			.endDate(course.getEndDate())
			.build();
	}
}
