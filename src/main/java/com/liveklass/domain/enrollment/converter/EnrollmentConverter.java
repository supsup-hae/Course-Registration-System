package com.liveklass.domain.enrollment.converter;

import com.liveklass.domain.course.converter.CourseConverter;
import com.liveklass.domain.enrollment.dto.common.EnrollmentCardInfo;
import com.liveklass.domain.enrollment.dto.response.EnrollmentResDto;
import com.liveklass.domain.enrollment.entity.Enrollment;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EnrollmentConverter {

	public EnrollmentResDto toEnrollmentResDto(final Enrollment enrollment) {
		return EnrollmentResDto.builder()
			.enrollmentId(enrollment.getEnrollmentId())
			.courseId(enrollment.getCourse().getCourseId())
			.studentId(enrollment.getStudent().getUserId())
			.status(enrollment.getStatus())
			.expiresAt(enrollment.getExpiresAt())
			.build();
	}

	public EnrollmentCardInfo toEnrollmentCardInfo(final Enrollment enrollment) {
		return EnrollmentCardInfo.builder()
			.enrollmentId(enrollment.getEnrollmentId())
			.course(CourseConverter.toCourseCardInfo(enrollment.getCourse()))
			.status(enrollment.getStatus())
			.build();
	}
}
