package com.liveklass.domain.enrollment.service.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.service.query.CourseQueryService;
import com.liveklass.domain.enrollment.EnrollmentException;
import com.liveklass.domain.enrollment.dto.response.EnrollmentResDto;
import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.enrollment.service.command.EnrollmentCommandService;
import com.liveklass.domain.enrollment.service.concurrency.EnrollmentSlotCounter;
import com.liveklass.domain.enrollment.service.query.EnrollmentQueryService;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.enums.Role;
import com.liveklass.domain.user.service.query.UserQueryService;

@ExtendWith(MockitoExtension.class)
class EnrollmentFacadeServiceTest {

	@Mock
	private UserQueryService userQueryService;

	@Mock
	private CourseQueryService courseQueryService;

	@Mock
	private EnrollmentQueryService enrollmentQueryService;

	@Mock
	private EnrollmentCommandService enrollmentCommandService;

	@Mock
	private EnrollmentSlotCounter redisCounter;

	@InjectMocks
	private EnrollmentFacadeService facade;

	private User student;
	private Course course;

	@BeforeEach
	void setUp() {
		student = User.create("테스트 학생", "student@test.com", "password", Role.STUDENT);
		course = Course.createDraft(
			User.create("크리에이터", "creator@test.com", "password", Role.CREATOR),
			new RegisterCourseReqDto("테스트 강의", "설명", BigDecimal.valueOf(10000), 100, null, null)
		);
	}

	@Test
	@DisplayName("createPending 정상 호출 시 PENDING 반환")
	void createPendingReturnsPendingSuccessfully() {
		// given
		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(course);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(false);
		given(redisCounter.tryIncrement(any(), eq(100))).willReturn(true);
		given(enrollmentQueryService.countActive(any())).willReturn(50L);
		Enrollment saved = Enrollment.pending(student, course, LocalDateTime.now());
		given(enrollmentCommandService.savePending(student, course)).willReturn(saved);

		// when
		EnrollmentResDto result = facade.createPending(1L, 10L);

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(EnrollmentStatus.PENDING);
		verify(redisCounter, never()).decrement(any());
	}

	@Test
	@DisplayName("Redis 게이트 실패 시 CAPACITY_FULL 예외 발생")
	void createPendingFailsAtRedisGateWithCapacityFull() {
		// given
		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(course);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(false);
		given(redisCounter.tryIncrement(any(), eq(100))).willReturn(false);

		// when & then
		assertThatThrownBy(() -> facade.createPending(1L, 10L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENROLLMENT_CAPACITY_FULL));
		verify(enrollmentCommandService, never()).savePending(any(), any());
	}

	@Test
	@DisplayName("DB 재검증 실패 시 Redis DECR 보상 후 예외 발생")
	void createPendingFailsAtDbRevalidationWithRedisDecrCompensation() {
		// given
		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(course);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(false);
		given(redisCounter.tryIncrement(any(), eq(100))).willReturn(true);
		given(enrollmentQueryService.countActive(any())).willReturn(100L);

		// when & then
		assertThatThrownBy(() -> facade.createPending(1L, 10L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENROLLMENT_CAPACITY_FULL));
		then(redisCounter).should(times(1)).decrement(any());
	}

	@Test
	@DisplayName("중복 신청 시 DUPLICATE 예외 발생")
	void createPendingThrowsDuplicateExceptionWhenAlreadyEnrolled() {
		// given
		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(course);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(true);

		// when & then
		assertThatThrownBy(() -> facade.createPending(1L, 10L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENROLLMENT_DUPLICATE));
		verify(redisCounter, never()).tryIncrement(any(), anyInt());
	}

	@Test
	@DisplayName("무제한 정원이면 Redis 건너뛰고 PENDING 생성")
	void createPendingSkipsRedisWhenCapacityIsUnlimited() {
		// given
		Course unlimitedCourse = Course.createDraft(
			User.create("크리에이터", "creator@test.com", "password", Role.CREATOR),
			new RegisterCourseReqDto("무제한 강의", "설명", BigDecimal.valueOf(10000), null, null, null)
		);
		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(unlimitedCourse);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(false);
		Enrollment saved = Enrollment.pending(student, unlimitedCourse, LocalDateTime.now());
		given(enrollmentCommandService.savePending(student, unlimitedCourse)).willReturn(saved);

		// when
		EnrollmentResDto result = facade.createPending(1L, 10L);

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(EnrollmentStatus.PENDING);
		verify(redisCounter, never()).tryIncrement(any(), anyInt());
	}
}
