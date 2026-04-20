package com.liveklass.domain.course.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.liveklass.common.config.SecurityConfig;
import com.liveklass.common.constants.AuthConstants;
import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.course.controller.query.CourseQueryController;
import com.liveklass.domain.course.dto.common.CourseInfoDto;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.exception.CourseException;
import com.liveklass.domain.course.service.facade.CourseFacadeService;
import com.liveklass.domain.user.dto.common.UserInfoDto;

@WebMvcTest(CourseQueryController.class)
@Import(SecurityConfig.class)
class CourseQueryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CourseFacadeService courseFacadeService;

	@Test
	@DisplayName("강의 상세 조회 API 호출 시 200 반환")
	void getCourse_returns200_whenValidRequest() throws Exception {
		// given
		given(courseFacadeService.findCourseDetail(1L)).willReturn(courseInfoDto());

		// when & then
		mockMvc.perform(get("/api/v1/courses/1")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("존재하지 않는 강의 조회 시 404 반환")
	void getCourse_returns404_whenCourseNotFound() throws Exception {
		// given
		given(courseFacadeService.findCourseDetail(anyLong()))
			.willThrow(new CourseException(ErrorCode.NOT_FOUND));

		// when & then
		mockMvc.perform(get("/api/v1/courses/999")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT"))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("인증 없이 강의 조회 API 호출 시 403 반환")
	void getCourse_returns403_whenUnauthenticated() throws Exception {
		// when & then
		mockMvc.perform(get("/api/v1/courses/1"))
			.andExpect(status().isForbidden());
	}

	private CourseInfoDto courseInfoDto() {
		return CourseInfoDto.builder()
			.courseId(1L)
			.creator(UserInfoDto.builder()
				.userId(1L)
				.name("크리에이터")
				.email("creator@test.com")
				.role("CREATOR")
				.build())
			.title("테스트 강의")
			.price(BigDecimal.valueOf(10000))
			.status(CourseStatus.DRAFT)
			.createdAt(LocalDateTime.of(2026, 4, 1, 0, 0))
			.build();
	}
}
