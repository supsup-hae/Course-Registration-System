package com.liveklass.domain.enrollment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	@Query("""
		SELECT COUNT(e) FROM Enrollment e
		WHERE e.course.courseId = :courseId
		AND e.status IN (com.liveklass.domain.enrollment.enums.EnrollmentStatus.PENDING,
		com.liveklass.domain.enrollment.enums.EnrollmentStatus.CONFIRMED)
		""")
	long countActiveByCourseId(@Param("courseId") Long courseId);

	@Query("""
		SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
		FROM Enrollment e
		WHERE e.student.userId = :studentId
		AND e.course.courseId = :courseId
		AND e.status IN (com.liveklass.domain.enrollment.enums.EnrollmentStatus.PENDING,
		com.liveklass.domain.enrollment.enums.EnrollmentStatus.CONFIRMED)
		""")
	boolean existsActiveEnrollment(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
	@Query("""
		SELECT e FROM Enrollment e
		WHERE e.status = :status
		AND e.expiresAt <= :threshold
		""")
	List<Enrollment> findExpiredPending(
		@Param("status") EnrollmentStatus status,
		@Param("threshold") LocalDateTime threshold
	);

	@Query("""
		SELECT e FROM Enrollment e
		JOIN FETCH e.student
		WHERE e.enrollmentId = :enrollmentId
		""")
	Optional<Enrollment> findWithStudentById(@Param("enrollmentId") Long enrollmentId);

	@Query("""
		SELECT e FROM Enrollment e
		JOIN FETCH e.course
		JOIN FETCH e.student
		WHERE e.enrollmentId = :enrollmentId
		""")
	Optional<Enrollment> findWithCourseAndStudentById(@Param("enrollmentId") Long enrollmentId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT e FROM Enrollment e
		JOIN FETCH e.course
		JOIN FETCH e.student
		WHERE e.enrollmentId = :enrollmentId
		""")
	Optional<Enrollment> findWithCourseAndStudentByIdForUpdate(@Param("enrollmentId") Long enrollmentId);

	@Query(
		value = """
			SELECT e FROM Enrollment e
			JOIN FETCH e.course c
			JOIN FETCH c.creator
			WHERE e.student.userId = :studentId
			AND (:status IS NULL OR e.status = :status)
			""",
		countQuery = """
			SELECT COUNT(e) FROM Enrollment e
			WHERE e.student.userId = :studentId
			AND (:status IS NULL OR e.status = :status)
			"""
	)
	Page<Enrollment> findByStudentId(
		@Param("studentId") Long studentId,
		@Param("status") EnrollmentStatus status,
		Pageable pageable
	);
}
