package com.liveklass.domain.enrollment.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.liveklass.domain.enrollment.entity.Enrollment;
import com.liveklass.domain.enrollment.enums.EnrollmentStatus;
import com.liveklass.domain.enrollment.event.SlotReleasedEvent;
import com.liveklass.domain.enrollment.repository.EnrollmentRepository;
import com.liveklass.domain.enrollment.service.concurrency.EnrollmentSlotCounter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentExpiryScheduler {

	private final ApplicationEventPublisher eventPublisher;
	private final EnrollmentRepository enrollmentRepository;
	private final EnrollmentSlotCounter redisCounter;

	@Scheduled(cron = "0 * * * * *")
	@Transactional
	public void sweep() {
		List<Enrollment> expired = enrollmentRepository.findExpiredPending(
				EnrollmentStatus.PENDING,
				LocalDateTime.now());
		if (CollectionUtils.isEmpty(expired)) {
			return;
		}
		for (Enrollment e : expired) {
			e.expire();
			Long courseId = e.getCourse().getCourseId();
			redisCounter.decrement(courseId);
			eventPublisher.publishEvent(new SlotReleasedEvent(courseId));
		}
		log.info("[Enrollment] 만료 PENDING 일괄 처리 완료 : count = {}", expired.size());
	}
}
