package com.liveklass.domain.enrollment.controller.command;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liveklass.common.response.BaseResponse;
import com.liveklass.common.security.UserPrincipal;
import com.liveklass.common.util.ResponseUtils;
import com.liveklass.domain.enrollment.dto.request.CreateEnrollmentReqDto;
import com.liveklass.domain.enrollment.dto.response.EnrollmentResDto;
import com.liveklass.domain.enrollment.service.facade.EnrollmentFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "수강신청", description = "수강신청 관련 API")
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentCommandController {

	private final EnrollmentFacadeService enrollmentFacadeService;

	@Operation(summary = "수강신청 생성", description = "특정 강의를 대상으로 수강신청을 요청하고 PENDING 상태로 대기합니다.")
	@PostMapping
	@PreAuthorize("hasRole('STUDENT')")
	public ResponseEntity<BaseResponse<EnrollmentResDto>> createEnrollment(
		@AuthenticationPrincipal final UserPrincipal principal,
		@RequestBody @Valid final CreateEnrollmentReqDto request
	) {
		EnrollmentResDto response = enrollmentFacadeService.createPendingEnrollment(
			principal.userId(), request.courseId());
		return ResponseUtils.created(response);
	}

	@Operation(summary = "수강신청 확정", description = "PENDING 상태의 수강신청을 CONFIRMED 상태로 변경합니다. (외부 결제 연동 대체)")
	@PatchMapping("/{id}/confirm")
	@PreAuthorize("hasRole('STUDENT')")
	public ResponseEntity<BaseResponse<EnrollmentResDto>> confirmEnrollment(
		@AuthenticationPrincipal final UserPrincipal principal,
		@PathVariable("id") final Long enrollmentId
	) {
		EnrollmentResDto response = enrollmentFacadeService.confirmEnrollment(principal.userId(), enrollmentId);
		return ResponseUtils.ok(response);
	}

	@Operation(summary = "수강신청 취소", description = "수강신청을 취소합니다. (학생 변심 또는 환불 등)")
	@PatchMapping("/{id}/cancel")
	@PreAuthorize("hasRole('STUDENT')")
	public ResponseEntity<BaseResponse<EnrollmentResDto>> cancelEnrollment(
		@AuthenticationPrincipal final UserPrincipal principal,
		@PathVariable("id") final Long enrollmentId
	) {
		EnrollmentResDto response = enrollmentFacadeService.cancelEnrollment(principal.userId(), enrollmentId);
		return ResponseUtils.ok(response);
	}

}
