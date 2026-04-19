package com.liveklass.domain.course.controller.command;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liveklass.common.response.BaseResponse;
import com.liveklass.common.security.UserPrincipal;
import com.liveklass.common.util.ResponseUtils;
import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.dto.request.UpdateCourseStatusReqDto;
import com.liveklass.domain.course.dto.response.RegisterCourseResDto;
import com.liveklass.domain.course.dto.response.UpdateCourseStatusResDto;
import com.liveklass.domain.course.service.facade.CourseFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "강의", description = "강의 관련 API")
@Validated
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseCommandController {

	private final CourseFacadeService courseFacadeService;

	@Operation(summary = "신규 강의 등록", description = "새로운 강의를 초안(DRAFT) 상태로 등록합니다.")
	@PostMapping
	@PreAuthorize("hasRole('CREATOR')")
	public ResponseEntity<BaseResponse<RegisterCourseResDto>> registerCourse(
		@AuthenticationPrincipal final UserPrincipal principal,
		@RequestBody @Valid final RegisterCourseReqDto reqDto
	) {
		RegisterCourseResDto response = courseFacadeService.registerCourse(principal.userId(), reqDto);
		return ResponseUtils.created(response);
	}

	@Operation(summary = "강의 상태 변경", description = "작성자가 본인의 강의 상태를 변경합니다 (예: OPEN, CLOSED).")
	@PatchMapping("/{courseId}/status")
	@PreAuthorize("hasRole('CREATOR')")
	public ResponseEntity<BaseResponse<UpdateCourseStatusResDto>> updateCourseStatus(
		@AuthenticationPrincipal final UserPrincipal principal,
		@RequestBody @Valid final UpdateCourseStatusReqDto reqDto,
		@PathVariable @Positive(message = "올바른 강의 ID를 입력해주세요.") final Long courseId
	) {
		UpdateCourseStatusResDto response = courseFacadeService.updateCourseStatus(
			principal.userId(), courseId, reqDto
		);
		return ResponseUtils.ok(response);
	}
}
