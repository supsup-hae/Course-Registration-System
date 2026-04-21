package com.liveklass.domain.enrollment.entity;

import java.time.LocalDateTime;

import com.liveklass.common.entity.BaseEntity;
import com.liveklass.domain.course.entity.Course;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.enrollment.policy.EnrollmentPolicy;
import com.liveklass.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
		@Index(name = "IX_ENROLLMENTS_COURSE", columnList = "course_id"),
		@Index(name = "IX_ENROLLMENTS_EXPIRES_AT", columnList = "expires_at")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "enrollment_id")
	private Long enrollmentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private User student;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private EnrollmentStatus status;

	@Column(name = "waitlist_order")
	private Integer waitlistOrder;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	@Column(name = "confirmed_at")
	private LocalDateTime confirmedAt;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	@Column(name = "cancelled_reason", length = 100)
	private String cancelledReason;

	@Builder
	private Enrollment(
		final User student,
		final Course course,
		final EnrollmentStatus status,
		final Integer waitlistOrder,
		final LocalDateTime expiresAt
	) {
		this.student = student;
		this.course = course;
		this.status = status;
		this.waitlistOrder = waitlistOrder;
		this.expiresAt = expiresAt;
	}

	public static Enrollment pending(final User student, final Course course, final LocalDateTime now) {
		return Enrollment.builder()
			.student(student)
			.course(course)
			.status(EnrollmentStatus.PENDING)
			.expiresAt(now.plusMinutes(EnrollmentPolicy.PENDING_TTL_MINUTES))
			.build();
	}

	public static Enrollment waitlisted(final User student, final Course course, final Integer waitlistOrder) {
		return Enrollment.builder()
			.student(student)
			.course(course)
			.status(EnrollmentStatus.WAITLISTED)
			.waitlistOrder(waitlistOrder)
			.build();
	}

	public void confirm() {
		this.status = EnrollmentStatus.CONFIRMED;
		if (this.confirmedAt == null) {
			this.confirmedAt = LocalDateTime.now();
		}
	}

	public void expire() {
		markCancelled(EnrollmentPolicy.REASON_TTL_EXPIRED);
	}

	public void cancelByUser() {
		markCancelled(EnrollmentPolicy.REASON_USER_REQUEST);
	}

	private void markCancelled(final String reason) {
		this.status = EnrollmentStatus.CANCELLED;
		this.cancelledReason = reason;
		if (this.cancelledAt == null) {
			this.cancelledAt = LocalDateTime.now();
		}
	}

	public boolean isPending() {
		return this.status == EnrollmentStatus.PENDING;
	}
}
