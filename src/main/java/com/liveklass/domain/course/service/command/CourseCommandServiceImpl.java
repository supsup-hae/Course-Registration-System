package com.liveklass.domain.course.service.command;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.enums.CourseStatus;
import com.liveklass.domain.course.repository.CourseRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseCommandServiceImpl implements CourseCommandService {

	private final CourseRepository courseRepository;

	@Override
	public Course registerCourse(final Course course) {
		Course savedCourse = courseRepository.save(course);
		log.info("[Course] 강의 등록 완료 : id = {}, creatorId = {}",
			savedCourse.getCourseId(), savedCourse.getCreator().getUserId());
		return savedCourse;
	}

	@Override
	public void updateStatus(final Course course, final CourseStatus status) {
		course.updateStatus(status);
		log.info("[Course] 강의 상태 변경 완료 : id = {}, status = {}", course.getCourseId(), status);
	}

	@Override
	public void openWith(final Course course, final LocalDateTime startDate, final LocalDateTime endDate) {
		course.openWith(startDate, endDate);
		log.info("[Course] 강의 OPEN 전환 완료 : id = {}, startDate = {}", course.getCourseId(), startDate);
	}

}
