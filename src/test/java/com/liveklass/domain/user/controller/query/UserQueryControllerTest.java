package com.liveklass.domain.user.controller.query;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.liveklass.common.config.SecurityConfig;
import com.liveklass.common.constants.AuthConstants;
import com.liveklass.domain.course.dto.common.CourseCardInfo;
import com.liveklass.domain.enrollment.dto.common.EnrollmentCardInfo;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.user.dto.common.UserCardInfo;
import com.liveklass.domain.user.service.facade.UserFacadeService;

@WebMvcTest(UserQueryController.class)
@Import(SecurityConfig.class)
class UserQueryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserFacadeService userFacadeService;

	private EnrollmentCardInfo createMockCardInfo() {
		return EnrollmentCardInfo.builder()
			.enrollmentId(999L)
			.status(EnrollmentStatus.PENDING)
			.course(
				CourseCardInfo.builder()
					.courseId(10L)
					.title("Spring Boot Master")
					.creator(UserCardInfo.builder().userId(2L).name("김튜터").build())
					.build()
			)
			.build();
	}

	@Test
	@DisplayName("STUDENT 권한으로 내 수강신청 목록 조회 - 200 반환")
	void authenticatedStudentReturns200() throws Exception {
		// given
		given(userFacadeService.findMyEnrollments(anyLong(), anyInt(), anyInt(), any(), any()))
			.willReturn(new PageImpl<>(List.of(createMockCardInfo()), PageRequest.of(0, 10), 1));

		// when & then
		mockMvc.perform(get("/api/v1/users/me/enrollments")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	@DisplayName("인증 없이 내 수강신청 목록 조회 - 403 반환")
	void unauthenticatedUserReturns403() throws Exception {
		// when & then
		mockMvc.perform(get("/api/v1/users/me/enrollments")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("CREATOR 권한으로 내 수강신청 목록 조회 - 403 반환")
	void creatorRoleReturns403() throws Exception {
		// when & then
		mockMvc.perform(get("/api/v1/users/me/enrollments")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("page=0, size=10 쿼리 파라미터 정상 바인딩 확인")
	void validQueryParamsReturns200() throws Exception {
		// given
		given(userFacadeService.findMyEnrollments(anyLong(), eq(0), eq(10), any(), any()))
			.willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

		// when & then
		mockMvc.perform(get("/api/v1/users/me/enrollments")
				.param("page", "0")
				.param("size", "10")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());

		verify(userFacadeService).findMyEnrollments(eq(1L), eq(0), eq(10), eq(null), any());
	}

	@Test
	@DisplayName("page=-1 요청 시 400 반환 (validation)")
	void negativePageReturns400() throws Exception {
		// when & then
		mockMvc.perform(get("/api/v1/users/me/enrollments")
				.param("page", "-1")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("size=-1 요청 시 400 반환 (validation)")
	void zeroSizeReturns400() throws Exception {
		// when & then
		mockMvc.perform(get("/api/v1/users/me/enrollments")
				.param("size", "-1")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("size=101 요청 시 400 반환 (validation)")
	void largeSizeReturns400() throws Exception {
		// when & then
		mockMvc.perform(get("/api/v1/users/me/enrollments")
				.param("size", "101")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("응답 body에 enrollmentId, course, status 필드 포함 확인")
	void responseBodyContainsExpectedFields() throws Exception {
		// given
		given(userFacadeService.findMyEnrollments(anyLong(), anyInt(), anyInt(), any(), any()))
			.willReturn(new PageImpl<>(List.of(createMockCardInfo()), PageRequest.of(0, 10), 1));

		// when & then
		mockMvc.perform(get("/api/v1/users/me/enrollments")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].enrollmentId").value(999))
			.andExpect(jsonPath("$.content[0].status").value("PENDING"))
			.andExpect(jsonPath("$.content[0].course").exists())
			.andExpect(jsonPath("$.content[0].course.courseId").value(10))
			.andExpect(jsonPath("$.content[0].course.title").value("Spring Boot Master"));
	}
}
