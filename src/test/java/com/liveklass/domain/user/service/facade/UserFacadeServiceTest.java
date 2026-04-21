package com.liveklass.domain.user.service.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.enrollment.dto.common.EnrollmentCardInfo;
import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.enrollment.service.query.EnrollmentQueryService;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.enums.Role;

@ExtendWith(MockitoExtension.class)
class UserFacadeServiceTest {

	@InjectMocks
	private UserFacadeService userFacadeService;

	@Mock
	private EnrollmentQueryService enrollmentQueryService;

	private Enrollment createMockEnrollment(EnrollmentStatus status) {
		User creator = User.create("Creator", "c@test.com", "pass", Role.CREATOR);
		Course course = Course.createDraft(creator, new RegisterCourseReqDto(
			"Spring Boot Class",
			"Description",
			BigDecimal.valueOf(10000),
			100,
			null,
			null
		));
		User student = User.create("Student", "s@test.com", "pass", Role.STUDENT);
		Enrollment enrollment = Enrollment.pending(student, course, LocalDateTime.now());
		if (status == EnrollmentStatus.CONFIRMED) {
			enrollment.confirm();
		} else if (status == EnrollmentStatus.CANCELLED) {
			enrollment.cancelByUser();
		}
		return enrollment;
	}

	@Test
	@DisplayName("정상 조회 - studentId로 Page 결과 반환")
	void findMyEnrollmentsReturnsPageSuccessfully() {
		// given
		Long studentId = 1L;
		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
		Enrollment enrollment = createMockEnrollment(EnrollmentStatus.PENDING);
		Page<Enrollment> enrollmentPage = new PageImpl<>(List.of(enrollment), pageRequest, 1);

		given(enrollmentQueryService.findByStudentId(eq(studentId), eq(EnrollmentStatus.PENDING), any()))
			.willReturn(enrollmentPage);

		// when
		Page<EnrollmentCardInfo> result = userFacadeService.findMyEnrollments(
			studentId, 0, 10, EnrollmentStatus.PENDING, Sort.Direction.DESC
		);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().getFirst().status()).isEqualTo(EnrollmentStatus.PENDING);
		verify(enrollmentQueryService, times(1)).findByStudentId(any(), any(), any());
	}

	@Test
	@DisplayName("결과가 비어있어도 빈 Page 반환 (예외 없음)")
	void returnsEmptyPageWhenNoEnrollments() {
		// given
		Long studentId = 1L;
		Page<Enrollment> emptyPage = new PageImpl<>(List.of());

		given(enrollmentQueryService.findByStudentId(eq(studentId), isNull(), any()))
			.willReturn(emptyPage);

		// when
		Page<EnrollmentCardInfo> result = userFacadeService.findMyEnrollments(
			studentId, 0, 10, null, Sort.Direction.DESC
		);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
		verify(enrollmentQueryService, times(1)).findByStudentId(eq(studentId), isNull(), any());
	}

	@Test
	@DisplayName("PENDING/CONFIRMED/CANCELLED 상태 모두 포함 조회 가능 (상태 필터 없는 경우)")
	void returnsAllStatusesWhenFilterIsNull() {
		// given
		Long studentId = 1L;
		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
		Enrollment pending = createMockEnrollment(EnrollmentStatus.PENDING);
		Enrollment confirmed = createMockEnrollment(EnrollmentStatus.CONFIRMED);
		Enrollment cancelled = createMockEnrollment(EnrollmentStatus.CANCELLED);

		Page<Enrollment> mixedPage = new PageImpl<>(List.of(pending, confirmed, cancelled), pageRequest, 3);

		given(enrollmentQueryService.findByStudentId(eq(studentId), isNull(), any()))
			.willReturn(mixedPage);

		// when
		Page<EnrollmentCardInfo> result = userFacadeService.findMyEnrollments(
			studentId, 0, 10, null, Sort.Direction.DESC
		);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(3);
		assertThat(result.getContent())
			.extracting("status")
			.containsExactly(
				EnrollmentStatus.PENDING,
				EnrollmentStatus.CONFIRMED,
				EnrollmentStatus.CANCELLED
			);
	}
}
