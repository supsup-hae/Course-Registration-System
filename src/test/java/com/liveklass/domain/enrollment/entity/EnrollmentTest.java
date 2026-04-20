package com.liveklass.domain.enrollment.entity;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.enums.Role;

class EnrollmentTest {

	@Test
	@DisplayName("PENDING 생성 시 만료시각이 15분 후로 설정")
	void pendingCreatedWithExpiryAtPlus15Minutes() {
		// given
		LocalDateTime now = LocalDateTime.now();

		// when
		Enrollment enrollment = Enrollment.pending(defaultStudent(), defaultCourse(), now);

		// then
		assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
		assertThat(enrollment.getExpiresAt()).isEqualTo(now.plusMinutes(15));
		assertThat(enrollment.getCancelledReason()).isNull();
	}

	@Test
	@DisplayName("expire() 호출 시 CANCELLED 상태와 TTL_EXPIRED 사유가 설정")
	void expireChangesStatusToCancelledWithTtlExpiredReason() {
		// given
		Enrollment enrollment = Enrollment.pending(defaultStudent(), defaultCourse(), LocalDateTime.now());

		// when
		enrollment.expire();

		// then
		assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
		assertThat(enrollment.getCancelledReason()).isEqualTo("TTL_EXPIRED");
		assertThat(enrollment.getCancelledAt()).isNotNull();
	}

	@Test
	@DisplayName("cancelByUser() 호출 시 CANCELLED 상태와 USER_REQUEST 사유가 설정")
	void cancelByUserChangesStatusToCancelledWithUserRequestReason() {
		// given
		Enrollment enrollment = Enrollment.pending(defaultStudent(), defaultCourse(), LocalDateTime.now());

		// when
		enrollment.cancelByUser();

		// then
		assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
		assertThat(enrollment.getCancelledReason()).isEqualTo("USER_REQUEST");
		assertThat(enrollment.getCancelledAt()).isNotNull();
	}

	private User defaultStudent() {
		return User.create("테스트 학생", "student@test.com", "password", Role.STUDENT);
	}

	private Course defaultCourse() {
		User creator = User.create("테스트 크리에이터", "creator@test.com", "password", Role.CREATOR);
		return Course.createDraft(creator, new RegisterCourseReqDto(
			"테스트 강의", "설명", BigDecimal.valueOf(10000), 30, null, null
		));
	}
}
