package com.liveklass.domain.enrollment.service.command;

import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.user.entity.User;

public interface EnrollmentCommandService {

	Enrollment savePending(User student, Course course);

	void expire(Enrollment enrollment);
}
