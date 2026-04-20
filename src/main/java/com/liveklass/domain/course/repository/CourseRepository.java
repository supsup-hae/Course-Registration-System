package com.liveklass.domain.course.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.liveklass.domain.course.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

	@Query("SELECT c FROM Course c JOIN FETCH c.creator WHERE c.courseId = :courseId")
	Optional<Course> findByIdWithCreator(@Param("courseId") Long courseId);

}