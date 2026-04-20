package com.liveklass.domain.course.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.course.enums.CourseStatus;

public interface CourseRepository extends JpaRepository<Course, Long> {

	@Query("SELECT c FROM Course c JOIN FETCH c.creator WHERE c.courseId = :courseId")
	Optional<Course> findByIdWithCreator(@Param("courseId") Long courseId);

	@Query(
		value = """
			SELECT c FROM Course c JOIN FETCH c.creator
			WHERE (:status IS NULL OR c.status = :status)
			AND (:minPrice IS NULL OR c.price >= :minPrice)
			AND (:maxPrice IS NULL OR c.price <= :maxPrice)
			AND (:hasCapacity IS NULL OR :hasCapacity = false OR c.capacity IS NOT NULL)
			""",
		countQuery = """
			SELECT COUNT(c) FROM Course c
			WHERE (:status IS NULL OR c.status = :status)
			AND (:minPrice IS NULL OR c.price >= :minPrice)
			AND (:maxPrice IS NULL OR c.price <= :maxPrice)
			AND (:hasCapacity IS NULL OR :hasCapacity = false OR c.capacity IS NOT NULL)
			"""
	)
	Page<Course> findAllWithFilters(
		@Param("status") CourseStatus status,
		@Param("minPrice") BigDecimal minPrice,
		@Param("maxPrice") BigDecimal maxPrice,
		@Param("hasCapacity") Boolean hasCapacity,
		Pageable pageable
	);
}