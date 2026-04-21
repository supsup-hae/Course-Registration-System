package com.liveklass.domain.course.controller.query;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.liveklass.common.response.BaseResponse;
import com.liveklass.common.response.PageResponse;
import com.liveklass.common.security.UserPrincipal;
import com.liveklass.common.util.ResponseUtils;
import com.liveklass.domain.course.dto.common.CourseCardInfo;
import com.liveklass.domain.course.dto.common.CourseInfoDto;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.service.facade.CourseFacadeService;
import com.liveklass.domain.enrollment.dto.response.CourseEnrollmentInfo;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "강의", description = "강의 관련 API")
@Validated
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseQueryController {

	private final CourseFacadeService courseFacadeService;

	@Operation(summary = "강의 상세 조회", description = "강의의 상세 정보를 조회합니다.")
	@GetMapping("/{courseId}")
	public ResponseEntity<BaseResponse<CourseInfoDto>> findCourseDetail(
		@PathVariable final Long courseId
	) {
		CourseInfoDto response = courseFacadeService.findCourseDetail(courseId);
		return ResponseUtils.ok(response);
	}

	@Operation(summary = "강의 목록 조회", description = "전체 강의 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<PageResponse<CourseCardInfo>> findAllCourses(
		@Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
		@RequestParam(defaultValue = "0") @Min(0) final int page,

		@Parameter(description = "페이지 크기", example = "10")
		@RequestParam(defaultValue = "10") @Min(1) @Max(100) final int size,

		@Parameter(description = "강의 상태 필터링 (DRAFT, OPEN, CLOSED)", example = "OPEN")
		@RequestParam(required = false) final CourseStatus status,

		@Parameter(description = "최소 수강료 (0 이상)", example = "10000")
		@RequestParam(required = false) @PositiveOrZero final BigDecimal minPrice,

		@Parameter(description = "최대 수강료 (0 이상)", example = "50000")
		@RequestParam(required = false) @PositiveOrZero final BigDecimal maxPrice,

		@Parameter(description = "정원 제한 여부 필터링", example = "true")
		@RequestParam(required = false) final Boolean hasCapacity
	) {
		Page<CourseCardInfo> response = courseFacadeService.findAllCourses(
			page, size, status, minPrice, maxPrice, hasCapacity
		);
		return ResponseUtils.page(response);
	}

	@PreAuthorize("hasRole('CREATOR')")
	@Operation(summary = "강의별 수강생 목록 조회", description = "크리에이터가 자신의 강의에 대한 수강생 목록을 조회합니다.")
	@GetMapping("/{courseId}/enrollments")
	public ResponseEntity<PageResponse<CourseEnrollmentInfo>> findCourseEnrollments(
		@AuthenticationPrincipal final UserPrincipal principal,

		@PathVariable final Long courseId,

		@Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
		@RequestParam(defaultValue = "0") @Min(0) final int page,

		@Parameter(description = "페이지 크기", example = "10")
		@RequestParam(defaultValue = "10") @Min(1) @Max(100) final int size,

		@Parameter(description = "수강신청 상태 필터 (PENDING, CONFIRMED, CANCELLED 등)")
		@RequestParam(required = false) final EnrollmentStatus status,

		@Parameter(description = "정렬 순서 (ASC, DESC)", example = "DESC")
		@RequestParam(defaultValue = "DESC") final Sort.Direction sortOrder
	) {
		Page<CourseEnrollmentInfo> response = courseFacadeService.findCourseEnrollments(
			principal.userId(), courseId, page, size, status, sortOrder
		);
		return ResponseUtils.page(response);
	}
}
