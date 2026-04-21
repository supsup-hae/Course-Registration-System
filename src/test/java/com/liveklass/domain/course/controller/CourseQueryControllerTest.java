package com.liveklass.domain.course.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.liveklass.common.config.SecurityConfig;
import com.liveklass.common.constants.AuthConstants;
import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.course.controller.query.CourseQueryController;
import com.liveklass.domain.course.dto.common.CourseCardInfo;
import com.liveklass.domain.course.dto.common.CourseInfoDto;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.exception.CourseException;
import com.liveklass.domain.course.service.facade.CourseFacadeService;
import com.liveklass.domain.user.dto.common.UserCardInfo;
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
	@DisplayName("강의 목록 조회 API 호출 시 200 반환")
	void getCourses_returns200_whenValidRequest() throws Exception {
		// given
		Page<CourseCardInfo> page = new PageImpl<>(List.of(courseCardInfo()));
		given(courseFacadeService.findAllCourses(anyInt(), anyInt(), any(), any(), any(), any())).willReturn(page);

		// when & then
		mockMvc.perform(get("/api/v1/courses")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].title").value("테스트 강의"))
			.andExpect(jsonPath("$.content[0].creator.name").value("크리에이터"));
	}

	@Test
	@DisplayName("status 필터로 목록 조회 시 200 반환")
	void getCourses_returns200_withStatusFilter() throws Exception {
		// given
		Page<CourseCardInfo> page = new PageImpl<>(List.of(courseCardInfo()));
		given(courseFacadeService.findAllCourses(anyInt(), anyInt(), any(), any(), any(), any())).willReturn(page);

		// when & then
		mockMvc.perform(get("/api/v1/courses")
				.param("status", "OPEN")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("가격 범위 필터로 목록 조회 시 200 반환")
	void getCourses_returns200_withPriceFilter() throws Exception {
		// given
		given(courseFacadeService.findAllCourses(anyInt(), anyInt(), any(), any(), any(), any())).willReturn(
			Page.empty());

		// when & then
		mockMvc.perform(get("/api/v1/courses")
				.param("minPrice", "5000")
				.param("maxPrice", "50000")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT"))
			.andExpect(status().isOk());

		verify(courseFacadeService).findAllCourses(
			0, 10, null, BigDecimal.valueOf(5000), BigDecimal.valueOf(50000), null);
	}

	@Test
	@DisplayName("음수 가격 필터 입력 시 400 반환")
	void getCourses_returns400_whenNegativePrice() throws Exception {
		// when & then
		mockMvc.perform(get("/api/v1/courses")
				.param("minPrice", "-1000")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("인증 없이 목록 조회 API 호출 시 200 반환")
	void getCourses_returns200_whenUnauthenticated() throws Exception {
		// given
		given(courseFacadeService.findAllCourses(anyInt(), anyInt(), any(), any(), any(), any())).willReturn(
			Page.empty());

		// when & then
		mockMvc.perform(get("/api/v1/courses"))
			.andExpect(status().isOk());
	}

	private CourseCardInfo courseCardInfo() {
		return CourseCardInfo.builder()
			.courseId(1L)
			.title("테스트 강의")
			.price(BigDecimal.valueOf(10000))
			.status(CourseStatus.OPEN)
			.creator(UserCardInfo.builder()
				.userId(1L)
				.name("크리에이터")
				.build())
			.build();
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
