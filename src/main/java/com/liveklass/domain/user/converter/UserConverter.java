package com.liveklass.domain.user.converter;

import com.liveklass.domain.user.dto.common.UserCardInfo;
import com.liveklass.domain.user.dto.common.UserInfoDto;
import com.liveklass.domain.user.entity.User;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserConverter {

	public UserInfoDto toUserInfo(User user) {
		return UserInfoDto.builder()
			.userId(user.getUserId())
			.name(user.getName())
			.email(user.getEmail())
			.role(user.getRole().name())
			.build();
	}

	public UserCardInfo toUserCardInfo(User user) {
		return UserCardInfo.builder()
			.userId(user.getUserId())
			.name(user.getName())
			.build();
	}
}

