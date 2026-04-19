package com.liveklass.domain.course.entity;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.enums.Role;

class CourseTest {

	@Test
	@DisplayName("capacity가 null이면 인원 제한 없는 강의 초안 생성")
	void nullCapacityCreatesUnlimitedCourse() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", "설명", BigDecimal.valueOf(10000), null, null, null
		);

		// when
		Course course = Course.createDraft(defaultCreator(), dto);

		// then
		assertThat(course.getCapacity()).isNull();
		assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
	}

	@Test
	@DisplayName("capacity가 지정되면 해당 정원으로 강의 초안 생성")
	void givenCapacityCreatesCourseWithThatCapacity() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", "설명", BigDecimal.valueOf(10000), 30, null, null
		);

		// when
		Course course = Course.createDraft(defaultCreator(), dto);

		// then
		assertThat(course.getCapacity()).isEqualTo(30);
	}

	@Test
	@DisplayName("capacity가 null이면 인원 제한 없음 여부 true 반환")
	void nullCapacityReturnsUnlimitedTrue() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.ZERO, null, null, null
		);

		// when
		Course course = Course.createDraft(defaultCreator(), dto);

		// then
		assertThat(course.isUnlimitedCapacity()).isTrue();
	}

	@Test
	@DisplayName("capacity가 존재하면 인원 제한 없음 여부 false 반환")
	void setCapacityReturnsUnlimitedFalse() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.ZERO, 10, null, null
		);

		// when
		Course course = Course.createDraft(defaultCreator(), dto);

		// then
		assertThat(course.isUnlimitedCapacity()).isFalse();
	}

	@Test
	@DisplayName("DRAFT에서 OPEN으로 전환 가능")
	void draftCanTransitionToOpen() {
		Course course = draftCourse();
		assertThat(course.canTransitionTo(CourseStatus.OPEN)).isTrue();
	}

	@Test
	@DisplayName("DRAFT에서 CLOSED로 전환 불가")
	void draftCannotTransitionToClosed() {
		Course course = draftCourse();
		assertThat(course.canTransitionTo(CourseStatus.CLOSED)).isFalse();
	}

	@Test
	@DisplayName("DRAFT에서 DRAFT로 전환 불가")
	void draftCannotTransitionToDraft() {
		Course course = draftCourse();
		assertThat(course.canTransitionTo(CourseStatus.DRAFT)).isFalse();
	}

	@Test
	@DisplayName("OPEN에서 CLOSED로 전환 가능")
	void openCanTransitionToClosed() {
		Course course = openCourse();
		assertThat(course.canTransitionTo(CourseStatus.CLOSED)).isTrue();
	}

	@Test
	@DisplayName("OPEN에서 DRAFT로 전환 불가")
	void openCannotTransitionToDraft() {
		Course course = openCourse();
		assertThat(course.canTransitionTo(CourseStatus.DRAFT)).isFalse();
	}

	@Test
	@DisplayName("CLOSED에서 OPEN으로 재오픈 가능")
	void closedCanTransitionToOpen() {
		Course course = closedCourse();
		assertThat(course.canTransitionTo(CourseStatus.OPEN)).isTrue();
	}

	@Test
	@DisplayName("CLOSED에서 DRAFT로 전환 불가")
	void closedCannotTransitionToDraft() {
		Course course = closedCourse();
		assertThat(course.canTransitionTo(CourseStatus.DRAFT)).isFalse();
	}

	@Test
	@DisplayName("openWith()는 상태를 OPEN으로 바꾸고 날짜를 업데이트한다")
	void openWithUpdatesStatusAndDates() {
		Course course = draftCourse();
		LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime end = LocalDateTime.of(2026, 6, 1, 0, 0);

		course.openWith(start, end);

		assertThat(course.getStatus()).isEqualTo(CourseStatus.OPEN);
		assertThat(course.getStartDate()).isEqualTo(start);
		assertThat(course.getEndDate()).isEqualTo(end);
	}

	@Test
	@DisplayName("openWith()는 endDate가 null이어도 OPEN으로 전환된다")
	void openWithNullEndDateIsAllowed() {
		Course course = draftCourse();
		LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);

		course.openWith(start, null);

		assertThat(course.getStatus()).isEqualTo(CourseStatus.OPEN);
		assertThat(course.getStartDate()).isEqualTo(start);
		assertThat(course.getEndDate()).isNull();
	}

	private User defaultCreator() {
		return User.create("테스트 크리에이터", "creator@test.com", "password", Role.CREATOR);
	}

	private Course draftCourse() {
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", "설명", BigDecimal.valueOf(10000), null, null, null
		);
		return Course.createDraft(defaultCreator(), dto);
	}

	private Course openCourse() {
		Course course = draftCourse();
		course.openWith(LocalDateTime.of(2026, 5, 1, 0, 0), null);
		return course;
	}

	private Course closedCourse() {
		Course course = openCourse();
		course.updateStatus(CourseStatus.CLOSED);
		return course;
	}
}
