package com.liveklass.domain.enrollment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
@Validated
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentController {

	private final EnrollmentFacadeService enrollmentFacadeService;

	@Operation(summary = "수강신청 생성", description = "특정 강의를 대상으로 수강신청을 요청하고 PENDING 상태로 대기합니다.")
	@PostMapping
	@PreAuthorize("hasRole('STUDENT')")
	public ResponseEntity<BaseResponse<EnrollmentResDto>> create(
		@AuthenticationPrincipal final UserPrincipal principal,
		@RequestBody @Valid final CreateEnrollmentReqDto request
	) {
		EnrollmentResDto response = enrollmentFacadeService.createPending(principal.userId(), request.courseId());
		return ResponseUtils.created(response);
	}
}
