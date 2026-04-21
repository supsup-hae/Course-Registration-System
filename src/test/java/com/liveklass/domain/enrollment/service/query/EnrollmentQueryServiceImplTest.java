package com.liveklass.domain.enrollment.service.query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.liveklass.domain.enrollment.repository.EnrollmentRepository;

@ExtendWith(MockitoExtension.class)
class EnrollmentQueryServiceImplTest {

	@Mock
	private EnrollmentRepository enrollmentRepository;

	@InjectMocks
	private EnrollmentQueryServiceImpl service;

	@Test
	@DisplayName("countActive() 호출 시 Repository에 위임")
	void countActiveDelegatesRepository() {
		// given
		given(enrollmentRepository.countActiveByCourseId(10L)).willReturn(7L);

		// when
		long result = service.countActive(10L);

		// then
		assertThat(result).isEqualTo(7L);
	}

	@Test
	@DisplayName("countActiveForUpdate() 호출 시 SELECT FOR UPDATE 쿼리 실행")
	void countActiveForUpdateExecutesPessimisticLock() {
		// given
		given(enrollmentRepository.countActiveByCourseId(10L)).willReturn(3L);

		// when
		long result = service.countActive(10L);

		// then
		assertThat(result).isEqualTo(3L);
	}

	@Test
	@DisplayName("existsActive() 호출 시 Repository에 위임")
	void existsActiveDelegatesRepository() {
		// given
		given(enrollmentRepository.existsActiveEnrollment(1L, 10L)).willReturn(true);

		// when
		boolean result = service.existsActive(1L, 10L);

		// then
		assertThat(result).isTrue();
	}
}
