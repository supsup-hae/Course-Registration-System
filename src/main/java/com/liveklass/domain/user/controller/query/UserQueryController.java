package com.liveklass.domain.user.controller.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.liveklass.common.response.PageResponse;
import com.liveklass.common.security.UserPrincipal;
import com.liveklass.common.util.ResponseUtils;
import com.liveklass.domain.enrollment.dto.common.EnrollmentCardInfo;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.user.service.facade.UserFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "사용자", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Validated
public class UserQueryController {

	private final UserFacadeService userFacadeService;

	@Operation(summary = "개인 수강신청 목록 조회", description = "현재 로그인한 사용자의 수강신청 목록을 조회합니다.")
	@PreAuthorize("hasRole('STUDENT')")
	@GetMapping("/me/enrollments")
	public ResponseEntity<PageResponse<EnrollmentCardInfo>> findMyEnrollments(
		@AuthenticationPrincipal final UserPrincipal principal,
		@Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
		@RequestParam(defaultValue = "0") @Min(0) final int page,
		@Parameter(description = "페이지 크기", example = "10")
		@RequestParam(defaultValue = "10") @Min(1) @Max(100) final int size,
		@Parameter(description = "수강신청 상태 필터 (PENDING, CONFIRMED, CANCELLED 등)")
		@RequestParam(required = false) final EnrollmentStatus status,
		@Parameter(description = "정렬 순서 (ASC, DESC)", example = "DESC")
		@RequestParam(defaultValue = "DESC") final Sort.Direction sortOrder
	) {
		Page<EnrollmentCardInfo> response = userFacadeService.findMyEnrollments(
			principal.userId(), page, size, status, sortOrder
		);
		return ResponseUtils.page(response);
	}
}
