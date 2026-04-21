package com.liveklass.domain.course.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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
import com.liveklass.domain.enrollment.dto.response.CourseEnrollmentInfo;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
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
	void getCourseReturns200WhenValidRequest() throws Exception {
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
	void getCourseReturns404WhenCourseNotFound() throws Exception {
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
	void getCoursesReturns200WhenValidRequest() throws Exception {
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
	void getCoursesReturns200WithStatusFilter() throws Exception {
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
	void getCoursesReturns200WithPriceFilter() throws Exception {
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
	void getCoursesReturns400WhenNegativePrice() throws Exception {
		// when & then
		mockMvc.perform(get("/api/v1/courses")
				.param("minPrice", "-1000")
				.header(AuthConstants.HEADER_USER_ID, "1")
				.header(AuthConstants.HEADER_USER_ROLE, "STUDENT"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("인증 없이 목록 조회 API 호출 시 200 반환")
	void getCoursesReturns200WhenUnauthenticated() throws Exception {
		// given
		given(courseFacadeService.findAllCourses(anyInt(), anyInt(), any(), any(), any(), any())).willReturn(
			Page.empty());

		// when & then
		mockMvc.perform(get("/api/v1/courses"))
			.andExpect(status().isOk());
	}

	@Nested
	@DisplayName("강의별 수강생 목록 조회 API")
	class FindCourseEnrollments {

		@Test
		@DisplayName("CREATOR 권한으로 수강생 목록 조회 - 200 반환")
		void creatorReturns200() throws Exception {
			// given
			given(courseFacadeService.findCourseEnrollments(anyLong(), anyLong(), anyInt(), anyInt(), any(), any()))
				.willReturn(new PageImpl<>(List.of(courseEnrollmentInfo()), PageRequest.of(0, 10), 1));

			// when & then
			mockMvc.perform(get("/api/v1/courses/1/enrollments")
					.header(AuthConstants.HEADER_USER_ID, "1")
					.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
		}

		@Test
		@DisplayName("STUDENT 권한으로 수강생 목록 조회 - 403 반환")
		void studentReturns403() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/courses/1/enrollments")
					.header(AuthConstants.HEADER_USER_ID, "1")
					.header(AuthConstants.HEADER_USER_ROLE, "STUDENT")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("인증 없이 수강생 목록 조회 - 403 반환")
		void unauthenticatedReturns403() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/courses/1/enrollments")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("page=0, size=10 쿼리 파라미터 정상 바인딩 확인")
		void validQueryParamsReturns200() throws Exception {
			// given
			given(courseFacadeService.findCourseEnrollments(anyLong(), anyLong(), eq(0), eq(10), any(), any()))
				.willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

			// when & then
			mockMvc.perform(get("/api/v1/courses/1/enrollments")
					.param("page", "0")
					.param("size", "10")
					.header(AuthConstants.HEADER_USER_ID, "1")
					.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

			verify(courseFacadeService).findCourseEnrollments(eq(1L), eq(1L), eq(0), eq(10), eq(null), any());
		}

		@Test
		@DisplayName("page=-1 요청 시 400 반환")
		void negativePageReturns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/courses/1/enrollments")
					.param("page", "-1")
					.header(AuthConstants.HEADER_USER_ID, "1")
					.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("size=101 요청 시 400 반환")
		void largeSizeReturns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/courses/1/enrollments")
					.param("size", "101")
					.header(AuthConstants.HEADER_USER_ID, "1")
					.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("다른 크리에이터의 강의 수강생 목록 조회 시 403 반환")
		void otherCreatorsCourseReturns403() throws Exception {
			// given
			given(courseFacadeService.findCourseEnrollments(anyLong(), anyLong(), anyInt(), anyInt(), any(), any()))
				.willThrow(new CourseException(ErrorCode.ACCESS_DENIED));

			// when & then
			mockMvc.perform(get("/api/v1/courses/999/enrollments")
					.header(AuthConstants.HEADER_USER_ID, "1")
					.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("응답 body에 enrollmentId, student, status 필드 포함 확인")
		void responseBodyContainsExpectedFields() throws Exception {
			// given
			given(courseFacadeService.findCourseEnrollments(anyLong(), anyLong(), anyInt(), anyInt(), any(), any()))
				.willReturn(new PageImpl<>(List.of(courseEnrollmentInfo()), PageRequest.of(0, 10), 1));

			// when & then
			mockMvc.perform(get("/api/v1/courses/1/enrollments")
					.header(AuthConstants.HEADER_USER_ID, "1")
					.header(AuthConstants.HEADER_USER_ROLE, "CREATOR")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].enrollmentId").value(100))
				.andExpect(jsonPath("$.content[0].status").value("CONFIRMED"))
				.andExpect(jsonPath("$.content[0].student").exists())
				.andExpect(jsonPath("$.content[0].student.userId").value(10))
				.andExpect(jsonPath("$.content[0].student.name").value("수강생"));
		}
	}

	private CourseEnrollmentInfo courseEnrollmentInfo() {
		return CourseEnrollmentInfo.builder()
			.enrollmentId(100L)
			.student(UserCardInfo.builder().userId(10L).name("수강생").build())
			.status(EnrollmentStatus.CONFIRMED)
			.enrolledAt(LocalDateTime.of(2026, 5, 1, 10, 0))
			.build();
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
