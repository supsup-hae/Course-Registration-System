package com.liveklass.domain.course.controller.query;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liveklass.common.response.BaseResponse;
import com.liveklass.common.response.PageResponse;
import com.liveklass.common.util.ResponseUtils;
import com.liveklass.domain.course.dto.common.CourseCardInfo;
import com.liveklass.domain.course.dto.common.CourseInfoDto;
import com.liveklass.domain.course.dto.request.FindCoursesReqDto;
import com.liveklass.domain.course.service.facade.CourseFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "강의", description = "강의 관련 API")
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
		@Valid @ModelAttribute final FindCoursesReqDto findCoursesReqDto
	) {
		Page<CourseCardInfo> response = courseFacadeService.findAllCourses(findCoursesReqDto);
		return ResponseUtils.page(response);
	}

}
