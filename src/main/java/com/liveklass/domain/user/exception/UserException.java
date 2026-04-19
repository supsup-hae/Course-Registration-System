package com.liveklass.domain.user.exception;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.common.error.exception.BusinessException;

public class UserException extends BusinessException {
	public UserException(final ErrorCode errorCode) {
		super(errorCode);
	}
}
