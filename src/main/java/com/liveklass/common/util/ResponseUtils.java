package com.liveklass.common.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.liveklass.common.response.BaseResponse;
import com.liveklass.common.response.PageResponse;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseUtils {
	public <T> ResponseEntity<BaseResponse<T>> ok(T data) {
		return ResponseEntity.ok(BaseResponse.ok(data));
	}

	public <T> ResponseEntity<BaseResponse<T>> created(T data) {
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.created(data));
	}

	public ResponseEntity<Void> noContent() {
		return ResponseEntity.noContent().build();
	}

	public <T> ResponseEntity<BaseResponse<T>> accepted() {
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(BaseResponse.accepted());
	}

	public <T> ResponseEntity<PageResponse<T>> page(Page<T> page) {
		return ResponseEntity.ok(PageResponse.of(page));
	}
}
