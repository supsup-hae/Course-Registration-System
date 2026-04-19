package com.liveklass.domain.course.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.liveklass.common.entity.BaseEntity;
import com.liveklass.domain.course.enums.CourseStatus;

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
	name = "courses",
	indexes = {
		@Index(name = "IX_COURSES_CREATOR", columnList = "creator_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "course_id")
	private Long courseId;

	@Column(name = "creator_id", nullable = false)
	private Long creatorId;

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "price", nullable = false)
	private BigDecimal price;

	@Column(name = "capacity", nullable = false)
	private Integer capacity;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, columnDefinition = "course_status")
	private CourseStatus status;

	@Column(name = "start_date")
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Builder
	private Course(
		Long creatorId,
		String title,
		String description,
		BigDecimal price,
		Integer capacity,
		CourseStatus status,
		LocalDateTime startDate,
		LocalDateTime endDate
	) {
		this.creatorId = creatorId;
		this.title = title;
		this.description = description;
		this.price = price;
		this.capacity = capacity;
		this.status = status;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public static Course createDraft(
		final Long creatorId,
		final String title,
		final String description,
		final BigDecimal price,
		final Integer capacity,
		final LocalDateTime startDate,
		final LocalDateTime endDate
	) {
		return Course.builder()
			.creatorId(creatorId)
			.title(title)
			.description(description)
			.price(price)
			.capacity(capacity)
			.status(CourseStatus.DRAFT)
			.startDate(startDate)
			.endDate(endDate)
			.build();
	}
}
