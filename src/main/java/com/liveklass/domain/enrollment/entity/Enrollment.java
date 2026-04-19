package com.liveklass.domain.enrollment.entity;

import java.time.LocalDateTime;

import com.liveklass.common.entity.BaseEntity;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "enrollments",
	indexes = {
		@Index(name = "IX_ENROLLMENTS_STUDENT", columnList = "student_id"),
		@Index(name = "IX_ENROLLMENTS_COURSE", columnList = "course_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "enrollment_id")
	private Long enrollmentId;

	@Column(name = "student_id", nullable = false)
	private Long studentId;

	@Column(name = "course_id", nullable = false)
	private Long courseId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, columnDefinition = "enrollment_status")
	private EnrollmentStatus status;

	@Column(name = "waitlist_order")
	private Integer waitlistOrder;

	@Column(name = "confirmed_at")
	private LocalDateTime confirmedAt;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	@Builder
	private Enrollment(
		final Long studentId,
		final Long courseId,
		final EnrollmentStatus status,
		final Integer waitlistOrder
	) {
		this.studentId = studentId;
		this.courseId = courseId;
		this.status = status;
		this.waitlistOrder = waitlistOrder;
	}

	public static Enrollment pending(final Long studentId, final Long courseId) {
		return Enrollment.builder()
			.studentId(studentId)
			.courseId(courseId)
			.status(EnrollmentStatus.PENDING)
			.build();
	}

	public static Enrollment waitlisted(final Long studentId, final Long courseId, final Integer waitlistOrder) {
		return Enrollment.builder()
			.studentId(studentId)
			.courseId(courseId)
			.status(EnrollmentStatus.WAITLISTED)
			.waitlistOrder(waitlistOrder)
			.build();
	}
}
