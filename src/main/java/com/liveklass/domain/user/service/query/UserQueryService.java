package com.liveklass.domain.user.service.query;

import com.liveklass.domain.user.entity.User;

public interface UserQueryService {
	User findById(Long id);
}
