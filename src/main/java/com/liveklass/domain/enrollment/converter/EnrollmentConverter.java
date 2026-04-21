package com.liveklass.domain.enrollment.converter;

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
}
