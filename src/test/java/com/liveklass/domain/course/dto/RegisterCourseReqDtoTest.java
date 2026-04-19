package com.liveklass.domain.course.dto;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.liveklass.domain.course.dto.request.RegisterCourseReqDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class RegisterCourseReqDtoTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	@DisplayName("capacity가 null이면 유효성 검사 통과 (무제한 정원)")
	void nullCapacityPassesValidation() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.valueOf(10000), null, null, null
		);

		// when
		Set<ConstraintViolation<RegisterCourseReqDto>> violations = validator.validate(dto);

		// then
		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("capacity가 0이면 유효성 검사 실패")
	void zeroCapacityFailsValidation() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.valueOf(10000), 0, null, null
		);

		// when
		Set<ConstraintViolation<RegisterCourseReqDto>> violations = validator.validate(dto);

		// then
		assertThat(violations)
			.isNotEmpty()
			.anyMatch(v -> v.getPropertyPath().toString().equals("capacity"));
	}

	@Test
	@DisplayName("capacity가 음수이면 유효성 검사 실패")
	void negativeCapacityFailsValidation() {
		// given
		RegisterCourseReqDto dto = new RegisterCourseReqDto(
			"테스트 강의", null, BigDecimal.valueOf(10000), -1, null, null
		);

		// when
		Set<ConstraintViolation<RegisterCourseReqDto>> violations = validator.validate(dto);

		// then
		assertThat(violations)
			.isNotEmpty()
			.anyMatch(v -> v.getPropertyPath().toString().equals("capacity"));
	}
}
