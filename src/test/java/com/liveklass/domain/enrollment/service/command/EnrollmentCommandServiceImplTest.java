package com.liveklass.domain.enrollment.service.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.enrollment.repository.EnrollmentRepository;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.enums.Role;

@ExtendWith(MockitoExtension.class)
class EnrollmentCommandServiceImplTest {

	@Mock
	private EnrollmentRepository enrollmentRepository;

	@InjectMocks
	private EnrollmentCommandServiceImpl service;

	@Test
	@DisplayName("savePending() 호출 시 PENDING 상태로 저장된다")
	void savePendingSavesEnrollmentWithPendingStatus() {
		// given
		User student = defaultStudent();
		Course course = defaultCourse();
		given(enrollmentRepository.save(any(Enrollment.class)))
			.willAnswer(inv -> inv.getArgument(0));

		// when
		Enrollment result = service.savePending(student, course);

		// then
		assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
		assertThat(result.getExpiresAt()).isNotNull();
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
