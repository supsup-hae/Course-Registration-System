package com.liveklass.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PageUtils {

	public PageRequest of(int page, int size) {
		return PageRequest.of(page, size);
	}

	public PageRequest of(int page, int size, Direction sortOrder, String... properties) {
		return PageRequest.of(page, size, Sort.by(sortOrder, properties));
	}
}
