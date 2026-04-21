package com.liveklass.domain.enrollment.service.concurrency;

public interface EnrollmentSlotCounter {

	boolean tryIncrement(Long courseId, int capacity);

	void decrement(Long courseId);

	long get(Long courseId);

	void set(Long courseId, long value);
}
