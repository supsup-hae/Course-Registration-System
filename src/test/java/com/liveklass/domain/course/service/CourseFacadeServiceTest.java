package com.liveklass.domain.course.service;

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
import com.liveklass.domain.course.dto.request.UpdateCourseStatusReqDto;
import com.liveklass.domain.course.dto.response.RegisterCourseResDto;
import com.liveklass.domain.course.dto.response.UpdateCourseStatusResDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.exception.CourseException;
import com.liveklass.domain.course.service.command.CourseCommandService;
import com.liveklass.domain.course.service.facade.CourseFacadeService;
import com.liveklass.domain.course.service.query.CourseQueryService;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.enums.Role;
import com.liveklass.domain.user.exception.UserException;
import com.liveklass.domain.user.service.query.UserQueryService;

@ExtendWith(MockitoExtension.class)
class CourseFacadeServiceTest {

	@Mock
	private CourseCommandService courseCommandService;

	@Mock
	private CourseQueryService courseQueryService;

	@Mock
	private UserQueryService userQueryService;

	@InjectMocks
	private CourseFacadeService courseFacadeService;

	private User creator;
	private Course draftCourse;

	@BeforeEach
	void setUp() {
		creator = User.create("크리에이터", "creator@test.com", "password", Role.CREATOR);
		draftCourse = Course.createDraft(creator, new RegisterCourseReqDto(
			"테스트 강의", "설명", BigDecimal.valueOf(10000), null, null, null
		));
		setCreatorId(creator, 1L);

		lenient().doAnswer(invocation -> {
			Course course = invocation.getArgument(0);
			CourseStatus status = invocation.getArgument(1);
			course.updateStatus(status);
			return null;
		}).when(courseCommandService).updateStatus(any(Course.class), any(CourseStatus.class));

		lenient().doAnswer(invocation -> {
			Course course = invocation.getArgument(0);
			LocalDateTime startDate = invocation.getArgument(1);
			LocalDateTime endDate = invocation.getArgument(2);
			course.openWith(startDate, endDate);
			return null;
		}).when(courseCommandService).openWith(any(Course.class), any(), any());
	}

	@Test
	@DisplayName("강의 등록 시 DRAFT 상태와 courseId 응답 반환")
	void registerCourseReturnsDraftStatusWithCourseId() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.valueOf(10000), 10, null, null
		);
		given(userQueryService.findById(1L)).willReturn(defaultCreator());
		given(courseCommandService.registerCourse(any(Course.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		RegisterCourseResDto result = courseFacadeService.registerCourse(1L, dto);

		// then
		assertThat(result.status()).isEqualTo(CourseStatus.DRAFT);
		assertThat(result.courseId()).isNull();
	}

	@Test
	@DisplayName("정원이 null이면 무제한으로 강의 등록")
	void nullCapacityRegistersAsUnlimited() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.valueOf(10000), null, null, null
		);
		given(userQueryService.findById(1L)).willReturn(defaultCreator());
		given(courseCommandService.registerCourse(any(Course.class)))
			.willAnswer(invocation -> {
				Course course = invocation.getArgument(0);
				assertThat(course.isUnlimitedCapacity()).isTrue();
				return course;
			});

		// when & then
		courseFacadeService.registerCourse(1L, dto);
	}

	@Test
	@DisplayName("종료일이 시작일보다 이전이면 예외 발생")
	void endDateBeforeStartDateThrowsException() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.valueOf(10000), 10,
			LocalDateTime.of(2026, 6, 1, 10, 0),
			LocalDateTime.of(2026, 5, 1, 10, 0)
		);

		// when & then
		assertThatThrownBy(() -> courseFacadeService.registerCourse(1L, dto))
			.isInstanceOf(CourseException.class);
	}

	@Test
	@DisplayName("존재하지 않는 사용자 ID로 등록 시 예외 발생")
	void unknownCreatorIdThrowsException() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.valueOf(10000), 10, null, null
		);
		given(userQueryService.findById(999L)).willThrow(new UserException(ErrorCode.USER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> courseFacadeService.registerCourse(999L, dto))
			.isInstanceOf(UserException.class);
	}

	@Test
	@DisplayName("DRAFT 강의를 startDate와 함께 OPEN으로 전환 성공")
	void updateStatusDraftToOpenSuccess() {
		// given
		Long userId = 1L;
		Long courseId = 1L;
		LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
		UpdateCourseStatusReqDto reqDto = new UpdateCourseStatusReqDto(CourseStatus.OPEN, start, null);
		given(courseQueryService.findById(courseId)).willReturn(draftCourse);

		// when
		UpdateCourseStatusResDto result = courseFacadeService.updateCourseStatus(userId, courseId, reqDto);

		// then
		assertThat(result.status()).isEqualTo(CourseStatus.OPEN);
		assertThat(result.startDate()).isEqualTo(start);
		assertThat(result.endDate()).isNull();
	}

	@Test
	@DisplayName("OPEN 전환 시 startDate가 null이면 예외 발생")
	void updateStatusToOpenWithoutStartDateThrows() {
		// given
		Long userId = 1L;
		Long courseId = 1L;
		UpdateCourseStatusReqDto reqDto = new UpdateCourseStatusReqDto(CourseStatus.OPEN, null, null);
		given(courseQueryService.findById(courseId)).willReturn(draftCourse);

		// when & then
		assertThatThrownBy(() -> courseFacadeService.updateCourseStatus(userId, courseId, reqDto))
			.isInstanceOf(CourseException.class)
			.satisfies(ex -> assertThat(((CourseException) ex).getErrorCode())
				.isEqualTo(ErrorCode.OPEN_REQUIRES_START_DATE));
	}

	@Test
	@DisplayName("OPEN 전환 시 endDate가 startDate 이전이면 예외 발생")
	void updateStatusToOpenWithInvalidDateRangeThrows() {
		// given
		Long userId = 1L;
		Long courseId = 1L;
		LocalDateTime start = LocalDateTime.of(2026, 6, 1, 0, 0);
		LocalDateTime end = LocalDateTime.of(2026, 5, 1, 0, 0);
		UpdateCourseStatusReqDto reqDto = new UpdateCourseStatusReqDto(CourseStatus.OPEN, start, end);
		given(courseQueryService.findById(courseId)).willReturn(draftCourse);

		// when & then
		assertThatThrownBy(() -> courseFacadeService.updateCourseStatus(userId, courseId, reqDto))
			.isInstanceOf(CourseException.class)
			.satisfies(ex -> assertThat(((CourseException) ex).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_COURSE_DATE_RANGE));
	}

	@Test
	@DisplayName("허용되지 않는 전환(DRAFT→CLOSED)이면 예외 발생")
	void invalidStatusTransitionThrows() {
		// given
		Long userId = 1L;
		Long courseId = 1L;
		UpdateCourseStatusReqDto reqDto = new UpdateCourseStatusReqDto(CourseStatus.CLOSED, null, null);
		given(courseQueryService.findById(courseId)).willReturn(draftCourse);

		// when & then
		assertThatThrownBy(() -> courseFacadeService.updateCourseStatus(userId, courseId, reqDto))
			.isInstanceOf(CourseException.class)
			.satisfies(ex -> assertThat(((CourseException) ex).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_COURSE_STATUS_TRANSITION));
	}

	@Test
	@DisplayName("CLOSED 강의를 startDate와 함께 OPEN으로 재오픈 성공")
	void updateStatusClosedToOpenSuccess() {
		// given
		Long userId = 1L;
		Long courseId = 1L;
		Course closedCourse = Course.createDraft(creator, new RegisterCourseReqDto(
			"테스트 강의", "설명", BigDecimal.valueOf(10000), null, null, null
		));
		closedCourse.openWith(LocalDateTime.of(2026, 5, 1, 0, 0), null);
		closedCourse.updateStatus(CourseStatus.CLOSED);
		UpdateCourseStatusReqDto reqDto = new UpdateCourseStatusReqDto(
			CourseStatus.OPEN, LocalDateTime.of(2026, 7, 1, 0, 0), null
		);
		given(courseQueryService.findById(courseId)).willReturn(closedCourse);

		// when
		UpdateCourseStatusResDto result = courseFacadeService.updateCourseStatus(userId, courseId, reqDto);

		// then
		assertThat(result.status()).isEqualTo(CourseStatus.OPEN);
	}

	@Test
	@DisplayName("OPEN 강의를 CLOSED로 전환 성공")
	void updateStatusOpenToClosedSuccess() {
		// given
		Long userId = 1L;
		Long courseId = 1L;
		Course openCourse = Course.createDraft(creator, new RegisterCourseReqDto(
			"테스트 강의", "설명", BigDecimal.valueOf(10000), null, null, null
		));
		openCourse.openWith(LocalDateTime.of(2026, 5, 1, 0, 0), null);

		UpdateCourseStatusReqDto reqDto = new UpdateCourseStatusReqDto(CourseStatus.CLOSED, null, null);
		given(courseQueryService.findById(courseId)).willReturn(openCourse);

		// when
		UpdateCourseStatusResDto result = courseFacadeService.updateCourseStatus(userId, courseId, reqDto);

		// then
		assertThat(result.status()).isEqualTo(CourseStatus.CLOSED);
	}

	private User defaultCreator() {
		return User.create("테스트 크리에이터", "creator@test.com", "password", Role.CREATOR);
	}

	private void setCreatorId(final User user, final Long id) {
		try {
			var field = User.class.getDeclaredField("userId");
			field.setAccessible(true);
			field.set(user, id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
