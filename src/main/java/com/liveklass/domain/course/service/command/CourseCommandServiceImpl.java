package com.liveklass.domain.course.service.command;

import org.springframework.stereotype.Service;

import com.liveklass.domain.course.entity.Course;
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

}
