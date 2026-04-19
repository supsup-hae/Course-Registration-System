package com.liveklass.domain.user.converter;

import com.liveklass.domain.user.dto.common.UserInfo;
import com.liveklass.domain.user.entity.User;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserConverter {

	public UserInfo toUserInfo(User user) {
		return UserInfo.builder()
			.userId(user.getUserId())
			.name(user.getName())
			.email(user.getEmail())
			.role(user.getRole().name())
			.build();
	}
}

