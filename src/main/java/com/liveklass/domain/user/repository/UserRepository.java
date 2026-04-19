package com.liveklass.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liveklass.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
