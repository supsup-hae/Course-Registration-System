package com.liveklass.domain.enrollment.service.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;

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
		student = spy(User.create("테스트 학생", "student@test.com", "password", Role.STUDENT));
		lenient().when(student.getUserId()).thenReturn(1L);

		course = spy(Course.createDraft(
			User.create("크리에이터", "creator@test.com", "password", Role.CREATOR),
			new RegisterCourseReqDto("테스트 강의", "설명", BigDecimal.valueOf(10000), 100, null, null)
		));
		lenient().when(course.getCourseId()).thenReturn(10L);
	}

	@Test
	@DisplayName("createPending 정상 호출 시 PENDING 반환")
	void createPendingReturnsPendingEnrollmentSuccessfully() {
		// given
		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(course);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(false);
		given(redisCounter.tryIncrement(any(), eq(100))).willReturn(true);
		given(enrollmentQueryService.countActive(any())).willReturn(50L);
		Enrollment saved = Enrollment.pending(student, course, LocalDateTime.now());
		given(enrollmentCommandService.savePending(student, course)).willReturn(saved);

		// when
		EnrollmentResDto result = facade.createPendingEnrollment(1L, 10L);

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(EnrollmentStatus.PENDING);
		verify(redisCounter, never()).decrement(any());
	}

	@Test
	@DisplayName("Redis 게이트 실패 시 CAPACITY_FULL 예외 발생")
	void createPendingEnrollmentFailsAtRedisGateWithCapacityFull() {
		// given
		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(course);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(false);
		given(redisCounter.tryIncrement(any(), eq(100))).willReturn(false);

		// when & then
		assertThatThrownBy(() -> facade.createPendingEnrollment(1L, 10L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENROLLMENT_CAPACITY_FULL));
		verify(enrollmentCommandService, never()).savePending(any(), any());
	}

	@Test
	@DisplayName("DB 재검증 실패 시 Redis DECR 보상 후 예외 발생")
	void createPendingEnrollmentFailsAtDbRevalidationWithRedisDecrCompensation() {
		// given
		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(course);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(false);
		given(redisCounter.tryIncrement(any(), eq(100))).willReturn(true);
		given(enrollmentQueryService.countActive(any())).willReturn(100L);

		// when & then
		assertThatThrownBy(() -> facade.createPendingEnrollment(1L, 10L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENROLLMENT_CAPACITY_FULL));
		then(redisCounter).should(times(1)).decrement(any());
	}

	@Test
	@DisplayName("중복 신청 시 DUPLICATE 예외 발생")
	void createPendingEnrollmentThrowsDuplicateExceptionWhenAlreadyEnrolled() {
		// given
		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(course);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(true);

		// when & then
		assertThatThrownBy(() -> facade.createPendingEnrollment(1L, 10L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENROLLMENT_DUPLICATE));
		verify(redisCounter, never()).tryIncrement(any(), anyInt());
	}

	@Test
	@DisplayName("무제한 정원이면 Redis 건너뛰고 PENDING 생성")
	void createPendingEnrollmentSkipsRedisWhenCapacityIsUnlimited() {
		// given
		Course unlimitedCourse = spy(Course.createDraft(
			User.create("크리에이터", "creator@test.com", "password", Role.CREATOR),
			new RegisterCourseReqDto("무제한 강의", "설명", BigDecimal.valueOf(10000), null, null, null)
		));
		lenient().when(unlimitedCourse.getCourseId()).thenReturn(20L);

		given(userQueryService.findById(1L)).willReturn(student);
		given(courseQueryService.findById(10L)).willReturn(unlimitedCourse);
		given(enrollmentQueryService.existsActive(1L, 10L)).willReturn(false);
		Enrollment saved = Enrollment.pending(student, unlimitedCourse, LocalDateTime.now());
		given(enrollmentCommandService.savePending(student, unlimitedCourse)).willReturn(saved);

		// when
		EnrollmentResDto result = facade.createPendingEnrollment(1L, 10L);

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(EnrollmentStatus.PENDING);
		verify(redisCounter, never()).tryIncrement(any(), anyInt());
	}

	@Test
	@DisplayName("PENDING 상태 수강신청 확정 시 CONFIRMED 반환")
	void confirmEnrollmentReturnsConfirmedSuccessfully() {
		// given
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		given(enrollmentQueryService.findWithStudentById(999L)).willReturn(enrollment);
		willAnswer(_ -> {
			enrollment.confirm();
			return null;
		}).given(enrollmentCommandService).confirm(enrollment);

		// when
		EnrollmentResDto result = facade.confirmEnrollment(1L, 999L);

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(EnrollmentStatus.CONFIRMED);
		verify(enrollmentCommandService).confirm(enrollment);
	}

	@Test
	@DisplayName("다른 학생의 수강신청 확정 시 ACCESS_DENIED 예외 발생")
	void confirmEnrollmentThrowsAccessDeniedWhenStudentIdDiffers() {
		// given
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		given(enrollmentQueryService.findWithStudentById(999L)).willReturn(enrollment);

		// when & then
		assertThatThrownBy(() -> facade.confirmEnrollment(2L, 999L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ACCESS_DENIED));
		verify(enrollmentCommandService, never()).confirm(any());
	}

	@Test
	@DisplayName("PENDING이 아닌 상태 확정 시 ENROLLMENT_INVALID_STATE 예외 발생")
	void confirmEnrollmentThrowsInvalidStateWhenNotPending() {
		// given
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		enrollment.confirm(); // Set to CONFIRMED
		given(enrollmentQueryService.findWithStudentById(999L)).willReturn(enrollment);

		// when & then
		assertThatThrownBy(() -> facade.confirmEnrollment(1L, 999L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENROLLMENT_INVALID_STATE));
		verify(enrollmentCommandService, never()).confirm(any());
	}

	@Test
	@DisplayName("수강신청이 없을 시 예외 전파 검증")
	void confirmEnrollmentThrowsExceptionWhenNotFound() {
		// given
		given(enrollmentQueryService.findWithStudentById(999L))
			.willThrow(new EnrollmentException(ErrorCode.ENROLLMENT_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> facade.confirmEnrollment(1L, 999L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND));
	}

	@Test
	@DisplayName("PENDING 상태 취소 시 CANCELLED 반환 및 Redis DECR 호출")
	void cancelEnrollmentReturnsCancelledAndCallsRedisDecr() {
		// given
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		given(enrollmentQueryService.findWithCourseAndStudentByIdForUpdate(999L)).willReturn(enrollment);
		willAnswer(_ -> {
			enrollment.cancelByUser();
			return null;
		}).given(enrollmentCommandService).cancel(enrollment);

		// when
		EnrollmentResDto result = facade.cancelEnrollment(1L, 999L);

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(EnrollmentStatus.CANCELLED);
		verify(redisCounter).decrement(10L);
	}

	@Test
	@DisplayName("CONFIRMED 상태 취소 시 CANCELLED 반환 및 Redis DECR 호출")
	void cancelEnrollmentReturnsCancelledWhenConfirmedAndCallsRedisDecr() {
		// given
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		enrollment.confirm();
		given(enrollmentQueryService.findWithCourseAndStudentByIdForUpdate(999L)).willReturn(enrollment);
		willAnswer(_ -> {
			enrollment.cancelByUser();
			return null;
		}).given(enrollmentCommandService).cancel(enrollment);

		// when
		EnrollmentResDto result = facade.cancelEnrollment(1L, 999L);

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(EnrollmentStatus.CANCELLED);
		verify(redisCounter).decrement(10L);
	}

	@Test
	@DisplayName("무제한 정원 강의 취소 시 Redis DECR 미호출")
	void cancelEnrollmentSkipsRedisDecrWhenUnlimitedCapacity() {
		// given
		Course unlimitedCourse = spy(Course.createDraft(
			User.create("크리에이터", "creator@test.com", "password", Role.CREATOR),
			new RegisterCourseReqDto("무제한 강의", "설명", BigDecimal.valueOf(10000), null, null, null)
		));
		lenient().when(unlimitedCourse.getCourseId()).thenReturn(20L);

		Enrollment enrollment = Enrollment.pending(student, unlimitedCourse, LocalDateTime.now());
		given(enrollmentQueryService.findWithCourseAndStudentByIdForUpdate(999L)).willReturn(enrollment);
		willAnswer(_ -> {
			enrollment.cancelByUser();
		 return null;
		}).given(enrollmentCommandService).cancel(enrollment);

		// when
		EnrollmentResDto result = facade.cancelEnrollment(1L, 999L);

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(EnrollmentStatus.CANCELLED);
		verify(redisCounter, never()).decrement(any());
	}

	@Test
	@DisplayName("이미 CANCELLED 상태 취소 시 ENROLLMENT_INVALID_STATE 예외 발생")
	void cancelEnrollmentThrowsInvalidStateWhenAlreadyCancelled() {
		// given
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		enrollment.cancelByUser();
		given(enrollmentQueryService.findWithCourseAndStudentByIdForUpdate(999L)).willReturn(enrollment);

		// when & then
		assertThatThrownBy(() -> facade.cancelEnrollment(1L, 999L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENROLLMENT_INVALID_STATE));
		verify(enrollmentCommandService, never()).cancel(any());
	}

	@Test
	@DisplayName("다른 학생의 enrollment 취소 시 ACCESS_DENIED 예외 발생")
	void cancelEnrollmentThrowsAccessDeniedWhenStudentIdDiffers() {
		// given
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		given(enrollmentQueryService.findWithCourseAndStudentByIdForUpdate(999L)).willReturn(enrollment);

		// when & then
		assertThatThrownBy(() -> facade.cancelEnrollment(2L, 999L))
			.isInstanceOf(EnrollmentException.class)
			.satisfies(ex -> assertThat(((EnrollmentException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ACCESS_DENIED));
		verify(enrollmentCommandService, never()).cancel(any());
	}

	@Test
	@DisplayName("Redis DECR 실패해도 취소 성공 검증")
	void cancelEnrollmentSucceedsEvenIfRedisDecrFails() {
		// given
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		given(enrollmentQueryService.findWithCourseAndStudentByIdForUpdate(999L)).willReturn(enrollment);
		willAnswer(_ -> {
			enrollment.cancelByUser();
			return null;
		}).given(enrollmentCommandService).cancel(enrollment);
		willThrow(new RuntimeException("Redis error")).given(redisCounter).decrement(10L);

		// when
		EnrollmentResDto result = facade.cancelEnrollment(1L, 999L);

		// then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(EnrollmentStatus.CANCELLED);
		verify(redisCounter).decrement(10L);
	}
}
