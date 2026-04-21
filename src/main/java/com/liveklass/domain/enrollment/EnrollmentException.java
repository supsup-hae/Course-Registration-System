package com.liveklass.domain.enrollment;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.common.error.exception.BusinessException;

public class EnrollmentException extends BusinessException {
	public EnrollmentException(final ErrorCode errorCode) {
		super(errorCode);
	}
}
