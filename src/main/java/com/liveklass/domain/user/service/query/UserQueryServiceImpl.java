package com.liveklass.domain.user.service.query;

import org.springframework.stereotype.Service;

import com.liveklass.common.error.ErrorCode;
import com.liveklass.domain.user.entity.User;
import com.liveklass.domain.user.exception.UserException;
import com.liveklass.domain.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQueryServiceImpl implements UserQueryService {
	private final UserRepository userRepository;

	@Override
	public User findById(final Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
		log.info("[User] 사용자 조회 완료 : id = {}, role = {}", id, user.getRole());
		return user;
	}
}
