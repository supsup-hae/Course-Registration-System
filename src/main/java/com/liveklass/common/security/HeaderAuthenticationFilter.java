package com.liveklass.common.security;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.liveklass.domain.user.enums.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HeaderAuthenticationFilter extends OncePerRequestFilter {

	private static final String HEADER_USER_ID = "X-User-Id";
	private static final String HEADER_USER_ROLE = "X-User-Role";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String userIdHeader = request.getHeader(HEADER_USER_ID);
		String roleHeader = request.getHeader(HEADER_USER_ROLE);

		if (userIdHeader != null && roleHeader != null) {
			try {
				Long userId = Long.parseLong(userIdHeader);
				Role role = Role.valueOf(roleHeader.toUpperCase());
				UserPrincipal principal = new UserPrincipal(userId, role);
				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (IllegalArgumentException _) {
				log.warn("[Auth] 유효하지 않은 헤더값 - X-User-Id: {}, X-User-Role: {}", userIdHeader, roleHeader);
			}
		}

		filterChain.doFilter(request, response);
	}
}
