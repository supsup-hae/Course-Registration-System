package com.liveklass.domain.course.converter;

import com.liveklass.domain.course.dto.common.CourseCardInfo;
import com.liveklass.domain.course.dto.common.CourseInfoDto;
import com.liveklass.domain.course.dto.response.RegisterCourseResDto;
import com.liveklass.domain.course.dto.response.UpdateCourseStatusResDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.user.converter.UserConverter;
import com.liveklass.domain.user.dto.common.UserInfoDto;

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

	public CourseCardInfo toCourseCardInfo(final Course course) {
		return CourseCardInfo.builder()
			.courseId(course.getCourseId())
			.title(course.getTitle())
			.price(course.getPrice())
			.status(course.getStatus())
			.creator(UserConverter.toUserCardInfo(course.getCreator()))
			.build();
	}

	public CourseInfoDto toCourseInfoDto(final Course course, final UserInfoDto creatorInfo) {
		return CourseInfoDto.builder()
			.courseId(course.getCourseId())
			.creator(creatorInfo)
			.title(course.getTitle())
			.description(course.getDescription())
			.price(course.getPrice())
			.capacity(course.getCapacity())
			.status(course.getStatus())
			.startDate(course.getStartDate())
			.endDate(course.getEndDate())
			.createdAt(course.getCreatedAt())
			.build();
	}

	public CourseInfoDto withEnrollmentCount(final CourseInfoDto dto, final long count) {
		return CourseInfoDto.builder()
			.courseId(dto.courseId())
			.creator(dto.creator())
			.title(dto.title())
			.description(dto.description())
			.price(dto.price())
			.capacity(dto.capacity())
			.status(dto.status())
			.startDate(dto.startDate())
			.endDate(dto.endDate())
			.createdAt(dto.createdAt())
			.currentEnrollmentCount(count)
			.build();
	}
}
