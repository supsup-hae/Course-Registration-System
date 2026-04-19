package com.liveklass.domain.course.exception;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.common.error.exception.BusinessException;

public class CourseException extends BusinessException {
	public CourseException(final ErrorCode errorCode) {
		super(errorCode);
	}
}
