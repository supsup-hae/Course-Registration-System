package com.liveklass.domain.enrollment.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import com.liveklass.domain.enrollment.EnrollmentException;
import com.liveklass.domain.enrollment.controller.command.EnrollmentCommandController;
import com.liveklass.domain.enrollment.dto.request.CreateEnrollmentReqDto;
import com.liveklass.domain.enrollment.dto.response.EnrollmentResDto;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.enrollment.service.facade.EnrollmentFacadeService;

@WebMvcTest(EnrollmentCommandController.class)
@Import(SecurityConfig.class)
class EnrollmentCommandControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EnrollmentFacadeService enrollmentFacadeService;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	}

	@Test
	@DisplayName("STUDENT 권한으로 수강신청 생성 API 호출 시 201 반환")
	void studentRoleReturns201() throws Exception {
		// given
		given(enrollmentFacadeService.createPendingEnrollment(1L, 10L))
			.willReturn(pendingResDto());

		// when & then
		mockMvc.perform(post("/api/v1/enrollments")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateEnrollmentReqDto(10L))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.enrollmentId").value(999))
			.andExpect(jsonPath("$.data.status").value("PENDING"));
	}

	@Test
	@DisplayName("인증 없이 수강신청 생성 API 호출 시 403 반환")
	void noAuthenticationReturns403() throws Exception {
		// when & then
		mockMvc.perform(post("/api/v1/enrollments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateEnrollmentReqDto(10L))))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("STUDENT 권한 없이 수강신청 생성 API 호출 시 403 반환")
	void creatorRoleReturns403() throws Exception {
		// when & then
		mockMvc.perform(post("/api/v1/enrollments")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateEnrollmentReqDto(10L))))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("필수값 courseId 누락 시 400 반환")
	void missingCourseIdReturns400() throws Exception {
		// when & then
		mockMvc.perform(post("/api/v1/enrollments")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"courseId\":null}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("정원 초과 시 409 반환")
	void capacityFullReturns409() throws Exception {
		given(enrollmentFacadeService.createPendingEnrollment(1L, 10L))
			.willThrow(new EnrollmentException(ErrorCode.ENROLLMENT_CAPACITY_FULL));

		mockMvc.perform(post("/api/v1/enrollments")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateEnrollmentReqDto(10L))))
			.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("중복 신청 시 409 반환")
	void duplicateEnrollmentReturns409() throws Exception {
		given(enrollmentFacadeService.createPendingEnrollment(1L, 10L))
			.willThrow(new EnrollmentException(ErrorCode.ENROLLMENT_DUPLICATE));

		mockMvc.perform(post("/api/v1/enrollments")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateEnrollmentReqDto(10L))))
			.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("STUDENT 권한으로 확정 API 호출 시 200 반환")
	void confirmEnrollmentByStudentReturns200() throws Exception {
		// given
		given(enrollmentFacadeService.confirmEnrollment(1L, 999L))
			.willReturn(confirmedResDto());

		// when & then
		mockMvc.perform(patch("/api/v1/enrollments/{id}/confirm", 999L)
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.enrollmentId").value(999))
			.andExpect(jsonPath("$.data.status").value("CONFIRMED"));
	}

	@Test
	@DisplayName("인증 없이 확정 API 호출 시 403 반환")
	void confirmEnrollmentWithoutAuthReturns403() throws Exception {
		// when & then
		mockMvc.perform(patch("/api/v1/enrollments/{id}/confirm", 999L))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("CREATOR 권한으로 확정 API 호출 시 403 반환")
	void confirmEnrollmentByCreatorReturns403() throws Exception {
		// when & then
		mockMvc.perform(patch("/api/v1/enrollments/{id}/confirm", 999L)
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "CREATOR"))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("이미 확정/취소된 상태 확정 시 409 반환")
	void confirmEnrollmentWithInvalidStateReturns409() throws Exception {
		// given
		given(enrollmentFacadeService.confirmEnrollment(1L, 999L))
			.willThrow(new EnrollmentException(ErrorCode.ENROLLMENT_INVALID_STATE));

		// when & then
		mockMvc.perform(patch("/api/v1/enrollments/{id}/confirm", 999L)
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT"))
			.andExpect(status().isConflict());
	}

	private EnrollmentResDto pendingResDto() {
		return EnrollmentResDto.builder()
			.enrollmentId(999L)
			.courseId(10L)
			.studentId(1L)
			.status(EnrollmentStatus.PENDING)
			.expiresAt(LocalDateTime.now().plusMinutes(15))
			.build();
	}

	private EnrollmentResDto confirmedResDto() {
		return EnrollmentResDto.builder()
			.enrollmentId(999L)
			.courseId(10L)
			.studentId(1L)
			.status(EnrollmentStatus.CONFIRMED)
			.build();
	}
}
