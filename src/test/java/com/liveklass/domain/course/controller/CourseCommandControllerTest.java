package com.liveklass.domain.course.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.liveklass.common.config.SecurityConfig;
import com.liveklass.common.constants.AuthConstants;
import com.liveklass.common.error.ErrorCode;
import com.liveklass.common.error.exception.BusinessException;
import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.dto.request.UpdateCourseStatusReqDto;
import com.liveklass.domain.course.dto.response.RegisterCourseResDto;
import com.liveklass.domain.course.dto.response.UpdateCourseStatusResDto;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.service.facade.CourseFacadeService;

@WebMvcTest(CourseCommandController.class)
@Import(SecurityConfig.class)
class CourseCommandControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CourseFacadeService courseFacadeService;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	}

	@Test
	@DisplayName("CREATOR 권한으로 강의 등록 API 호출 시 201 반환")
	void creatorRoleReturns201() throws Exception {
		// given
		given(courseFacadeService.registerCourse(anyLong(), any()))
			.willReturn(new RegisterCourseResDto(1L, CourseStatus.DRAFT));

		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validDto())))
			.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("인증 없이 강의 등록 API 호출 시 403 반환")
	void noAuthenticationReturns403() throws Exception {
		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validDto())))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("CREATOR 권한 없이 강의 등록 API 호출 시 403 반환")
	void studentRoleReturns403() throws Exception {
		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validDto())))
			.andExpect(status().isForbidden());
	}

	@ParameterizedTest
	@MethodSource("invalidRequestCases")
	@DisplayName("필수값 누락 및 정원 0으로 API 호출 시 400 반환")
	void invalidRequestReturns400(RegisterCourseReqDto dto) throws Exception {
		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("종료일이 시작일보다 이전으로 API 호출 시 400 반환")
	void endDateBeforeStartDateReturns400() throws Exception {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.valueOf(10000), 10,
			LocalDateTime.of(2026, 6, 1, 10, 0),
			LocalDateTime.of(2026, 5, 1, 10, 0)
		);
		given(courseFacadeService.registerCourse(anyLong(), any()))
			.willThrow(new BusinessException(ErrorCode.INVALID_COURSE_DATE_RANGE));

		// when & then
		mockMvc.perform(post("/api/v1/courses")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest());
	}

	static Stream<RegisterCourseReqDto> invalidRequestCases() {
		return Stream.of(
			new RegisterCourseReqDto(null, null, BigDecimal.valueOf(10000), 10, null, null),
			new RegisterCourseReqDto("테스트 강의", null, null, 10, null, null),
			new RegisterCourseReqDto("테스트 강의", null, BigDecimal.valueOf(10000), 0, null, null)
		);
	}

	private RegisterCourseReqDto validDto() {
		return new RegisterCourseReqDto("테스트 강의", null, BigDecimal.valueOf(10000), 10, null, null);
	}

	@Test
	@DisplayName("CREATOR 권한으로 강의 상태 변경 API 호출 시 200 반환")
	void updateStatusCreatorRoleReturns200() throws Exception {
		// given
		UpdateCourseStatusReqDto dto = new UpdateCourseStatusReqDto(CourseStatus.OPEN, null, null);
		given(courseFacadeService.updateCourseStatus(anyLong(), anyLong(), any()))
			.willReturn(UpdateCourseStatusResDto.builder()
				.courseId(1L)
				.status(CourseStatus.OPEN)
				.build());

		// when & then
		mockMvc.perform(patch("/api/v1/courses/1/status")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("인증 없이 강의 상태 변경 API 호출 시 403 반환")
	void updateStatusNoAuthenticationReturns403() throws Exception {
		// given
		UpdateCourseStatusReqDto dto = new UpdateCourseStatusReqDto(CourseStatus.OPEN, null, null);

		// when & then
		mockMvc.perform(patch("/api/v1/courses/1/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isForbidden());
	}
}
