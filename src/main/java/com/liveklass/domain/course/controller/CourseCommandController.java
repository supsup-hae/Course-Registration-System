package com.liveklass.domain.course.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liveklass.common.response.BaseResponse;
import com.liveklass.common.security.UserPrincipal;
import com.liveklass.common.util.ResponseUtils;
import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.dto.response.RegisterCourseResDto;
import com.liveklass.domain.course.service.facade.CourseFacadeService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseCommandController {

	private final CourseFacadeService courseFacadeService;

	@PostMapping
	@PreAuthorize("hasRole('CREATOR')")
	public ResponseEntity<BaseResponse<RegisterCourseResDto>> registerCourse(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestBody @Valid final RegisterCourseReqDto reqDto
	) {
		RegisterCourseResDto response = courseFacadeService.registerCourse(principal.userId(), reqDto);
		return ResponseUtils.created(response);
	}
}
