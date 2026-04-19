package com.liveklass.domain.course.service.command;

import java.time.LocalDateTime;

import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.enums.CourseStatus;

public interface CourseCommandService {

	Course registerCourse(Course course);

	void updateStatus(Course course, CourseStatus status);

	void openWith(Course course, LocalDateTime startDate, LocalDateTime endDate);
}
