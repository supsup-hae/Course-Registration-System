package com.liveklass.domain.enrollment.policy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnrollmentPolicy {

	public static final int PENDING_TTL_MINUTES = 15;
	public static final String REASON_TTL_EXPIRED = "TTL_EXPIRED";
	public static final String REASON_USER_REQUEST = "USER_REQUEST";
}
