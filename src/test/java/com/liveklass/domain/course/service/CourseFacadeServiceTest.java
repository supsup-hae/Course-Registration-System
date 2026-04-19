package com.liveklass.domain.course.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.dto.response.RegisterCourseResDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.exception.CourseException;
import com.liveklass.domain.course.service.command.CourseCommandService;
import com.liveklass.domain.course.service.facade.CourseFacadeService;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.enums.Role;
import com.liveklass.domain.user.service.query.UserQueryService;

@ExtendWith(MockitoExtension.class)
class CourseFacadeServiceTest {

	@Mock
	private CourseCommandService courseCommandService;

	@Mock
	private UserQueryService userQueryService;

	@InjectMocks
	private CourseFacadeService courseFacadeService;

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
		given(userQueryService.findById(999L)).willThrow(CourseException.class);

		// when & then
		assertThatThrownBy(() -> courseFacadeService.registerCourse(999L, dto))
			.isInstanceOf(CourseException.class);
	}

	private User defaultCreator() {
		return User.create("테스트 크리에이터", "creator@test.com", "password", Role.CREATOR);
	}
}
