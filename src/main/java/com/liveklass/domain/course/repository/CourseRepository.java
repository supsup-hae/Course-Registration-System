package com.liveklass.domain.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liveklass.domain.course.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

}