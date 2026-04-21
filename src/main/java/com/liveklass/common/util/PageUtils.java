package com.liveklass.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.common.error.exception.BusinessException;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PageUtils {

	public PageRequest of(int page, int size) {
		validatePagination(page, size);
		return PageRequest.of(page, size);
	}

	public PageRequest of(int page, int size, Direction sortOrder, String... properties) {
		validatePagination(page, size, sortOrder);
		return PageRequest.of(page, size, Sort.by(sortOrder, properties));
	}

	public void validatePagination(int page, int size) {
		if (page < 0 || size <= 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	public void validatePagination(int page, int size, Direction sortOrder) {
		if (page < 0 || size <= 0 || sortOrder == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}
}
