package com.liveklass.domain.course.service;

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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.repository.CourseRepository;
import com.liveklass.domain.course.service.command.CourseCommandServiceImpl;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.enums.Role;

@ExtendWith(MockitoExtension.class)
class CourseCommandServiceImplTest {

	@Mock
	private CourseRepository courseRepository;

	@InjectMocks
	private CourseCommandServiceImpl courseCommandService;

	@Test
	@DisplayName("강의 저장 시 save 호출 및 저장된 강의 반환")
	void saveCalledAndReturnsSavedCourse() {
		// given
		Course course = defaultCourse();
		given(courseRepository.save(any(Course.class))).willReturn(course);

		// when
		Course result = courseCommandService.registerCourse(course);

		// then
		then(courseRepository).should(times(1)).save(course);
		assertThat(result).isEqualTo(course);
	}

	@Test
	@DisplayName("repository 저장 실패 시 예외 전파")
	void repositoryFailurePropagatesException() {
		// given
		Course course = defaultCourse();
		given(courseRepository.save(any(Course.class)))
			.willThrow(new DataIntegrityViolationException("DB 오류"));

		// when & then
		assertThatThrownBy(() -> courseCommandService.registerCourse(course))
			.isInstanceOf(DataAccessException.class);
	}

	private Course defaultCourse() {
		User creator = User.create("테스트 크리에이터", "creator@test.com", "password", Role.CREATOR);
		return Course.createDraft(creator, new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.valueOf(10000), 10, null, null
		));
	}
}
