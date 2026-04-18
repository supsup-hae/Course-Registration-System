package com.liveklass.common.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record PageResponse<T>(
	List<T> content,
	boolean hasNext,
	int totalPages,
	long totalElements,
	int page,
	int size,
	boolean isFirst,
	boolean isLast
) {
	public static <T> PageResponse<T> of(@NonNull Page<T> page) {
		return PageResponse.<T>builder()
			.content(page.getContent())
			.hasNext(page.hasNext())
			.totalPages(page.getTotalPages())
			.totalElements(page.getTotalElements())
			.page(page.getNumber())
			.size(page.getSize())
			.isFirst(page.isFirst())
			.isLast(page.isLast())
			.build();
	}
}
