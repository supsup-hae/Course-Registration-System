package com.liveklass.domain.course.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.liveklass.common.entity.BaseEntity;
import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;
import com.liveklass.domain.course.enums.CourseStatus;
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
	name = "courses",
	indexes = {
		@Index(name = "IX_COURSES_CREATOR", columnList = "creator_id"),
		@Index(name = "IX_COURSES_PRICE", columnList = "price")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "course_id")
	private Long courseId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_id", nullable = false)
	private User creator;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "description")
	private String description;

	@Column(name = "price", nullable = false, precision = 12, scale = 0)
	private BigDecimal price;

	@Column(name = "capacity")
	private Integer capacity;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private CourseStatus status;

	@Column(name = "start_date")
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Builder
	private Course(
		User creator,
		String title,
		String description,
		BigDecimal price,
		Integer capacity,
		CourseStatus status,
		LocalDateTime startDate,
		LocalDateTime endDate
	) {
		this.creator = creator;
		this.title = title;
		this.description = description;
		this.price = price;
		this.capacity = capacity;
		this.status = status;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public static Course createDraft(final User creator, final RegisterCourseReqDto dto) {
		return Course.builder()
			.creator(creator)
			.title(dto.title())
			.description(dto.description())
			.price(dto.price())
			.capacity(dto.capacity())
			.status(CourseStatus.DRAFT)
			.startDate(dto.startDate())
			.endDate(dto.endDate())
			.build();
	}

	public void updateStatus(final CourseStatus status) {
		this.status = status;
	}

	public boolean canTransitionTo(final CourseStatus next) {
		return switch (this.status) {
			case DRAFT, CLOSED -> next == CourseStatus.OPEN;
			case OPEN -> next == CourseStatus.CLOSED;
		};
	}

	public void openWith(final LocalDateTime startDate, final LocalDateTime endDate) {
		this.status = CourseStatus.OPEN;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public boolean isUnlimitedCapacity() {
		return this.capacity == null;
	}
}
